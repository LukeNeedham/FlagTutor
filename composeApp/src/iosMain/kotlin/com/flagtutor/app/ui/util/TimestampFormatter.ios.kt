package com.flagtutor.app.ui.util

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter

actual fun formatTimestamp(epochMillis: Long): String {
    val date = NSDate.dateWithTimeIntervalSince1970(epochMillis / 1000.0)
    val formatter = NSDateFormatter().apply { dateFormat = "yyyy-MM-dd HH:mm:ss" }
    return formatter.stringFromDate(date)
}
