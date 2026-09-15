package com.flagtutor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.flagtutor.app.data.crash.CrashRepositoryImpl
import com.flagtutor.app.di.androidModule
import com.flagtutor.app.ui.navigation.NavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val crashRepository = CrashRepositoryImpl(applicationContext)
        crashRepository.installUncaughtExceptionHandler()
        setContent {
            App(extraModules = listOf(androidModule(applicationContext))) {
                NavGraph()
            }
        }
    }
}
