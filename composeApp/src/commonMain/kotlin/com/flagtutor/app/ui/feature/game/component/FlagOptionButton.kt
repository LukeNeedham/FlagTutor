package com.flagtutor.app.ui.feature.game.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.flagtutor.app.domain.model.Country
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val PIECE_COUNT = 4
private const val BASE_FADE_DURATION_MS = 120
private const val CRUMBLE_FALL_DURATION_MS = 550
private const val CRUMBLE_STAGGER_MS = 60L

/** Eases in like an object accelerating under gravity. */
private val AccelerateEasing = Easing { fraction -> fraction * fraction }

/** Per-piece animation state for the crumble effect. */
private class CrumblePieceState {
    val translationX = Animatable(0f)
    val translationY = Animatable(0f)
    val rotation = Animatable(0f)
    val alpha = Animatable(1f)
}

/**
 * A button showing a [Country] name as a flag-quiz option.
 *
 * When [isCrumbled] becomes true (the option was picked incorrectly), the button fades out while
 * shattering into pieces that tumble and fall away, leaving it disabled so the player must pick a
 * different option.
 */
@Composable
fun FlagOptionButton(
    country: Country,
    isCorrectAnswer: Boolean,
    isCrumbled: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
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

    val colors = if (isCorrectAnswer) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary,
        )
    } else {
        ButtonDefaults.filledTonalButtonColors()
    }

    Box(modifier = modifier) {
        Button(
            onClick = onClick,
            enabled = enabled,
            colors = colors,
            shape = MaterialTheme.shapes.large,
            contentPadding = PaddingValues(vertical = 16.dp, horizontal = 24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = baseAlpha.value },
        ) {
            if (isCorrectAnswer) {
                Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(country.name)
        }

        if (isCrumbled) {
            val pieceContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            val pieceContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

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
                        .clip(MaterialTheme.shapes.large)
                        .clip(crumblePieceShape(index, PIECE_COUNT)),
                ) {
                    Surface(
                        color = pieceContainerColor,
                        contentColor = pieceContentColor,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(country.name)
                        }
                    }
                }
            }
        }
    }
}

/** A horizontal slice of the button's bounds, used to split it into crumbling pieces. */
private fun crumblePieceShape(index: Int, count: Int): Shape = GenericShape { size, _ ->
    val top = size.height * index / count
    val bottom = size.height * (index + 1) / count
    addRect(Rect(left = 0f, top = top, right = size.width, bottom = bottom))
}
