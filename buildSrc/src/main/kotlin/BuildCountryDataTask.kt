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
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * Builds `country_data.json`, the single data file describing every country, from Wikipedia.
 *
 * The country list is the ISO 3166-1 alpha-2 table on Wikipedia ("ISO 3166-1 alpha-2", the
 * "Officially assigned code elements" section). Nothing outside that table is added. For each
 * country the task records:
 *
 *  - `name`: the ISO English short name, as written in that table;
 *  - `wikipediaUrl`: the country's own article (the table links to it);
 *  - `flagWikipediaUrl`: the article dedicated to its flag, `Flag of <country>` by convention, with
 *    exceptions listed in [overridesFile] under `flagPages`;
 *  - `flagImage`: the flag's PNG, saved in [flagsDir] and referenced relative to its parent, taken
 *    from the lead image of the flag article; `null` for the countries in `noOfficialFlag`;
 *  - `flagImageSource`: the Wikimedia Commons file that PNG was rendered from, for attribution;
 *  - `flagNote`: only for countries without a flag, why (copied from the overrides file);
 *  - `symbolism`: a short text on what the flag symbolises, written from the flag article.
 *
 * `symbolism` is prose, so it cannot be generated; the task keeps whatever is already in the data file
 * and lists the countries that are still missing it. It is `null` for countries without a flag.
 *
 * Whether a country has no official flag is a judgement the articles sometimes word differently
 * ("no official flag", "no flag of its own", "French flag used"), so the list, with a reason for each,
 * lives in [overridesFile] where a human can change it. The task only prints a hint for any country whose flag article says
 * something similar and that is not on the list.
 *
 * A failed lookup or download never turns into a `null` flag: the task keeps the previous value where
 * there is one, and fails at the end otherwise, so a rate-limited run cannot silently drop flags.
 *
 * Needs network access to en.wikipedia.org and, to download images, thumb.wikimedia.org.
 */
abstract class BuildCountryDataTask : DefaultTask() {

    /**
     * ```
     * { "articles": { "<alpha2>": "Article title" },        country article, where the ISO table links somewhere odd
     *   "flagPages": { "<alpha2>": "Article title" },       flag article, where "Flag of <country>" does not exist
     *   "flagImageFiles": { "<alpha2>": "File name.svg" },  Commons file, where the flag article has no lead image
     *   "noOfficialFlag": { "<alpha2>": "reason" } }        countries whose flagImage is null, and why (copied to flagNote)
     * ```
     */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val overridesFile: RegularFileProperty

    @get:OutputFile
    abstract val dataFile: RegularFileProperty

    @get:OutputDirectory
    abstract val flagsDir: DirectoryProperty

    @get:Input
    abstract val thumbnailWidth: Property<Int>

    /** Only download images for these alpha-2 codes (comma separated); empty means all. Data is always rebuilt in full. */
    @get:Input
    @get:Optional
    abstract val onlyCodes: Property<String>

    /** Rebuild the data file without downloading any image. */
    @get:Input
    abstract val skipDownloads: Property<Boolean>

    /**
     * After a complete, problem-free download of every country, delete the PNGs in [flagsDir] that the data
     * file does not reference (for example a country that was dropped, or one that now has no flag), so the
     * directory holds exactly what Wikipedia provided. Ignored for partial or failed runs.
     */
    @get:Input
    abstract val pruneFlags: Property<Boolean>

    init {
        // The result depends on live Wikipedia content, so never treat the task as up to date.
        outputs.upToDateWhen { false }
    }

