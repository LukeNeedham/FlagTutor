import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.IOException
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * Refreshes the bundled flag images from each country's Wikipedia flag article.
 *
 * For every entry in [linksFile] (`{ "<alpha2>": "https://en.wikipedia.org/wiki/Flag_of_X" }`) it asks
 * the Wikipedia API for the article's lead image, which for a flag article is the current flag, then
 * downloads a PNG rendering of it, about [thumbnailWidth]px wide (Wikimedia rounds to its standard
 * thumbnail sizes, so 320 becomes 330), into [flagsDir] as `<alpha2>.png`.
 *
 * Wikipedia only needs one batched API call per [API_BATCH_SIZE] flags; the images themselves come
 * from thumb.wikimedia.org, so the task needs network access to both hosts. It is deliberately not
 * cacheable or up-to-date checked, since the answer depends on what Wikipedia currently shows.
 *
 * Safety rails, because a bad lead image would silently corrupt a flag:
 *  - the image's file name must contain "flag", otherwise the existing image is kept and the country
 *    is reported as skipped;
 *  - one failed country never aborts the run, but [CIRCUIT_BREAKER_THRESHOLD] failures in a row
 *    (almost always rate limiting) stop it early;
 *  - the downloaded bytes must be a PNG.
 *
 * Besides the images it writes [sourcesFile], mapping each country to the Wikimedia Commons file it
 * was taken from (for attribution and auditing), and prints which flags changed.
 *
 * Run with `-PdryRun` to resolve and list the images without downloading anything.
 */
abstract class DownloadWikipediaFlagsTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val linksFile: RegularFileProperty

    @get:OutputDirectory
    abstract val flagsDir: DirectoryProperty

    @get:OutputFile
    abstract val sourcesFile: RegularFileProperty

    @get:Input
    abstract val thumbnailWidth: Property<Int>

    /** Only handle these alpha-2 codes (comma separated); empty means every country. */
    @get:Input
    @get:Optional
    abstract val onlyCodes: Property<String>

    /** Resolve and report the images, but do not download or write anything. */
    @get:Input
    abstract val dryRun: Property<Boolean>

    init {
        // The result depends on live Wikipedia content, so never treat the task as up to date.
        outputs.upToDateWhen { false }
    }

    private val client: HttpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(30))
        .build()

    @TaskAction
    fun download() {
        val links = readLinks()
        val wanted = onlyCodes.orNull.orEmpty().split(',').map { it.trim().lowercase() }.filter { it.isNotEmpty() }
        val selected = links.filterKeys { wanted.isEmpty() || it in wanted }
        if (selected.isEmpty()) throw GradleException("No countries selected from ${linksFile.get().asFile}")

        val images = resolveLeadImages(selected)

        val outDir = flagsDir.get().asFile.also { it.mkdirs() }
        val sources = readExistingSources().toSortedMap()
        val changed = mutableListOf<String>()
        val unchanged = mutableListOf<String>()
        val skipped = mutableListOf<String>()
        val failed = mutableListOf<String>()
        var consecutiveFailures = 0

        for ((code, title) in selected) {
            val image = images[code]
            if (image == null) {
                skipped += "$code: no lead image found for \"$title\""
                continue
            }
            if (!image.fileName.contains("flag", ignoreCase = true)) {
                skipped += "$code: lead image ${image.fileName} of \"$title\" is not a flag, keeping the existing file"
                continue
            }
            if (dryRun.get()) {
                logger.lifecycle("$code <- ${image.fileName}  (${image.thumbnailUrl})")
                continue
            }

            try {
                val bytes = fetch(image.thumbnailUrl)
                check(bytes.isPng()) { "response was not a PNG" }
                val target = outDir.resolve("$code.png")
                if (target.exists() && target.readBytes().contentEquals(bytes)) {
                    unchanged += code
                } else {
                    target.writeBytes(bytes)
                    changed += code
                }
                sources[code] = image.fileName
                consecutiveFailures = 0
            } catch (e: Exception) {
                failed += "$code: ${image.fileName}: ${e.message}"
                if (++consecutiveFailures >= CIRCUIT_BREAKER_THRESHOLD) {
                    logger.error("$CIRCUIT_BREAKER_THRESHOLD downloads failed in a row, assuming Wikimedia is blocking or rate limiting this runner. Stopping early.")
                    break
                }
            }
            Thread.sleep(REQUEST_DELAY_MS)
        }

        if (!dryRun.get()) {
            val target = sourcesFile.get().asFile
            target.parentFile.mkdirs()
            target.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(sources)) + "\n")
        }

        logger.lifecycle("Flags changed (${changed.size}): ${changed.joinToString()}")
        logger.lifecycle("Flags already up to date: ${unchanged.size}")
        skipped.forEach { logger.warn("Skipped $it") }
        failed.forEach { logger.warn("Failed $it") }
        if (failed.isNotEmpty() && changed.isEmpty() && unchanged.isEmpty()) {
            throw GradleException("Every flag download failed, see the warnings above.")
        }
    }

    private data class LeadImage(val fileName: String, val thumbnailUrl: String)

    private fun readLinks(): Map<String, String> {
        @Suppress("UNCHECKED_CAST")
        return JsonSlurper().parse(linksFile.get().asFile) as Map<String, String>
    }

    private fun readExistingSources(): MutableMap<String, String> {
        val file = sourcesFile.get().asFile
        if (!file.exists()) return mutableMapOf()
        @Suppress("UNCHECKED_CAST")
        return (JsonSlurper().parse(file) as Map<String, String>).toMutableMap()
    }

    /** Wikipedia article title from `https://en.wikipedia.org/wiki/<Title>`, with spaces for underscores. */
    private fun titleOf(url: String): String =
        URLDecoder.decode(url.substringAfter("/wiki/").substringBefore('#'), Charsets.UTF_8).replace('_', ' ')

    private fun resolveLeadImages(selected: Map<String, String>): Map<String, LeadImage> {
        val result = mutableMapOf<String, LeadImage>()
        for (batch in selected.entries.chunked(API_BATCH_SIZE)) {
            val titlesByCode = batch.associate { it.key to titleOf(it.value) }
            val query = mapOf(
                "action" to "query",
                "prop" to "pageimages",
                "piprop" to "thumbnail|name",
                "pithumbsize" to thumbnailWidth.get().toString(),
                "titles" to titlesByCode.values.toSet().joinToString("|"),
                "redirects" to "1",
                "format" to "json",
                "formatversion" to "2",
            ).entries.joinToString("&") { (k, v) -> "$k=${URLEncoder.encode(v, Charsets.UTF_8)}" }

            val json = JsonSlurper().parseText(fetch("$API_URL?$query").toString(Charsets.UTF_8)) as Map<*, *>
            val q = json["query"] as Map<*, *>
            // The API reports title normalisation and redirects separately; follow both to find each page.
            val aliases = mutableMapOf<String, String>()
            for (key in listOf("normalized", "redirects")) {
                (q[key] as? List<*>).orEmpty().forEach {
                    it as Map<*, *>
                    aliases[it["from"] as String] = it["to"] as String
                }
            }
            val pages = (q["pages"] as List<*>).associateBy { (it as Map<*, *>)["title"] as String }
            for ((code, title) in titlesByCode) {
                var resolved = title
                repeat(2) { resolved = aliases[resolved] ?: resolved }
                val page = pages[resolved] as? Map<*, *> ?: continue
                val name = page["pageimage"] as? String ?: continue
                val thumb = (page["thumbnail"] as? Map<*, *>)?.get("source") as? String ?: continue
                result[code] = LeadImage(name, thumb)
            }
            Thread.sleep(REQUEST_DELAY_MS)
        }
        return result
    }

    /** GET with the identifying User-Agent Wikimedia requires, honouring Retry-After on HTTP 429/503. */
    private fun fetch(url: String): ByteArray {
        var lastError: Exception? = null
        repeat(MAX_ATTEMPTS) { attempt ->
            try {
                val request = HttpRequest.newBuilder(URI.create(url))
                    .header("User-Agent", USER_AGENT)
                    .timeout(Duration.ofSeconds(60))
                    .GET()
                    .build()
                val response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())
                when (response.statusCode()) {
                    200 -> return response.body()
                    429, 503 -> {
                        val wait = response.headers().firstValue("Retry-After").map { it.toLongOrNull() }.orElse(null)
                            ?: (RETRY_BASE_DELAY_S * (attempt + 1))
                        lastError = IOException("HTTP ${response.statusCode()}")
                        Thread.sleep(minOf(wait, MAX_RETRY_DELAY_S) * 1000)
                    }
                    else -> throw IOException("HTTP ${response.statusCode()} for $url")
                }
            } catch (e: IOException) {
                lastError = e
                Thread.sleep(RETRY_BASE_DELAY_S * (attempt + 1) * 1000)
            }
        }
        throw IOException("giving up on $url: ${lastError?.message}")
    }

    private fun ByteArray.isPng() =
        size > 8 && this[0] == 0x89.toByte() && this[1] == 'P'.code.toByte() &&
            this[2] == 'N'.code.toByte() && this[3] == 'G'.code.toByte()

    private companion object {
        const val API_URL = "https://en.wikipedia.org/w/api.php"
        const val API_BATCH_SIZE = 25
        const val USER_AGENT = "Vexed/1.0 (https://github.com/lukeneedham/flagtutor; contact via GitHub)"
        const val REQUEST_DELAY_MS = 1_000L
        const val MAX_ATTEMPTS = 4
        const val RETRY_BASE_DELAY_S = 5L
        const val MAX_RETRY_DELAY_S = 60L
        const val CIRCUIT_BREAKER_THRESHOLD = 10
    }
}
