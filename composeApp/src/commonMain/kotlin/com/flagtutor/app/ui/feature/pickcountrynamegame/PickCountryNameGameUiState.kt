package com.flagtutor.app.ui.feature.pickcountrynamegame

import androidx.compose.ui.graphics.ImageBitmap
import com.flagtutor.app.domain.model.Country
import com.flagtutor.app.ui.util.ExtractedColor

sealed interface PickCountryNameGameUiState {

    data object Loading : PickCountryNameGameUiState

    data object Error : PickCountryNameGameUiState

    data class Success(
        val flag: Country,
        val flagImage: ImageBitmap,
        /** Null when there is no map image for [flag]. */
        val mapImage: ImageBitmap?,
        val colors: List<ExtractedColor>,
        val options: List<Country>,
        val incorrectAlpha2Codes: Set<String>,
        val isAnswerRevealed: Boolean,
    ) : PickCountryNameGameUiState
}
