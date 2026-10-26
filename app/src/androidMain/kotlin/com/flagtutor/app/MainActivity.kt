package com.flagtutor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.flagtutor.app.di.androidModule
import com.flagtutor.app.ui.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App(extraModules = listOf(androidModule(applicationContext))) {
                SystemBarsAppearance()
                AppNavigation()
            }
        }
    }
}
