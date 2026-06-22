package com.flagtutor.app.domain.model

object CountryBoundaries {

    data class LatLng(val lon: Float, val lat: Float)

    @Volatile
    private var parsed: Map<String, List<List<LatLng>>>? = null

    val boundaries: Map<String, List<List<LatLng>>>
        get() = parsed ?: emptyMap()

    val isLoaded: Boolean get() = parsed != null

    fun load(jsonBytes: ByteArray) {
        if (parsed != null) return
        parsed = parse(String(jsonBytes, Charsets.UTF_8))
    }

    private fun parse(json: String): Map<String, List<List<LatLng>>> {
        val map = HashMap<String, List<List<LatLng>>>(300)
        var i = json.indexOf('{') + 1
        val len = json.length

        while (i < len) {
            // Find next key
            val keyStart = json.indexOf('"', i)
            if (keyStart < 0) break
            val keyEnd = json.indexOf('"', keyStart + 1)
            val code = json.substring(keyStart + 1, keyEnd)
            i = json.indexOf('[', keyEnd) + 1 // outer polygon array

            val polygons = ArrayList<List<LatLng>>()
            while (i < len && json[i] != ']') {
                if (json[i] == '[') {
                    i++ // start of polygon ring array
                    val ring = ArrayList<LatLng>()
                    while (i < len && json[i] != ']') {
                        if (json[i] == '[') {
                            i++ // start of coordinate pair
                            val lonEnd = json.indexOf(',', i)
                            val lon = json.substring(i, lonEnd).toFloat()
                            i = lonEnd + 1
                            val latEnd = findNumberEnd(json, i)
                            val lat = json.substring(i, latEnd).toFloat()
                            i = json.indexOf(']', latEnd) + 1 // past closing ]
                            ring.add(LatLng(lon, lat))
                        } else {
                            i++
                        }
                    }
                    if (i < len) i++ // past polygon ring ]
                    polygons.add(ring)
                } else {
                    i++
                }
            }
            if (i < len) i++ // past outer polygon array ]
            map[code] = polygons
        }
        return map
    }

    private fun findNumberEnd(s: String, start: Int): Int {
        var i = start
        val len = s.length
        while (i < len) {
            val c = s[i]
            if (c == ']' || c == ',') return i
            i++
        }
        return i
    }
}
