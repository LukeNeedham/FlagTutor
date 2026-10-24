package com.flagtutor.app

import android.app.Application

class FlagTutorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initDebugKit()
    }
}
