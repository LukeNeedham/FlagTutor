package com.flagtutor.app.domain.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.float
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

object CountryBoundaries {

    data class LatLng(val lon: Float, val lat: Float)

    @Volatile
    private var parsed: Map<String, List<List<LatLng>>>? = null

    val boundaries: Map<String, List<List<LatLng>>>
        get() = parsed ?: emptyMap()

    val isLoaded: Boolean get() = parsed != null

    fun load(jsonBytes: ByteArray) {
        if (parsed != null) return
        val root = Json.parseToJsonElement(String(jsonBytes)) as JsonObject
        val map = mutableMapOf<String, List<List<LatLng>>>()
        for ((code, polygonsElement) in root) {
            val polygons = (polygonsElement as JsonArray).map { polyElement ->
                (polyElement as JsonArray).map { pointElement ->
                    val arr = pointElement.jsonArray
                    LatLng(arr[0].jsonPrimitive.float, arr[1].jsonPrimitive.float)
                }
            }
            map[code] = polygons
        }
        parsed = map
    }
}
