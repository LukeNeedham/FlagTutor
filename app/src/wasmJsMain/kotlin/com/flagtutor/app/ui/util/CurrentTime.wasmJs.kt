package com.flagtutor.app.ui.util

@JsFun("() => Date.now()")
private external fun jsNow(): Double

actual fun currentTimeMillis(): Long = jsNow().toLong()
