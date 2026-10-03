import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

/**
 * Finds countries whose flag images are pixel-identical and writes the groups to
 * `files/identical_flags.json` in [outputDir], which ships with the app.
 *
 * Format: `[["<alpha2>", "<alpha2>", ...], ...]`, only groups with two or more countries.
 */
@CacheableTask
abstract class GenerateIdenticalFlagsTask : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val flagsDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val groups = flagsDir.get().asFile
            .listFiles { file -> file.extension == "png" }
            .orEmpty()
            .sortedBy { it.name }
            .groupBy { file ->
                val image = checkNotNull(ImageIO.read(file)) { "Could not decode ${file.name}" }
                pixelKey(image)
            }
            .values
            .map { files -> files.map { it.nameWithoutExtension } }
            .filter { it.size > 1 }
            .sortedBy { it.first() }

        val target = outputDir.get().asFile.resolve("files/identical_flags.json")
        target.parentFile.mkdirs()
        target.writeText(
            groups.joinToString(",\n", prefix = "[\n", postfix = "\n]\n") { group ->
                "  [" + group.joinToString(",") { "\"$it\"" } + "]"
            },
        )
    }

    private fun pixelKey(image: BufferedImage): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        digest.update("${image.width}x${image.height}".toByteArray())
        val row = IntArray(image.width)
        for (y in 0 until image.height) {
            image.getRGB(0, y, image.width, 1, row, 0, image.width)
            for (pixel in row) {
                digest.update(
                    byteArrayOf(
                        (pixel shr 24).toByte(), (pixel shr 16).toByte(),
                        (pixel shr 8).toByte(), pixel.toByte(),
                    ),
                )
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
