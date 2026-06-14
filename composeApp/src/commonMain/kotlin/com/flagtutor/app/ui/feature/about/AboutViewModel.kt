package com.flagtutor.app.ui.feature.about

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class AboutViewModel : ViewModel() {

    var message by mutableStateOf("FlagTutor helps you learn the flags of the world.")
        private set
}
