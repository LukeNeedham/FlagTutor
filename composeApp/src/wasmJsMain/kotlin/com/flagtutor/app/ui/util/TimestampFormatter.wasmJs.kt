package com.flagtutor.app.ui.util

// Formats in the browser's local time zone, matching the other platforms.
@JsFun(
    """(ms) => {
        const d = new Date(ms);
        const p = (n) => String(n).padStart(2, '0');
        return d.getFullYear() + '-' + p(d.getMonth() + 1) + '-' + p(d.getDate()) + ' ' +
            p(d.getHours()) + ':' + p(d.getMinutes()) + ':' + p(d.getSeconds());
    }"""
)
private external fun jsFormat(epochMillis: Double): String

actual fun formatTimestamp(epochMillis: Long): String = jsFormat(epochMillis.toDouble())
