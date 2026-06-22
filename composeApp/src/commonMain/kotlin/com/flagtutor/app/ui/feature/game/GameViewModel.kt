package com.flagtutor.app.ui.feature.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flagtutor.app.data.image.FlagImagePrefetcher
import com.flagtutor.app.data.repository.CountryRepository
import com.flagtutor.app.domain.model.Country
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private const val OPTIONS_COUNT = 4

class GameViewModel(
    private val countryRepository: CountryRepository,
    private val flagImagePrefetcher: FlagImagePrefetcher,
) : ViewModel() {

    private var countries: List<Country> = emptyList()

    var uiState by mutableStateOf<GameUiState>(GameUiState.Loading)
        private set

    init {
        loadCountries()
    }

    fun loadCountries() {
        uiState = GameUiState.Loading
        viewModelScope.launch {
            try {
                val result = countryRepository.getCountries()
                countries = result.countries
                showRandomFlag()
                flagImagePrefetcher.prefetch(result.countries)

                if (result.isFromCache) {
                    countryRepository.refreshInBackground()?.let { refreshed ->
                        countries = refreshed
                        flagImagePrefetcher.prefetch(refreshed)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                uiState = GameUiState.Error
            }
        }
    }

    fun onNextFlag() {
        val state = uiState as? GameUiState.Success ?: return
        if (!state.isAnswerRevealed) return
        showRandomFlag(previous = state.flag)
    }

    fun onOptionSelected(country: Country) {
        val state = uiState as? GameUiState.Success ?: return
        if (state.isAnswerRevealed || country.alpha2Code in state.incorrectAlpha2Codes) return

        if (country.alpha2Code == state.flag.alpha2Code) {
            uiState = state.copy(isAnswerRevealed = true)
        } else {
            uiState = state.copy(incorrectAlpha2Codes = state.incorrectAlpha2Codes + country.alpha2Code)
        }
    }

    private fun showRandomFlag(previous: Country? = null) {
        var next = countries.random()
        while (next.alpha2Code == previous?.alpha2Code) {
            next = countries.random()
        }
        uiState = GameUiState.Success(
            flag = next,
            options = generateOptions(next),
            incorrectAlpha2Codes = emptySet(),
            isAnswerRevealed = false,
        )
    }

    private fun generateOptions(correct: Country): List<Country> {
        val distractors = countries
            .filter { it.alpha2Code != correct.alpha2Code }
            .shuffled()
            .take(OPTIONS_COUNT - 1)
        return (distractors + correct).shuffled()
    }
}
