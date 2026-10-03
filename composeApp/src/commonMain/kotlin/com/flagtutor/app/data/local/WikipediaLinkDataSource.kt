package com.flagtutor.app.data.local

import flagtutor.composeapp.generated.resources.Res
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.resources.ExperimentalResourceApi

class WikipediaLinkDataSource {

    private val links = LoadOnce {
        val bytes = readFile("files/wikipedia_links.json")
        val json = Json.decodeFromString<JsonObject>(bytes.decodeToString())
        json.mapValues { it.value.jsonPrimitive.content }
    }

    suspend fun getLinks(): Map<String, String> = links.get()

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun readFile(path: String) = Res.readBytes(path)
}
