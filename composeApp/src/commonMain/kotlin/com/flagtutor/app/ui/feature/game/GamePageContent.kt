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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.flagtutor.app.domain.model.Country
import com.flagtutor.app.ui.component.CountryMapHighlight
import com.flagtutor.app.ui.feature.game.component.FlagOptionButton
import com.flagtutor.app.ui.util.ExtractedColor
import com.flagtutor.app.ui.util.extractColorsFromImage

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
                            modifier = Modifier.fillMaxWidth().weight(1f),
                        ) { state ->
                            var buttonColors by remember { mutableStateOf<List<ExtractedColor>>(emptyList()) }

                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                AsyncImage(
                                    model = state.flag.flagUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit,
                                    onSuccess = { success ->
                                        buttonColors = extractColorsFromImage(success.result.image, 4)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .aspectRatio(3f / 2f),
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
                                Spacer(modifier = Modifier.height(16.dp))
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    val cornerRadius = 24.dp
                                    val gridShapes = arrayOf(
                                        arrayOf(
                                            RoundedCornerShape(topStart = cornerRadius),
                                            RoundedCornerShape(topEnd = cornerRadius),
                                        ),
                                        arrayOf(
                                            RoundedCornerShape(bottomStart = cornerRadius),
                                            RoundedCornerShape(bottomEnd = cornerRadius),
                                        ),
                                    )
                                    val colorOrder = checkerboardColorOrder(buttonColors)
                                    state.options.chunked(2).forEachIndexed { rowIndex, rowOptions ->
                                        Row(
                                            modifier = Modifier.weight(1f).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        ) {
                                            rowOptions.forEachIndexed { colIndex, country ->
                                                val colorIndex = colorOrder[rowIndex * 2 + colIndex]
                                                val extractedColor = if (buttonColors.isNotEmpty()) {
                                                    buttonColors[colorIndex]
                                                } else null

                                                key(country.alpha2Code) {
                                                    FlagOptionButton(
                                                        country = country,
                                                        isCorrectAnswer = state.isAnswerRevealed && country.alpha2Code == state.flag.alpha2Code,
                                                        isCrumbled = country.alpha2Code in state.incorrectAlpha2Codes,
                                                        enabled = !state.isAnswerRevealed && country.alpha2Code !in state.incorrectAlpha2Codes,
                                                        onClick = { onOptionSelected(country) },
                                                        shape = gridShapes[rowIndex][colIndex],
                                                        containerColor = extractedColor?.containerColor,
                                                        contentColor = extractedColor?.contentColor,
                                                        modifier = Modifier.weight(1f).fillMaxHeight(),
                                                    )
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
        }
    }
}

/**
 * Returns color indices arranged so the most similar pair sits on the diagonal
 * (non-adjacent grid positions), avoiding same-colour buttons touching.
 * Output order: [topLeft, topRight, bottomLeft, bottomRight].
 */
private fun checkerboardColorOrder(colors: List<ExtractedColor>): IntArray {
    if (colors.size < 4) return intArrayOf(0, 1, 2, 3)

    fun dist(i: Int, j: Int): Float {
        val a = colors[i].containerColor
        val b = colors[j].containerColor
        val dr = a.red - b.red
        val dg = a.green - b.green
        val db = a.blue - b.blue
        return dr * dr + dg * dg + db * db
    }

    // Try all 3 ways to split 4 colours into 2 diagonal pairs.
    // Diagonal A = positions (0,0) and (1,1) — non-adjacent.
    // Diagonal B = positions (0,1) and (1,0) — non-adjacent.
    // All grid adjacencies are cross-diagonal, so we maximise the
    // minimum cross-diagonal colour distance.
    val splits = arrayOf(
        intArrayOf(0, 1, 2, 3),
        intArrayOf(0, 2, 1, 3),
        intArrayOf(0, 3, 1, 2),
    )

    var best = splits[0]
    var bestMin = -1f
    for (s in splits) {
        val min = minOf(
            minOf(dist(s[0], s[2]), dist(s[0], s[3])),
            minOf(dist(s[1], s[2]), dist(s[1], s[3])),
        )
        if (min > bestMin) {
            bestMin = min
            best = s
        }
    }

    // best = [diagA1, diagA2, diagB1, diagB2]
    // Grid: topLeft=A1, topRight=B1, bottomLeft=B2, bottomRight=A2
    return intArrayOf(best[0], best[2], best[3], best[1])
}
