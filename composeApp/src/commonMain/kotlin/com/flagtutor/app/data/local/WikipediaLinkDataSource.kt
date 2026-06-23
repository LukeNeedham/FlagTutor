package com.flagtutor.app.data.local

import flagtutor.composeapp.generated.resources.Res
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.resources.ExperimentalResourceApi

class WikipediaLinkDataSource {

    private var cache: Map<String, String>? = null

    @OptIn(ExperimentalResourceApi::class)
    suspend fun getLinks(): Map<String, String> {
        cache?.let { return it }
        val bytes = Res.readBytes("files/wikipedia_links.json")
        val json = Json.decodeFromString<JsonObject>(bytes.decodeToString())
        val links = json.mapValues { it.value.jsonPrimitive.content }
        cache = links
        return links
    }
}

