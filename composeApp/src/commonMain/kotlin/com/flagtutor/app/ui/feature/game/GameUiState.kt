package com.flagtutor.app.ui.feature.game

import com.flagtutor.app.domain.model.Country

sealed interface GameUiState {

    data object Loading : GameUiState

    data object Error : GameUiState

    data class Success(
        val flag: Country,
        val options: List<Country>,
        val incorrectAlpha2Codes: Set<String>,
        val isAnswerRevealed: Boolean,
    ) : GameUiState
}
