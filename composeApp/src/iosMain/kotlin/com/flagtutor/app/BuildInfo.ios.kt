package com.flagtutor.app

actual val isDebugBuild: Boolean = kotlin.native.Platform.isDebugBinary
