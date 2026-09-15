package com.flagtutor.app.ui.feature.crashes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.flagtutor.app.data.crash.CrashEntry
import com.flagtutor.app.data.crash.CrashRepository

class CrashesViewModel(private val crashRepository: CrashRepository) : ViewModel() {

    var crashes by mutableStateOf<List<CrashEntry>>(emptyList())
        private set

    init {
        crashes = crashRepository.getCrashes()
    }

    fun clearCrashes() {
        crashRepository.clearCrashes()
        crashes = emptyList()
    }
}
