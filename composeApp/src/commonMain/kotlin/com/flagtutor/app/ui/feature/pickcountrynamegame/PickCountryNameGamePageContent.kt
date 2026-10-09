package com.flagtutor.app.ui.feature.pickcountrynamegame

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.flagtutor.app.ui.theme.AppTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flagtutor.app.ui.util.LocalScaledAnimation
import com.flagtutor.app.domain.model.Country
import com.flagtutor.app.domain.util.GoogleMapsLinkBuilder
import com.flagtutor.app.ui.feature.pickcountrynamegame.component.FlagOptionButton
import com.flagtutor.app.ui.component.BoundedCountryMap
import com.flagtutor.app.ui.util.ExtractedColor
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.max
import kotlin.math.sqrt

// Next button occupies 10dp top margin + 64dp height + 10dp bottom margin; leave at least 30dp above that.
private val NextButtonReservedHeight = 114.dp

// Horizontal inset shared by the option buttons and the Next button, so the two are the same width.
private val ContentHorizontalPadding = 24.dp

private val NextButtonHeight = 64.dp

// The Next button's margin from the left, right and bottom edges of the answer panel it sits in.
private val NextButtonMargin = 10.dp

// The answer panel (the flooded area) sits this far above the bottom of the screen.
private val AnswerPanelBottomInset = 16.dp

// The corner radius of the option buttons grid, and so of the answer panel that floods it.
private val AnswerPanelCornerRadius = 24.dp

// The round buttons beside the name.
private val AnswerActionButtonSize = 44.dp

