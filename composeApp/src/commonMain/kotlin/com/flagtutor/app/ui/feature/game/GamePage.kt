package com.flagtutor.app.ui.feature.game

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GamePage(
    viewModel: GameViewModel = koinViewModel(),
) {
    GamePageContent(
        flag = viewModel.flag,
        options = viewModel.options,
        incorrectAlpha2Codes = viewModel.incorrectAlpha2Codes,
        isAnswerRevealed = viewModel.isAnswerRevealed,
        onOptionSelected = viewModel::onOptionSelected,
    )
}
