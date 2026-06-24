package com.flagtutor.app.ui.feature.pickflaggame.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.flagtutor.app.domain.model.Country
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val PIECE_COUNT = 4
private const val BASE_FADE_DURATION_MS = 120
private const val CRUMBLE_FALL_DURATION_MS = 550
private const val CRUMBLE_STAGGER_MS = 60L

private val AccelerateEasing = Easing { fraction -> fraction * fraction }

private class CrumblePieceState {
    val translationX = Animatable(0f)
    val translationY = Animatable(0f)
    val rotation = Animatable(0f)
    val alpha = Animatable(1f)
}

@Composable
fun FlagOptionImageButton(
    country: Country,
    isCorrectAnswer: Boolean,
    isCrumbled: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
) {
    val density = LocalDensity.current
    val baseAlpha = remember { Animatable(1f) }
    val pieces = remember { List(PIECE_COUNT) { CrumblePieceState() } }

    LaunchedEffect(isCrumbled) {
        if (isCrumbled) {
            launch { baseAlpha.animateTo(0f, tween(BASE_FADE_DURATION_MS)) }
            pieces.forEachIndexed { index, piece ->
                launch {
                    delay(index * CRUMBLE_STAGGER_MS)
                    val direction = if (index % 2 == 0) 1f else -1f
                    val fallDistancePx = with(density) { (50 + index * 22).dp.toPx() }
                    val driftPx = with(density) { (10 + index * 6).dp.toPx() } * direction
                    val rotationDegrees = (18f + index * 7f) * direction

                    launch {
                        piece.translationY.animateTo(
                            fallDistancePx,
                            tween(CRUMBLE_FALL_DURATION_MS, easing = AccelerateEasing),
                        )
                    }
                    launch { piece.translationX.animateTo(driftPx, tween(CRUMBLE_FALL_DURATION_MS)) }
                    launch { piece.rotation.animateTo(rotationDegrees, tween(CRUMBLE_FALL_DURATION_MS)) }
                    launch { piece.alpha.animateTo(0f, tween(CRUMBLE_FALL_DURATION_MS)) }
                }
            }
        }
    }

    val border = when {
        isCorrectAnswer -> BorderStroke(3.dp, MaterialTheme.colorScheme.tertiary)
        else -> null
    }

    Box(modifier = modifier) {
        Card(
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            border = border,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = baseAlpha.value },
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = country.flagUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                )
                if (isCorrectAnswer) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomEnd,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(28.dp),
                        )
                    }
                }
            }
        }

        if (isCrumbled) {
            pieces.forEachIndexed { index, piece ->
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            translationX = piece.translationX.value
                            translationY = piece.translationY.value
                            rotationZ = piece.rotation.value
                            alpha = piece.alpha.value
                        }
                        .clip(shape)
                        .clip(crumblePieceShape(index, PIECE_COUNT)),
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        AsyncImage(
                            model = country.flagUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun crumblePieceShape(index: Int, count: Int): Shape = GenericShape { size, _ ->
    val top = size.height * index / count
    val bottom = size.height * (index + 1) / count
    addRect(Rect(left = 0f, top = top, right = size.width, bottom = bottom))
}
