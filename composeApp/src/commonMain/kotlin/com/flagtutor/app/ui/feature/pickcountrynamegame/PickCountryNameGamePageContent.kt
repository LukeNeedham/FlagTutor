package com.flagtutor.app.ui.feature.pickcountrynamegame

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flagtutor.app.domain.model.Country
import com.flagtutor.app.domain.util.GoogleMapsLinkBuilder
import com.flagtutor.app.ui.component.CountryMapHighlight
import com.flagtutor.app.ui.feature.pickcountrynamegame.component.FlagOptionButton
import com.flagtutor.app.ui.util.ExtractedColor
import com.flagtutor.app.ui.util.decodeImageBitmap
import com.flagtutor.app.ui.util.extractColorsFromImage
import flagtutor.composeapp.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi

private data class FlagData(val bitmap: ImageBitmap, val colors: List<ExtractedColor>)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun PickCountryNameGamePageContent(
    uiState: PickCountryNameGameUiState,
    onOptionSelected: (Country) -> Unit,
    onNextFlag: () -> Unit,
    onMoreInfo: (String) -> Unit,
    onOpenMap: (String) -> Unit,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
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
                is PickCountryNameGameUiState.Loading -> {
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

                is PickCountryNameGameUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Couldn't load flags. Please try again.",
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

                is PickCountryNameGameUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
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
                            var flagData by remember { mutableStateOf<FlagData?>(null) }

                            LaunchedEffect(state.flag.alpha2Code) {
                                flagData = withContext(Dispatchers.Default) {
                                    val bytes = Res.readBytes("files/flags/${state.flag.alpha2Code}.png")
                                    FlagData(
                                        bitmap = decodeImageBitmap(bytes),
                                        colors = extractColorsFromImage(bytes, 4),
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                flagData?.bitmap?.let { bmp ->
                                    Image(
                                        bitmap = bmp,
                                        contentDescription = null,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxWidth(0.85f)
                                            .aspectRatio(3f / 2f),
                                    )
                                } ?: Spacer(
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .aspectRatio(3f / 2f),
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                val revealTransition = updateTransition(
                                    targetState = state.isAnswerRevealed,
                                    label = "reveal-transition",
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                ) {
                                    revealTransition.AnimatedContent(
                                        contentAlignment = Alignment.TopCenter,
                                        transitionSpec = {
                                            (
                                                slideIntoContainer(
                                                    towards = SlideDirection.Down,
                                                    animationSpec = tween(400),
                                                ) + fadeIn(tween(400))
                                                ).togetherWith(
                                                slideOutOfContainer(
                                                    towards = SlideDirection.Down,
                                                    animationSpec = tween(400),
                                                ) + fadeOut(tween(400)),
                                            )
                                        },
                                        modifier = Modifier.fillMaxSize(),
                                    ) { revealed ->
                                        if (revealed) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.fillMaxWidth(),
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.fillMaxWidth(),
                                                ) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                    Text(
                                                        text = state.flag.name,
                                                        style = MaterialTheme.typography.headlineMedium,
                                                        color = MaterialTheme.colorScheme.onBackground,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier.clickable(
                                                            enabled = state.flag.wikipediaUrl.isNotEmpty(),
                                                            onClick = { onMoreInfo(state.flag.wikipediaUrl) },
                                                        ),
                                                    )
                                                    Box(
                                                        modifier = Modifier.weight(1f),
                                                        contentAlignment = Alignment.CenterStart,
                                                    ) {
                                                        if (state.flag.wikipediaUrl.isNotEmpty()) {
                                                            IconButton(
                                                                onClick = { onMoreInfo(state.flag.wikipediaUrl) },
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Filled.Info,
                                                                    contentDescription = "More Info",
                                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(12.dp))
                                                CountryMapHighlight(
                                                    alpha2Code = state.flag.alpha2Code,
                                                    modifier = Modifier
                                                        .fillMaxWidth(0.85f)
                                                        .aspectRatio(16f / 10f)
                                                        .clickable(onClick = { onOpenMap(GoogleMapsLinkBuilder.searchUrl(state.flag.name)) }),
                                                )
                                                Spacer(modifier = Modifier.height(20.dp))
                                                Button(
                                                    onClick = onNextFlag,
                                                    shape = MaterialTheme.shapes.large,
                                                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
                                                    modifier = Modifier.fillMaxWidth(0.85f),
                                                ) {
                                                    Text(
                                                        text = "Next flag",
                                                        style = MaterialTheme.typography.titleMedium,
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                        contentDescription = null,
                                                    )
                                                }
                                            }
                                        } else {
                                            Column(modifier = Modifier.fillMaxSize()) {
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
                                                    val buttonColors = flagData?.colors ?: emptyList()
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
                                    if (revealTransition.isRunning) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(28.dp)
                                                .align(Alignment.TopCenter)
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            MaterialTheme.colorScheme.background,
                                                            MaterialTheme.colorScheme.background.copy(alpha = 0f),
                                                        ),
                                                    ),
                                                ),
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

private fun checkerboardColorOrder(colors: List<ExtractedColor>): IntArray {
    if (colors.isEmpty()) return intArrayOf(0, 1, 2, 3)

    val effective = List(4) { colors[it % colors.size] }

    fun dist(i: Int, j: Int): Float {
        val a = effective[i].containerColor
        val b = effective[j].containerColor
        val dr = a.red - b.red
        val dg = a.green - b.green
        val db = a.blue - b.blue
        return dr * dr + dg * dg + db * db
    }

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

    val order = intArrayOf(best[0], best[2], best[3], best[1])
    return IntArray(4) { order[it] % colors.size }
}
