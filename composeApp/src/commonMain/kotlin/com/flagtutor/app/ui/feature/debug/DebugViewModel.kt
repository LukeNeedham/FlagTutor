package com.flagtutor.app.ui.feature.debug

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flagtutor.app.data.repository.CountryRepository
import com.flagtutor.app.domain.model.Country
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class DebugViewModel(
    private val countryRepository: CountryRepository,
) : ViewModel() {

    var countries by mutableStateOf<List<Country>>(emptyList())
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
                val result = countryRepository.getCountries()
                countries = result.countries
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
