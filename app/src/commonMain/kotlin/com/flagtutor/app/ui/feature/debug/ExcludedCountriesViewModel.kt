package com.flagtutor.app.ui.feature.debug

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flagtutor.app.data.repository.CountryRepository
import com.flagtutor.app.domain.model.Country
import com.lukeneedham.common.logger.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** A country the app leaves out, and why. */
data class ExcludedCountry(val country: Country, val reason: String)

class ExcludedCountriesViewModel(
    private val countryRepository: CountryRepository,
) : ViewModel() {

    var countries by mutableStateOf<List<ExcludedCountry>>(emptyList())
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
                val namesByCode = countryRepository.getGameCountries().associate { it.alpha2Code to it.name }
                countries = countryRepository.getExcludedCountries().map { country ->
                    val parent = country.flagSharedWith
                    ExcludedCountry(
                        country = country,
                        reason = when {
                            parent != null -> "Uses the flag of ${namesByCode[parent] ?: parent.uppercase()}, so only that country is in the game."
                            else -> country.flagNote.orEmpty()
                        },
                    )
                }
                isLoading = false
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Logger.e("Could not load the excluded countries", e)
                isLoading = false
                isError = true
            }
        }
    }
}
