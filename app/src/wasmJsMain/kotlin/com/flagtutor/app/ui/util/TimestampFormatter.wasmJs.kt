package com.flagtutor.app.ui.util

// Always Amsterdam time, whatever the browser's own time zone is. Formatted as yyyy-MM-dd HH:mm:ss.
@JsFun(
    """(ms) => {
        const parts = Object.fromEntries(
            new Intl.DateTimeFormat('en-GB', {
                timeZone: 'Europe/Amsterdam',
                year: 'numeric', month: '2-digit', day: '2-digit',
                hour: '2-digit', minute: '2-digit', second: '2-digit',
                hourCycle: 'h23',
            }).formatToParts(new Date(ms)).map((p) => [p.type, p.value])
        );
        return parts.year + '-' + parts.month + '-' + parts.day + ' ' +
            parts.hour + ':' + parts.minute + ':' + parts.second;
    }"""
)
private external fun jsFormat(epochMillis: Double): String

actual fun formatTimestamp(epochMillis: Long): String = jsFormat(epochMillis.toDouble())
