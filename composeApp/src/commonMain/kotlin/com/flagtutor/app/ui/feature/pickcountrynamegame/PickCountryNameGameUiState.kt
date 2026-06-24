package com.flagtutor.app.ui.feature.pickcountrynamegame

import com.flagtutor.app.domain.model.Country

sealed interface PickCountryNameGameUiState {

    data object Loading : PickCountryNameGameUiState

    data object Error : PickCountryNameGameUiState

    data class Success(
        val flag: Country,
        val options: List<Country>,
        val incorrectAlpha2Codes: Set<String>,
        val isAnswerRevealed: Boolean,
    ) : PickCountryNameGameUiState
}
