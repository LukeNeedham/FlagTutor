package com.flagtutor.app.ui.feature.pickcountrynamegame

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flagtutor.app.data.repository.CountryRepository
import com.flagtutor.app.data.repository.FlagImageRepository
import com.flagtutor.app.data.stats.FlagAttemptRepository
import com.flagtutor.app.domain.model.Country
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private const val OPTIONS_COUNT = 4

class PickCountryNameGameViewModel(
    private val countryRepository: CountryRepository,
    private val flagAttemptRepository: FlagAttemptRepository,
    private val flagImageRepository: FlagImageRepository,
) : ViewModel() {

    private var countries: List<Country> = emptyList()

    // Chosen one flag ahead so its images can load while the current flag is being guessed.
    private var upcoming: Country? = null
    private var isLoadingNext = false

    var uiState by mutableStateOf<PickCountryNameGameUiState>(PickCountryNameGameUiState.Loading)
        private set

    init {
        loadCountries()
    }

    fun loadCountries() {
        uiState = PickCountryNameGameUiState.Loading
        upcoming = null
        viewModelScope.launch {
            try {
                countries = countryRepository.getCountries()
                showNextFlag(previous = null)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                uiState = PickCountryNameGameUiState.Error
            }
        }
    }

    fun onNextFlag() {
        val state = uiState as? PickCountryNameGameUiState.Success ?: return
        if (!state.isAnswerRevealed || isLoadingNext) return
        isLoadingNext = true
        viewModelScope.launch {
            try {
                showNextFlag(previous = state.flag)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                uiState = PickCountryNameGameUiState.Error
            } finally {
                isLoadingNext = false
            }
        }
    }

    fun onOptionSelected(country: Country) {
        val state = uiState as? PickCountryNameGameUiState.Success ?: return
        if (state.isAnswerRevealed || country.alpha2Code in state.incorrectAlpha2Codes) return

        if (country.alpha2Code == state.flag.alpha2Code) {
            uiState = state.copy(isAnswerRevealed = true)
            val guessCount = state.incorrectAlpha2Codes.size + 1
            viewModelScope.launch {
                flagAttemptRepository.recordAttempt(
                    alpha2Code = state.flag.alpha2Code,
                    guessCount = guessCount,
                )
            }
        } else {
            uiState = state.copy(incorrectAlpha2Codes = state.incorrectAlpha2Codes + country.alpha2Code)
        }
    }

    private suspend fun showNextFlag(previous: Country?) {
        val next = upcoming ?: randomCountry(excluding = previous)
        val after = randomCountry(excluding = next)
        upcoming = after

        // Kick both off together so the following flag loads while this one is awaited.
        flagImageRepository.preload(next.alpha2Code)
        flagImageRepository.preload(after.alpha2Code)
        val assets = flagImageRepository.load(next.alpha2Code)

        uiState = PickCountryNameGameUiState.Success(
            flag = next,
            flagImage = assets.flag,
            mapImage = assets.map,
            colors = assets.colors,
            options = generateOptions(next),
            incorrectAlpha2Codes = emptySet(),
            isAnswerRevealed = false,
        )
    }

    private fun randomCountry(excluding: Country?): Country {
        var candidate = countries.random()
        while (countries.size > 1 && candidate.alpha2Code == excluding?.alpha2Code) {
            candidate = countries.random()
        }
        return candidate
    }

    private fun generateOptions(correct: Country): List<Country> {
        val distractors = countries
            .filter { it.alpha2Code != correct.alpha2Code }
            .shuffled()
            .take(OPTIONS_COUNT - 1)
        return (distractors + correct).shuffled()
    }
}
