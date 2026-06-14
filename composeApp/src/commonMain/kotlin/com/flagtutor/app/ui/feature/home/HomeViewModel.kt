package com.flagtutor.app.ui.feature.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    var title by mutableStateOf("FlagTutor")
        private set

    var subtitle by mutableStateOf("Learn the flags of the world")
        private set
}
