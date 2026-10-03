package com.flagtutor.app.data.local

import androidx.compose.ui.graphics.Color
import com.flagtutor.app.ui.util.ExtractedColor
import flagtutor.composeapp.generated.resources.Res
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * Reads each flag's dominant colors from `files/flag_colors.json`, which is generated at build
 * time by the `generateFlagColors` Gradle task.
 */
class FlagColorDataSource {

    private val colors = LoadOnce {
        val bytes = readFile("files/flag_colors.json")
        val raw = Json.decodeFromString<Map<String, List<List<String>>>>(bytes.decodeToString())
        raw.mapValues { (_, pairs) ->
            pairs.map { (container, content) ->
                ExtractedColor(containerColor = parseColor(container), contentColor = parseColor(content))
            }
        }
    }

    suspend fun getColors(alpha2Code: String): List<ExtractedColor> =
        colors.get()[alpha2Code.lowercase()].orEmpty()

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun readFile(path: String) = Res.readBytes(path)

    private fun parseColor(hex: String) = Color(0xFF000000L or hex.removePrefix("#").toLong(16))
}