@OptIn(ExperimentalResourceApi::class)
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
    val animation = LocalScaledAnimation.current
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppTheme.colors.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.padding(start = 5.dp, top = 5.dp),
            ) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
            }
            Spacer(modifier = Modifier.height(5.dp))
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (uiState) {
                is PickCountryNameGameUiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(color = AppTheme.colors.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading flags…",
                            style = AppTheme.typography.bodyLarge,
                            color = AppTheme.colors.textSecondary,
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
                            style = AppTheme.typography.bodyLarge,
                            color = AppTheme.colors.textSecondary,
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onRetry, shape = AppTheme.shapes.large) {
                            Icon(imageVector = Icons.Filled.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }

                is PickCountryNameGameUiState.Success -> {
                    // The Next button's slide, as fractions of its own size: it enters from below and leaves
                    // to the left. This is driven by hand rather than with AnimatedVisibility, because that
                    // swaps in a default spring for the exit if it interrupts a half-finished enter, so the
                    // button would then leave at a different speed to the rest of the content.
                    val nextButtonSlideX = remember { Animatable(-1f) }
                    val nextButtonAlpha = remember { Animatable(0f) }
                    val isAnswerRevealed = uiState.isAnswerRevealed
                    // True once the colour flood has covered the option buttons: only then does the Next
                    // button come in, along with the answer content.
                    var isFloodDone by remember { mutableStateOf(false) }
                    // The Next button's colours come from the flood of the flag whose answer is showing, so
                    // they stay put while it slides away after the next flag has loaded.
                    val currentFloodColors = floodColorsFor(uiState)
                    var nextButtonColors by remember { mutableStateOf(currentFloodColors) }
                    LaunchedEffect(isFloodDone) {
                        if (isFloodDone) nextButtonColors = currentFloodColors
                    }
                    LaunchedEffect(isAnswerRevealed) {
                        if (!isAnswerRevealed) isFloodDone = false
                    }
                    LaunchedEffect(isFloodDone, isAnswerRevealed) {
                        if (isFloodDone) {
                            nextButtonSlideX.snapTo(0f)
                            nextButtonAlpha.animateTo(1f, tween(animation.long))
                        } else if (!isAnswerRevealed) {
                            nextButtonSlideX.animateTo(-1f, tween(animation.medium))
                            nextButtonAlpha.snapTo(0f)
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = AnswerPanelBottomInset),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        AnimatedContent(
                            targetState = uiState,
                            contentKey = { it.flag.alpha2Code },
                            transitionSpec = {
                                slideInHorizontally(
                                    animationSpec = tween(animation.medium),
                                ) { fullWidth -> fullWidth }.togetherWith(
                                    slideOutHorizontally(
                                        animationSpec = tween(animation.medium),
                                    ) { fullWidth -> -fullWidth },
                                ).using(SizeTransform(clip = false))
                            },
                            label = "flag-transition",
                            modifier = Modifier.fillMaxWidth().weight(1f),
                        ) { state ->
                            BoxWithConstraints(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = ContentHorizontalPadding),
                            ) {
                                val gridCornerRadius = AnswerPanelCornerRadius
                                val gridGap = 12.dp
                                val gridShape = RoundedCornerShape(gridCornerRadius)
                                val colorOrder = checkerboardColorOrder(state.colors)
                                val correctIndex = state.options.indexOfFirst { it.alpha2Code == state.flag.alpha2Code }
                                // The flood is the colour of the correct option's button.
                                val (floodColor, floodContentColor) = floodColorsFor(state)
                                // Where the correct option was tapped, within its button: the flood spreads out from here.
                                var floodTouchPoint by remember { mutableStateOf<Offset?>(null) }
                                // Fills from the correct option's button across the whole grid of options, and
                                // only then does the answer content come in on top of it.
                                val floodProgress = remember { Animatable(if (state.isAnswerRevealed) 1f else 0f) }
                                var showAnswer by remember { mutableStateOf(state.isAnswerRevealed) }
                                // Once the flood has covered the option buttons they are no longer drawn, and the
                                // answer content fades in over the flood colour.
                                val answerAlpha = remember { Animatable(if (state.isAnswerRevealed) 1f else 0f) }
                                LaunchedEffect(showAnswer) {
                                    if (showAnswer) answerAlpha.animateTo(1f, tween(animation.long))
                                }
                                val buttonsPanelHeight = maxHeight * 0.7f
                                val flagMaxWidth = maxWidth * 0.85f
                                val flagMaxHeight = maxHeight * 0.3f
                                LaunchedEffect(state.isAnswerRevealed) {
                                    if (state.isAnswerRevealed) {
                                        if (!showAnswer) {
                                            floodProgress.animateTo(1f, tween(animation.flood))
                                            showAnswer = true
                                        }
                                        isFloodDone = true
                                    }
                                }

                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    val bmp = state.flagImage
                                    val bitmapAspectRatio = bmp.width.toFloat() / bmp.height.toFloat()
                                    val flagWidth = minOf(flagMaxWidth, flagMaxHeight * bitmapAspectRatio)
                                    Image(
                                        bitmap = bmp,
                                        contentDescription = null,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .width(flagWidth)
                                            .aspectRatio(bitmapAspectRatio),
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth(),
                                    ) {
                                        // The flood stays behind the answer content once the option buttons are gone.
                                        if (showAnswer) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .fillMaxWidth()
                                                    .height(buttonsPanelHeight)
                                                    .padding(top = 16.dp)
                                                    .clip(gridShape)
                                                    .background(floodColor),
                                            )
                                        }
                                        run {
                                            if (showAnswer) {
                                                Box(modifier = Modifier.fillMaxSize()) {
                                                Column(
                                                    horizontalAlignment = Alignment.Start,
                                                    modifier = Modifier
                                                        .align(Alignment.BottomCenter)
                                                        .fillMaxWidth()
                                                        .height(buttonsPanelHeight)
                                                        .graphicsLayer { alpha = answerAlpha.value }
                                                        .padding(top = 16.dp, start = 20.dp, end = 20.dp),
                                                ) {
                                                    Spacer(modifier = Modifier.height(12.dp))
                                                    // The name is left aligned and always on one line, shrinking to fit; round
                                                    // buttons for the map and Wikipedia sit at the end, centred with it.
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        modifier = Modifier.fillMaxWidth(),
                                                    ) {
                                                        Text(
                                                            text = state.flag.name,
                                                            style = AppTheme.typography.headlineLarge,
                                                            color = floodContentColor,
                                                            maxLines = 1,
                                                            autoSize = TextAutoSize.StepBased(
                                                                minFontSize = 12.sp,
                                                                maxFontSize = AppTheme.typography.headlineLarge.fontSize,
                                                            ),
                                                            modifier = Modifier.weight(1f),
                                                        )
                                                        AnswerActionButton(
                                                            icon = Icons.Filled.LocationOn,
                                                            contentDescription = "Show location in Maps",
                                                            tint = floodContentColor,
                                                            onClick = { onOpenMap(GoogleMapsLinkBuilder.searchUrl(state.flag.name)) },
                                                        )
                                                        if (state.flag.wikipediaUrl.isNotEmpty()) {
                                                            AnswerActionButton(
                                                                icon = Icons.Filled.Info,
                                                                contentDescription = "More Info",
                                                                tint = floodContentColor,
                                                                onClick = { onMoreInfo(state.flag.wikipediaUrl) },
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(1.dp)
                                                            .background(floodContentColor.copy(alpha = 0.2f)),
                                                    )
                                                    if (!state.flag.flagSymbolism.isNullOrEmpty()) {
                                                        Spacer(modifier = Modifier.height(16.dp))
                                                        Text(
                                                            text = state.flag.flagSymbolism,
                                                            style = AppTheme.typography.bodyLarge,
                                                            color = floodContentColor.copy(alpha = 0.7f),
                                                            modifier = Modifier.fillMaxWidth(),
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(20.dp))
                                                    // The map image carries its own background, so it is drawn bare.
                                                    BoundedCountryMap(
                                                        bitmap = state.mapImage,
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .fillMaxWidth(),
                                                        onClick = { onOpenMap(GoogleMapsLinkBuilder.searchUrl(state.flag.name)) },
                                                    )
                                                    Spacer(modifier = Modifier.height(NextButtonReservedHeight))
                                                }
                                                }
                                            } else {
                                                Box(modifier = Modifier.fillMaxSize()) {
                                                    Column(
                                                        modifier = Modifier
                                                            .align(Alignment.BottomCenter)
                                                            .fillMaxWidth()
                                                            .height(buttonsPanelHeight),
                                                    ) {
                                                        Spacer(modifier = Modifier.height(16.dp))
                                                        Column(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .fillMaxWidth()
                                                                .clip(gridShape)
                                                                // Paint the flood over the buttons, in a circle growing from the
                                                                // tap point on the correct one until every button is covered.
                                                                .drawWithContent {
                                                                    drawContent()
                                                                    val progress = floodProgress.value
                                                                    if (progress <= 0f || correctIndex < 0) return@drawWithContent
                                                                    val gap = gridGap.toPx()
                                                                    val cellWidth = (size.width - gap) / 2f
                                                                    val cellHeight = (size.height - gap) / 2f
                                                                    val touch = floodTouchPoint ?: Offset(cellWidth / 2f, cellHeight / 2f)
                                                                    val origin = Offset(
                                                                        x = (correctIndex % 2) * (cellWidth + gap) + touch.x,
                                                                        y = (correctIndex / 2) * (cellHeight + gap) + touch.y,
                                                                    )
                                                                    val farthestX = max(origin.x, size.width - origin.x)
                                                                    val farthestY = max(origin.y, size.height - origin.y)
                                                                    val fullRadius = sqrt(farthestX * farthestX + farthestY * farthestY)
                                                                    drawCircle(color = floodColor, radius = fullRadius * progress, center = origin)
                                                                },
                                                            verticalArrangement = Arrangement.spacedBy(gridGap),
                                                        ) {
                                                            run {
                                                                val cornerRadius = gridCornerRadius
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
                                                                val buttonColors = state.colors
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
                                                                                    isErased = country.alpha2Code in state.incorrectAlpha2Codes,
                                                                                    enabled = !state.isAnswerRevealed && country.alpha2Code !in state.incorrectAlpha2Codes,
                                                                                    onClick = { onOptionSelected(country) },
                                                                                    onTouch = { point ->
                                                                                        if (country.alpha2Code == state.flag.alpha2Code) floodTouchPoint = point
                                                                                    },
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
                        }
                    }

                    // Fades in over the flooded answer panel, and leaves by sliding with the rest of the screen.
                    // The padding lives inside the content so the slide distance covers it and the button is
                    // fully off screen.
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .graphicsLayer {
                                translationX = nextButtonSlideX.value * size.width
                                alpha = nextButtonAlpha.value
                            },
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier.padding(
                                    start = ContentHorizontalPadding + NextButtonMargin,
                                    end = ContentHorizontalPadding + NextButtonMargin,
                                    bottom = AnswerPanelBottomInset + NextButtonMargin,
                                ),
                            ) {
                                Button(
                                    onClick = onNextFlag,
                                    // Square at the top, and concentric with the panel's rounded corners at the bottom.
                                    shape = RoundedCornerShape(
                                        bottomStart = AnswerPanelCornerRadius - NextButtonMargin,
                                        bottomEnd = AnswerPanelCornerRadius - NextButtonMargin,
                                    ),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = nextButtonColors.second,
                                        contentColor = nextButtonColors.first,
                                    ),
                                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(NextButtonHeight),
                                ) {
                                    Text(
                                        text = "Next",
                                        style = AppTheme.typography.titleMedium,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
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

/** A round button, tinted from [tint], shown beside the answer's name. */
@Composable
private fun AnswerActionButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(AnswerActionButtonSize)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.16f)),
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription, tint = tint)
    }
}

/** The flood colour (the correct option button's colour) and the colour to draw on top of it. */
@Composable
private fun floodColorsFor(state: PickCountryNameGameUiState.Success): Pair<Color, Color> {
    val correctIndex = state.options.indexOfFirst { it.alpha2Code == state.flag.alpha2Code }
    val extracted = if (state.colors.isNotEmpty() && correctIndex >= 0) {
        state.colors[checkerboardColorOrder(state.colors)[correctIndex]]
    } else null
    return Pair(
        extracted?.containerColor ?: AppTheme.colors.option,
        extracted?.contentColor ?: AppTheme.colors.onOption,
    )
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
