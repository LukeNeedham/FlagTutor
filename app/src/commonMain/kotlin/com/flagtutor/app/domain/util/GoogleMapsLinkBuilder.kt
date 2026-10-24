package com.flagtutor.app.domain.util

object GoogleMapsLinkBuilder {

    private const val UNRESERVED_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.~"

    fun searchUrl(query: String): String =
        "https://www.google.com/maps/search/?api=1&query=${encodeUrlQueryComponent(query)}"

    private fun encodeUrlQueryComponent(value: String): String {
        val builder = StringBuilder()
        for (byte in value.encodeToByteArray()) {
            val code = byte.toInt() and 0xFF
            val char = code.toChar()
            if (code < 128 && UNRESERVED_CHARS.contains(char)) {
                builder.append(char)
            } else {
                builder.append('%')
                builder.append(code.toString(16).uppercase().padStart(2, '0'))
            }
        }
        return builder.toString()
    }
}
