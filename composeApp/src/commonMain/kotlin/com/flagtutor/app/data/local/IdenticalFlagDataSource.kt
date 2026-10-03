package com.flagtutor.app.data.local

import flagtutor.composeapp.generated.resources.Res
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * Reads groups of countries with identical flags from `files/identical_flags.json`, which is
 * generated at build time by the `generateIdenticalFlags` Gradle task.
 */
class IdenticalFlagDataSource {

    private val identicalFlags = LoadOnce {
        val bytes = readFile("files/identical_flags.json")
        val groups = Json.decodeFromString<List<List<String>>>(bytes.decodeToString())
        groups.flatMap { group ->
            group.map { code -> code.lowercase() to (group.map { it.lowercase() }.toSet() - code.lowercase()) }
        }.toMap()
    }

    /** Maps an alpha-2 code to the other codes that share its flag. Codes without twins are absent. */
    suspend fun getIdenticalFlags(): Map<String, Set<String>> = identicalFlags.get()

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun readFile(path: String) = Res.readBytes(path)
}
