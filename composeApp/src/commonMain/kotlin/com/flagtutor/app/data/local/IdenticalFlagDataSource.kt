package com.flagtutor.app.data.local

import flagtutor.composeapp.generated.resources.Res
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * Reads groups of countries with identical flags from `files/identical_flags.json`, which is
 * generated at build time by the `generateIdenticalFlags` Gradle task.
 */
class IdenticalFlagDataSource {

    private var cached: Map<String, Set<String>>? = null

    /** Maps an alpha-2 code to the other codes that share its flag. Codes without twins are absent. */
    @OptIn(ExperimentalResourceApi::class)
    suspend fun getIdenticalFlags(): Map<String, Set<String>> {
        cached?.let { return it }
        val bytes = Res.readBytes("files/identical_flags.json")
        val groups = Json.decodeFromString<List<List<String>>>(bytes.decodeToString())
        val map = groups.flatMap { group ->
            group.map { code -> code.lowercase() to (group.map { it.lowercase() }.toSet() - code.lowercase()) }
        }.toMap()
        cached = map
        return map
    }
}
