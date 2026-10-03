package com.flagtutor.app

// Set by config.js, which the deploy workflows generate: true for PR previews, false in production.
@JsFun("() => globalThis.vexedDebug === true")
private external fun readDebugFlag(): Boolean

actual val isDebugBuild: Boolean = readDebugFlag()