    private val client: HttpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(30))
        .build()

    private data class IsoRow(val code: String, val name: String, val articleTitle: String)
    private data class Page(val title: String, val isDisambiguation: Boolean)
    private data class LeadImage(val fileName: String, val thumbnailUrl: String)
    private data class Entry(
        val name: String,
        val wikipediaUrl: String?,
        val flagWikipediaUrl: String?,
        val flagImage: String?,
        val flagImageSource: String?,
        val flagNote: String?,
        val symbolism: String?,
    )

    @TaskAction
    fun build() {
        val overrides = readOverrides()
        val existing = readExisting()
        val problems = mutableListOf<String>()

        val rows = fetchIsoRows(overrides)
        logger.lifecycle("ISO 3166-1 alpha-2: ${rows.size} officially assigned codes")

        val countryPages = resolvePages(rows.map { it.articleTitle })
        val flagPageTitles = rows.associate { row ->
            val country = countryPages[row.articleTitle]
            row.code to (overrides.flagPages[row.code]?.let { listOf(it) }
                ?: if (country == null) emptyList() else listOf("Flag of ${country.title}", "Flag of the ${country.title}"))
        }
        val flagPages = resolvePages(flagPageTitles.values.flatten())
        val flagPageByCode = flagPageTitles.mapValues { (_, candidates) ->
            candidates.firstNotNullOfOrNull { flagPages[it] }?.takeUnless { it.isDisambiguation }
        }

        val withFlag = rows.filter { it.code !in overrides.noOfficialFlag && flagPageByCode[it.code] != null }
        val leadImages = fetchLeadImages(withFlag.associate { it.code to flagPageByCode.getValue(it.code)!!.title }).toMutableMap()
        leadImages.putAll(fetchCommonsFiles(overrides.flagImageFiles.filterKeys { it in withFlag.map { row -> row.code } && it !in leadImages }))
        printHints(rows, flagPageByCode, overrides)

        val wanted = onlyCodes.orNull.orEmpty().split(',').map { it.trim().lowercase() }.filter { it.isNotEmpty() }
        val outDir = flagsDir.get().asFile.also { it.mkdirs() }
        val entries = linkedMapOf<String, Entry>()
        val changed = mutableListOf<String>()
        var unchanged = 0
        var consecutiveFailures = 0
        var circuitOpen = false

        for (row in rows) {
            val code = row.code
            val previous = existing[code]
            val country = countryPages[row.articleTitle]?.takeUnless { it.isDisambiguation }
            val flagPage = flagPageByCode[code]
            if (country == null) problems += "$code: no Wikipedia article found for \"${row.articleTitle}\""
            if (flagPage == null) problems += "$code: no flag article found (tried ${flagPageTitles[code].orEmpty()}); add it to flagPages in the overrides file"

            var flagImage: String? = null
            var flagImageSource: String? = null
            if (code !in overrides.noOfficialFlag) {
                val lead = leadImages[code]
                val download = !skipDownloads.get() && !circuitOpen && (wanted.isEmpty() || code in wanted)
                val keepPrevious = previous?.flagImage != null
                when {
                    lead == null || !lead.fileName.contains("flag", ignoreCase = true) -> {
                        problems += if (lead == null) "$code: flag article ${flagPage?.title} has no lead image"
                        else "$code: lead image ${lead.fileName} of ${flagPage?.title} is not a flag"
                        if (keepPrevious) { flagImage = previous?.flagImage; flagImageSource = previous?.flagImageSource }
                    }
                    !download -> {
                        // Not refreshing this one: keep what is recorded, or just point at the file the next download will write.
                        flagImage = previous?.flagImage ?: "flags/$code.png"
                        flagImageSource = if (previous?.flagImage != null) previous.flagImageSource else lead.fileName
                    }
                    else -> try {
                        val bytes = fetch(lead.thumbnailUrl)
                        check(bytes.isPng()) { "response was not a PNG" }
                        val target = outDir.resolve("$code.png")
                        if (target.exists() && target.readBytes().contentEquals(bytes)) unchanged++ else {
                            target.writeBytes(bytes)
                            changed += code
                        }
                        flagImage = "flags/$code.png"
                        flagImageSource = lead.fileName
                        consecutiveFailures = 0
                        Thread.sleep(REQUEST_DELAY_MS)
                    } catch (e: Exception) {
                        problems += "$code: could not download ${lead.fileName}: ${e.message}"
                        if (keepPrevious) { flagImage = previous?.flagImage; flagImageSource = previous?.flagImageSource }
                        if (++consecutiveFailures >= CIRCUIT_BREAKER_THRESHOLD) {
                            logger.error("$CIRCUIT_BREAKER_THRESHOLD downloads failed in a row, assuming Wikimedia is blocking or rate limiting this runner. Stopping downloads.")
                            circuitOpen = true
                        }
                    }
                }
            }

            entries[code] = Entry(
                name = row.name,
                wikipediaUrl = country?.let { urlOf(it.title) },
                flagWikipediaUrl = flagPage?.let { urlOf(it.title) },
                flagImage = flagImage,
                flagImageSource = flagImageSource,
                flagNote = overrides.noOfficialFlag[code],
                symbolism = if (flagImage == null) null else previous?.symbolism,
            )
        }

        val target = dataFile.get().asFile.also { it.parentFile.mkdirs() }
        target.writeText(toJson(entries))

        val missingSymbolism = entries.filter { it.value.flagImage != null && it.value.symbolism.isNullOrBlank() }.keys
        val noFlag = entries.filterValues { it.flagImage == null }.keys
        logger.lifecycle("Wrote ${entries.size} countries to $target")
        logger.lifecycle("Images changed (${changed.size}): ${changed.joinToString()}")
        logger.lifecycle("Images already up to date: $unchanged")
        logger.lifecycle("Countries with flagImage null (${noFlag.size}): ${noFlag.joinToString()}")
        if (missingSymbolism.isNotEmpty()) logger.warn("Missing symbolism text for: ${missingSymbolism.joinToString()}")
        problems.forEach { logger.warn("Problem $it") }
        if (pruneFlags.get()) {
            if (wanted.isNotEmpty() || skipDownloads.get() || circuitOpen || problems.isNotEmpty()) {
                logger.warn("Not pruning flag images: the run was partial or had problems.")
            } else {
                val referenced = entries.values.mapNotNull { it.flagImage?.substringAfterLast('/') }.toSet()
                val stale = outDir.listFiles { file -> file.extension == "png" && file.name !in referenced }.orEmpty().sortedBy { it.name }
                stale.forEach { it.delete() }
                logger.lifecycle("Pruned ${stale.size} flag images no longer in the data: ${stale.joinToString { it.name }}")
            }
        }
        if (problems.isNotEmpty()) throw GradleException("${problems.size} problem(s) while building the country data, see the warnings above.")
    }

    // ── Overrides and existing data ──────────────────────────────────────────────

    private data class Overrides(
        val articles: Map<String, String>,
        val flagPages: Map<String, String>,
        val flagImageFiles: Map<String, String>,
        val noOfficialFlag: Map<String, String>,
    )

    private fun readOverrides(): Overrides {
        val json = JsonSlurper().parse(overridesFile.get().asFile) as Map<*, *>
        @Suppress("UNCHECKED_CAST")
        return Overrides(
            articles = (json["articles"] as? Map<String, String>).orEmpty(),
            flagPages = (json["flagPages"] as? Map<String, String>).orEmpty(),
            flagImageFiles = (json["flagImageFiles"] as? Map<String, String>).orEmpty(),
            noOfficialFlag = (json["noOfficialFlag"] as? Map<String, String>).orEmpty(),
        )
    }

    private fun readExisting(): Map<String, Entry> {
        val file = dataFile.get().asFile
        if (!file.exists()) return emptyMap()
        val json = JsonSlurper().parse(file) as Map<*, *>
        return json.entries.associate { (code, value) ->
            value as Map<*, *>
            code as String to Entry(
                name = value["name"] as? String ?: "",
                wikipediaUrl = value["wikipediaUrl"] as? String,
                flagWikipediaUrl = value["flagWikipediaUrl"] as? String,
                flagImage = value["flagImage"] as? String,
                flagImageSource = value["flagImageSource"] as? String,
                flagNote = value["flagNote"] as? String,
                symbolism = value["symbolism"] as? String,
            )
        }
    }

    // ── ISO 3166-1 table ─────────────────────────────────────────────────────────

    private fun fetchIsoRows(overrides: Overrides): List<IsoRow> {
        val json = api(
            "action" to "parse", "page" to "ISO 3166-1 alpha-2", "prop" to "wikitext",
            "format" to "json", "formatversion" to "2",
        )
        val wikitext = (json["parse"] as Map<*, *>)["wikitext"] as String
        val start = wikitext.indexOf("===Officially assigned code elements===")
        val end = wikitext.indexOf("===User-assigned code elements===")
        if (start < 0 || end <= start) throw GradleException("Could not find the officially assigned codes table on the ISO 3166-1 alpha-2 page.")

        // Each table row is a single line: | id="AD" | [[ISO 3166-2:AD|AD]] || [[Andorra]] || 1974 || [[.ad]] || notes
        val rowStart = Regex("""^\| id="([A-Z]{2})"\s*\|(.*)$""")
        val link = Regex("""\[\[([^\]|]+)(?:\|([^\]]*))?]]""")
        return wikitext.substring(start, end).lines().mapNotNull { line ->
            val match = rowStart.find(line) ?: return@mapNotNull null
            val nameCell = cleanWikitext(match.groupValues[2].split("||").getOrNull(1) ?: return@mapNotNull null)
            val first = link.find(nameCell) ?: throw GradleException("No article link in the ISO name cell for ${match.groupValues[1]}: $nameCell")
            val target = first.groupValues[1].trim()
            val display = first.groupValues[2].trim().ifEmpty { target }
            IsoRow(match.groupValues[1].lowercase(), display, overrides.articles[match.groupValues[1].lowercase()] ?: target)
        }.also { check(it.size in 200..300) { "Parsed ${it.size} ISO rows, which looks wrong" } }
    }

    private fun cleanWikitext(cell: String): String {
        var text = cell.replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), "")
        text = Regex("""\{\{sic\|([^|}]*)[^}]*}}""").replace(text) { it.groupValues[1] }
        text = Regex("""\{\{sort\|[^|{}]*\|(.*)}}""").replace(text) { it.groupValues[1] }
        text = Regex("""\{\{nowrap\|(.*)}}""").replace(text) { it.groupValues[1] }
        return text.trim()
    }

    // ── Wikipedia API ────────────────────────────────────────────────────────────

    /** Resolves article titles (following normalisation and redirects) and flags disambiguation pages; unknown titles map to nothing. */
    private fun resolvePages(titles: Collection<String>): Map<String, Page> {
        val result = mutableMapOf<String, Page>()
        for (batch in titles.distinct().chunked(API_BATCH_SIZE)) {
            val query = queryPages(batch, "pageprops", "ppprop" to "disambiguation")
            val pages = query.pages.associateBy { it["title"] as String }
            for (requested in batch) {
                val page = pages[query.resolve(requested)] ?: continue
                if (page["missing"] == true || page["invalid"] == true) continue
                result[requested] = Page(page["title"] as String, (page["pageprops"] as? Map<*, *>)?.containsKey("disambiguation") == true)
            }
            Thread.sleep(REQUEST_DELAY_MS)
        }
        return result
    }

    private fun fetchLeadImages(titlesByCode: Map<String, String>): Map<String, LeadImage> {
        val result = mutableMapOf<String, LeadImage>()
        for (batch in titlesByCode.entries.chunked(API_BATCH_SIZE)) {
            val query = queryPages(
                batch.map { it.value }.distinct(), "pageimages",
                "piprop" to "thumbnail|name", "pithumbsize" to thumbnailWidth.get().toString(),
            )
            val pages = query.pages.associateBy { it["title"] as String }
            for ((code, title) in batch) {
                val page = pages[query.resolve(title)] ?: continue
                val name = page["pageimage"] as? String ?: continue
                val thumb = (page["thumbnail"] as? Map<*, *>)?.get("source") as? String ?: continue
                result[code] = LeadImage(name, thumb)
            }
            Thread.sleep(REQUEST_DELAY_MS)
        }
        return result
    }

    /** Thumbnails of named Commons files, for flag articles that have no lead image. */
    private fun fetchCommonsFiles(fileNamesByCode: Map<String, String>): Map<String, LeadImage> {
        if (fileNamesByCode.isEmpty()) return emptyMap()
        val query = queryPages(
            fileNamesByCode.values.map { "File:$it" }, "imageinfo",
            "iiprop" to "url", "iiurlwidth" to thumbnailWidth.get().toString(),
        )
        val pages = query.pages.associateBy { it["title"] as String }
        return fileNamesByCode.mapNotNull { (code, name) ->
            val info = (pages[query.resolve("File:$name")]?.get("imageinfo") as? List<*>)?.firstOrNull() as? Map<*, *> ?: return@mapNotNull null
            code to LeadImage(name.replace(' ', '_'), info["thumburl"] as? String ?: return@mapNotNull null)
        }.toMap()
    }

    /** Prints a hint for countries whose flag article says the country has no flag of its own but which are not listed as such. */
    private fun printHints(rows: List<IsoRow>, flagPageByCode: Map<String, Page?>, overrides: Overrides) {
        val phrase = Regex(
            "no official flag|no flag of its own|has no flag|does not have (its own|an official|a separate)( \\w+){0,2} flag" +
                "|without (an )?official flag|no flag with official status",
            RegexOption.IGNORE_CASE,
        )
        val candidates = rows.filter { it.code !in overrides.noOfficialFlag }.mapNotNull { row ->
            flagPageByCode[row.code]?.let { row.code to it.title }
        }
        for (batch in candidates.chunked(EXTRACT_BATCH_SIZE)) {
            val query = queryPages(batch.map { it.second }.distinct(), "extracts", "exintro" to "1", "explaintext" to "1", "exlimit" to "max")
            val pages = query.pages.associateBy { it["title"] as String }
            for ((code, title) in batch) {
                val extract = pages[query.resolve(title)]?.get("extract") as? String ?: continue
                phrase.find(extract)?.let { logger.warn("Hint $code: \"$title\" says \"${it.value}\" but $code is not in noOfficialFlag") }
            }
            Thread.sleep(REQUEST_DELAY_MS)
        }
    }

    private class PagesQuery(val pages: List<Map<*, *>>, private val aliases: Map<String, String>) {
        fun resolve(title: String): String {
            var current = title
            repeat(3) { current = aliases[current] ?: current }
            return current
        }
    }

    private fun queryPages(titles: List<String>, prop: String, vararg extra: Pair<String, String>): PagesQuery {
        val json = api(
            "action" to "query", "prop" to prop, "titles" to titles.joinToString("|"), "redirects" to "1",
            "format" to "json", "formatversion" to "2", *extra,
        )
        val query = json["query"] as Map<*, *>
        val aliases = mutableMapOf<String, String>()
        for (key in listOf("normalized", "redirects")) {
            (query[key] as? List<*>).orEmpty().forEach {
                it as Map<*, *>
                aliases[it["from"] as String] = it["to"] as String
            }
        }
        return PagesQuery((query["pages"] as List<*>).map { it as Map<*, *> }, aliases)
    }

    private fun api(vararg params: Pair<String, String>): Map<*, *> {
        val query = params.joinToString("&") { (k, v) -> "$k=${URLEncoder.encode(v, Charsets.UTF_8)}" }
        return JsonSlurper().parseText(fetch("$API_URL?$query").toString(Charsets.UTF_8)) as Map<*, *>
    }

    private fun urlOf(title: String): String =
        "https://en.wikipedia.org/wiki/" + title.replace(' ', '_')
            .replace("%", "%25").replace("?", "%3F").replace("#", "%23").replace("&", "%26").replace("\"", "%22")

    // ── HTTP ─────────────────────────────────────────────────────────────────────

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

    // ── Output ───────────────────────────────────────────────────────────────────

    /** Fixed schema and key order, 2-space indent, non-ASCII left as is, like the repository's other data files. */
    private fun toJson(entries: Map<String, Entry>): String = entries.entries.joinToString(",\n", "{\n", "\n}\n") { (code, e) ->
        listOf(
            "name" to e.name,
            "wikipediaUrl" to e.wikipediaUrl,
            "flagWikipediaUrl" to e.flagWikipediaUrl,
            "flagImage" to e.flagImage,
            "flagImageSource" to e.flagImageSource,
            "flagNote" to e.flagNote,
            "symbolism" to e.symbolism,
        ).filterNot { (key, value) -> key == "flagNote" && value == null }.joinToString(",\n", "  ${quote(code)}: {\n", "\n  }") { (key, value) -> "    ${quote(key)}: ${value?.let(::quote) ?: "null"}" }
    }

    private fun quote(text: String): String = buildString {
        append('"')
        for (c in text) when {
            c == '"' -> append("\\\"")
            c == '\\' -> append("\\\\")
            c == '\n' -> append("\\n")
            c.code < 0x20 -> append("\\u%04x".format(c.code))
            else -> append(c)
        }
        append('"')
    }

    private companion object {
        const val API_URL = "https://en.wikipedia.org/w/api.php"
        const val API_BATCH_SIZE = 25
        const val EXTRACT_BATCH_SIZE = 20
        const val USER_AGENT = "Vexed/1.0 (https://github.com/lukeneedham/flagtutor; contact via GitHub)"
        const val REQUEST_DELAY_MS = 1_000L
        const val MAX_ATTEMPTS = 4
        const val RETRY_BASE_DELAY_S = 5L
        const val MAX_RETRY_DELAY_S = 60L
        const val CIRCUIT_BREAKER_THRESHOLD = 10
    }
}
