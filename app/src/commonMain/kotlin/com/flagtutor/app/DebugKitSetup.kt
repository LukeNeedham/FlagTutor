package com.flagtutor.app

import com.lukeneedham.common.DebugKit
import com.lukeneedham.common.DebugKitConfig
import com.lukeneedham.common.logger.Logger

/**
 * Starts the shared library's logger and crash handler. Call it once, as early as possible, from
 * each platform's entry point, before anything can log or crash.
 */
fun initDebugKit() {
    DebugKit.init(DebugKitConfig(enabled = isDebugBuild))
    Logger.i("App started")
}
