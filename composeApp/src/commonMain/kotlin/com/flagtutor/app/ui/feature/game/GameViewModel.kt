package com.flagtutor.app.ui.feature.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flagtutor.app.domain.model.Countries
import com.flagtutor.app.domain.model.Country
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val OPTIONS_COUNT = 4
private const val NEXT_FLAG_DELAY_MS = 700L

class GameViewModel : ViewModel() {

    private val countries: List<Country> = Countries.all

    var flag by mutableStateOf(countries.random())
        private set

    var options by mutableStateOf(generateOptions(flag))
        private set

    var incorrectAlpha2Codes by mutableStateOf(emptySet<String>())
        private set

    var isAnswerRevealed by mutableStateOf(false)
        private set

    fun onOptionSelected(country: Country) {
        if (isAnswerRevealed || country.alpha2Code in incorrectAlpha2Codes) return

        if (country.alpha2Code == flag.alpha2Code) {
            isAnswerRevealed = true
            viewModelScope.launch {
                delay(NEXT_FLAG_DELAY_MS)
                nextFlag()
            }
        } else {
            incorrectAlpha2Codes = incorrectAlpha2Codes + country.alpha2Code
        }
    }

    private fun nextFlag() {
        val previousAlpha2Code = flag.alpha2Code
        var next = countries.random()
        while (next.alpha2Code == previousAlpha2Code) {
            next = countries.random()
        }
        flag = next
        options = generateOptions(next)
        incorrectAlpha2Codes = emptySet()
        isAnswerRevealed = false
    }

    private fun generateOptions(correct: Country): List<Country> {
        val distractors = countries
            .filter { it.alpha2Code != correct.alpha2Code }
            .shuffled()
            .take(OPTIONS_COUNT - 1)
        return (distractors + correct).shuffled()
    }
}
