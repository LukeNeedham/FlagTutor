package com.flagtutor.app.ui.feature.flagattempts

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flagtutor.app.data.stats.FlagAttemptRepository
import com.flagtutor.app.domain.model.FlagAttempt
import kotlinx.coroutines.launch

class FlagAttemptsViewModel(
    private val flagAttemptRepository: FlagAttemptRepository,
) : ViewModel() {

    var attempts by mutableStateOf<List<FlagAttempt>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    init {
        loadAttempts()
    }

    fun loadAttempts() {
        isLoading = true
        viewModelScope.launch {
            attempts = flagAttemptRepository.getAttempts().sortedByDescending { it.timestamp }
            isLoading = false
        }
    }
}
