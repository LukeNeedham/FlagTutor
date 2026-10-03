import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import javax.imageio.ImageIO

private const val COLORS_PER_FLAG = 4

/**
 * Extracts the dominant colors of every flag image and writes them to `files/flag_colors.json`
 * in [outputDir], which is registered as a compose resources directory so it ships with the app.
 *
 * Format: `{ "<alpha2>": [[container, content], ...] }` with colors as `"#RRGGBB"` strings.
 */
@CacheableTask
abstract class GenerateFlagColorsTask : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val flagsDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val entries = flagsDir.get().asFile
            .listFiles { file -> file.extension == "png" }
            .orEmpty()
            .sortedBy { it.name }
            .map { file ->
                val image = checkNotNull(ImageIO.read(file)) { "Could not decode ${file.name}" }
                val colors = extractColorsFromImage(image, COLORS_PER_FLAG).joinToString(",") {
                    "[\"${it.container.toHex()}\",\"${it.content.toHex()}\"]"
                }
                "  \"${file.nameWithoutExtension}\": [$colors]"
            }

        val target = outputDir.get().asFile.resolve("files/flag_colors.json")
        target.parentFile.mkdirs()
        target.writeText(entries.joinToString(",\n", prefix = "{\n", postfix = "\n}\n"))
    }

    private fun Int.toHex() = "#" + (this and 0xFFFFFF).toString(16).padStart(6, '0')
}
