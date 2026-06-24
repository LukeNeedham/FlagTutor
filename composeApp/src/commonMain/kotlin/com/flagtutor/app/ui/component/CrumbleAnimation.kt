package com.flagtutor.app.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val CRUMBLE_PIECE_COUNT = 4
private const val BASE_FADE_DURATION_MS = 120
private const val CRUMBLE_FALL_DURATION_MS = 550
private const val CRUMBLE_STAGGER_MS = 60L

private val AccelerateEasing = Easing { fraction -> fraction * fraction }

class CrumblePieceState {
    val translationX = Animatable(0f)
    val translationY = Animatable(0f)
    val rotation = Animatable(0f)
    val alpha = Animatable(1f)
}

class CrumbleState(
    val baseAlpha: Animatable<Float, *>,
    val pieces: List<CrumblePieceState>,
)

@Composable
fun rememberCrumbleState(): CrumbleState {
    val baseAlpha = remember { Animatable(1f) }
    val pieces = remember { List(CRUMBLE_PIECE_COUNT) { CrumblePieceState() } }
    return remember { CrumbleState(baseAlpha, pieces) }
}

@Composable
fun AnimateCrumble(isCrumbled: Boolean, state: CrumbleState) {
    val density = LocalDensity.current
    LaunchedEffect(isCrumbled) {
        if (isCrumbled) {
            launch { state.baseAlpha.animateTo(0f, tween(BASE_FADE_DURATION_MS)) }
            state.pieces.forEachIndexed { index, piece ->
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
}

@Composable
fun CrumblePieces(
    state: CrumbleState,
    shape: Shape,
    modifier: Modifier = Modifier,
    pieceContent: @Composable () -> Unit,
) {
    state.pieces.forEachIndexed { index, piece ->
        Box(
            modifier = modifier
                .graphicsLayer {
                    translationX = piece.translationX.value
                    translationY = piece.translationY.value
                    rotationZ = piece.rotation.value
                    alpha = piece.alpha.value
                }
                .clip(shape)
                .clip(crumblePieceShape(index, CRUMBLE_PIECE_COUNT)),
        ) {
            pieceContent()
        }
    }
}

private fun crumblePieceShape(index: Int, count: Int): Shape = GenericShape { size, _ ->
    val top = size.height * index / count
    val bottom = size.height * (index + 1) / count
    addRect(Rect(left = 0f, top = top, right = size.width, bottom = bottom))
}
