package com.flagtutor.app.ui.feature.debug

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flagtutor.app.data.repository.CountryRepository
import com.flagtutor.app.data.stats.FlagAttemptRepository
import com.flagtutor.app.domain.model.Country
import com.flagtutor.app.domain.model.FlagAttempt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class CountriesOverviewViewModel(
    private val countryRepository: CountryRepository,
    private val flagAttemptRepository: FlagAttemptRepository,
) : ViewModel() {

    var countries by mutableStateOf<List<Country>>(emptyList())
        private set

    var attemptStatsByCountry by mutableStateOf<Map<String, FlagAttemptStats>>(emptyMap())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var isError by mutableStateOf(false)
        private set

    init {
        loadCountries()
    }

    fun loadCountries() {
        isLoading = true
        isError = false
        viewModelScope.launch {
            try {
                countries = countryRepository.getCountries()
                attemptStatsByCountry = flagAttemptRepository.getAttempts()
                    .groupBy { it.alpha2Code }
                    .mapValues { (_, attempts) -> attempts.toStats() }
                isLoading = false
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                isLoading = false
                isError = true
            }
        }
    }
}

/** Assumes [this] is ordered oldest-first, matching [FlagAttemptRepository.getAttempts]. */
private fun List<FlagAttempt>.toStats(): FlagAttemptStats {
    val totalIncorrectAnswers = sumOf { it.guessCount - 1 }
    return FlagAttemptStats(
        totalAttempts = size,
        totalIncorrectAnswers = totalIncorrectAnswers,
        averageIncorrectPerAttempt = totalIncorrectAnswers.toDouble() / size,
        currentStreak = asReversed().takeWhile { it.guessCount == 1 }.size,
    )
}
