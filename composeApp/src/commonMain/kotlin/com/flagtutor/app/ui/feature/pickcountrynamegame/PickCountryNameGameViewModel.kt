package com.flagtutor.app.ui.feature.pickcountrynamegame

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flagtutor.app.data.local.IdenticalFlagDataSource
import com.flagtutor.app.data.repository.CountryRepository
import com.flagtutor.app.data.repository.FlagImageRepository
import com.flagtutor.app.data.repository.GamePreloader
import com.flagtutor.app.data.stats.FlagAttemptRepository
import com.flagtutor.app.domain.model.Country
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private const val OPTIONS_COUNT = 4

class PickCountryNameGameViewModel(
    private val countryRepository: CountryRepository,
    private val flagAttemptRepository: FlagAttemptRepository,
    private val flagImageRepository: FlagImageRepository,
    private val gamePreloader: GamePreloader,
    private val identicalFlagDataSource: IdenticalFlagDataSource,
    // Debug only: the first flag shown is this country, instead of a random one.
    private val forcedAlpha2Code: String? = null,
) : ViewModel() {

    private var countries: List<Country> = emptyList()
    private var identicalFlags: Map<String, Set<String>> = emptyMap()

    // Chosen one flag ahead so its images can load while the current flag is being guessed.
    private var upcoming: Country? = null
    private var queuedAfter: Country? = null
    private var isLoadingNext = false

    var uiState by mutableStateOf<PickCountryNameGameUiState>(PickCountryNameGameUiState.Loading)
        private set

    init {
        loadCountries()
    }

    override fun onCleared() {
        // The user has left the game: get the next game's first two flags ready straight away.
        gamePreloader.prepareNext()
    }

    fun loadCountries() {
        uiState = PickCountryNameGameUiState.Loading
        upcoming = null
        viewModelScope.launch {
            try {
                countries = countryRepository.getCountriesWithFlags()
                identicalFlags = identicalFlagDataSource.getIdenticalFlags()
                // Use the countries whose images were loaded at app start, if available.
                val forced = forcedAlpha2Code?.let { code -> countries.firstOrNull { it.alpha2Code == code } }
                if (forced != null) {
                    upcoming = forced
                    queuedAfter = null
                } else {
                    gamePreloader.takeInitialCountries()?.let { (first, second) ->
                        upcoming = first
                        queuedAfter = second
                    }
                }
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
        val after = queuedAfter ?: randomCountry(excluding = next)
        queuedAfter = null
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
        // Countries sharing a flag would make several options equally correct, so each picked
        // option also rules out every country with the same flag.
        val excluded = mutableSetOf(correct.alpha2Code)
        excluded += identicalFlags[correct.alpha2Code].orEmpty()
        val distractors = mutableListOf<Country>()
        for (candidate in countries.shuffled()) {
            if (distractors.size == OPTIONS_COUNT - 1) break
            if (candidate.alpha2Code in excluded) continue
            distractors += candidate
            excluded += candidate.alpha2Code
            excluded += identicalFlags[candidate.alpha2Code].orEmpty()
        }
        return (distractors + correct).shuffled()
    }
}
