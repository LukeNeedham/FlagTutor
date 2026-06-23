package com.flagtutor.app.ui.feature.game

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.flagtutor.app.domain.model.Country
import com.flagtutor.app.ui.component.CountryMapHighlight
import com.flagtutor.app.ui.component.FlagImage
import com.flagtutor.app.ui.feature.game.component.FlagOptionButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamePageContent(
    uiState: GameUiState,
    onOptionSelected: (Country) -> Unit,
    onNextFlag: () -> Unit,
    onMoreInfo: (String) -> Unit,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Guess the Flag") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (uiState) {
                is GameUiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading flags…",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
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
                        Icon(
                            imageVector = Icons.Filled.WifiOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.height(48.dp),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Couldn't load flags. Check your connection and try again.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onRetry, shape = MaterialTheme.shapes.large) {
                            Icon(imageVector = Icons.Filled.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }

                is GameUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = if (uiState.isAnswerRevealed) uiState.flag.name else "Which country's flag is this?",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
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
                                        .fillMaxWidth(0.85f)
                                        .aspectRatio(3f / 2f),
                                    contentScale = ContentScale.Fit,
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                AnimatedVisibility(
                                    visible = state.isAnswerRevealed,
                                    enter = fadeIn(tween(400)) + expandVertically(tween(400)),
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Card(
                                            shape = MaterialTheme.shapes.extraLarge,
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            modifier = Modifier.fillMaxWidth(0.85f),
                                        ) {
                                            CountryMapHighlight(
                                                alpha2Code = state.flag.alpha2Code,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .aspectRatio(16f / 10f)
                                                    .padding(12.dp),
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        ) {
                                            Button(
                                                onClick = onNextFlag,
                                                shape = MaterialTheme.shapes.large,
                                            ) {
                                                Text("Next")
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                    contentDescription = null,
                                                )
                                            }
                                            if (state.flag.wikipediaUrl.isNotEmpty()) {
                                                FilledTonalButton(
                                                    onClick = { onMoreInfo(state.flag.wikipediaUrl) },
                                                    shape = MaterialTheme.shapes.large,
                                                ) {
                                                    Text("More Info")
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Icon(
                                                        imageVector = Icons.Filled.Info,
                                                        contentDescription = null,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                if (!state.isAnswerRevealed) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                                state.options.forEach { country ->
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
}
