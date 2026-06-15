package com.flagtutor.app.ui.feature.game

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.flagtutor.app.domain.model.Country
import com.flagtutor.app.ui.component.FlagImage
import com.flagtutor.app.ui.feature.game.component.FlagOptionButton

@Composable
fun GamePageContent(
    uiState: GameUiState,
    onOptionSelected: (Country) -> Unit,
    onRetry: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is GameUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is GameUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Couldn't load flags. Check your connection and try again.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            }

            is GameUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Which country's flag is this?",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    AnimatedContent(
                        targetState = uiState,
                        contentKey = { it.flag.alpha2Code },
                        transitionSpec = {
                            (
                                fadeIn(animationSpec = tween(durationMillis = 300, delayMillis = 90)) +
                                    slideInHorizontally(
                                        animationSpec = tween(durationMillis = 300, delayMillis = 90),
                                    ) { width -> width / 3 }
                                ).togetherWith(
                                fadeOut(animationSpec = tween(durationMillis = 90)) +
                                    slideOutHorizontally(
                                        animationSpec = tween(durationMillis = 90),
                                    ) { width -> -width / 3 },
                            )
                        },
                        label = "flag-transition",
                        modifier = Modifier.fillMaxWidth(),
                    ) { state ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            FlagImage(
                                flagUrl = state.flag.flagUrl,
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .aspectRatio(3f / 2f),
                                contentScale = ContentScale.Fit,
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            state.options.forEach { country ->
                                // Keyed so each option gets a fresh crumble animation state when the flag changes.
                                key(country.alpha2Code) {
                                    FlagOptionButton(
                                        country = country,
                                        isCorrectAnswer = state.isAnswerRevealed && country.alpha2Code == state.flag.alpha2Code,
                                        isCrumbled = country.alpha2Code in state.incorrectAlpha2Codes,
                                        enabled = !state.isAnswerRevealed && country.alpha2Code !in state.incorrectAlpha2Codes,
                                        onClick = { onOptionSelected(country) },
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
