package com.flagtutor.app.data.local

import flagtutor.composeapp.generated.resources.Res
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.resources.ExperimentalResourceApi

class FlagDescriptionDataSource {

    private val descriptions = LoadOnce {
        val bytes = readFile("files/flag_descriptions.json")
        val json = Json.decodeFromString<JsonObject>(bytes.decodeToString())
        json.mapValues { it.value.jsonPrimitive.content }
    }

    suspend fun getDescriptions(): Map<String, String> = descriptions.get()

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun readFile(path: String) = Res.readBytes(path)
}
