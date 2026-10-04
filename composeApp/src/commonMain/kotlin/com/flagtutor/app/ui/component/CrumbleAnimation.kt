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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.flagtutor.app.ui.util.LocalScaledAnimation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

private const val SpokeCount = 6
private val RingRadii = floatArrayOf(0.2f, 0.5f, 1.1f)

private val AccelerateEasing = Easing { fraction -> fraction * fraction }

/**
 * One fragment of the shattered button. Points are fractions of the button's size, so the shard scales
 * with whatever it is clipping. [ring] is 0 for the fragments at the impact point and grows outwards.
 */
class CrumbleShard(
    val points: List<Offset>,
    val ring: Int,
    val centroid: Offset,
    val shape: Shape,
)

/**
 * Cracks radiating from the middle of the button, crossed by rings of cracks, like glass struck at its
 * centre. Fixed seed, so every button shatters the same way and the pieces stay stable across recomposition.
 */
private fun buildShards(): List<CrumbleShard> {
    val random = Random(7)
    val angleStep = 2f * PI.toFloat() / SpokeCount
    val spokeAngles = FloatArray(SpokeCount) { spoke ->
        spoke * angleStep + (random.nextFloat() - 0.5f) * angleStep * 0.5f
    }
    val center = Offset(0.5f, 0.5f)
    // vertices[ring][spoke]: crack intersections, jittered so the fragments are irregular.
    val vertices = List(RingRadii.size) { ring ->
        List(SpokeCount) { spoke ->
            val radius = RingRadii[ring] * (1f + (random.nextFloat() - 0.5f) * 0.3f)
            val angle = spokeAngles[spoke] + (random.nextFloat() - 0.5f) * angleStep * 0.3f
            Offset(center.x + cos(angle) * radius, center.y + sin(angle) * radius)
        }
    }
    val shards = mutableListOf<CrumbleShard>()
    for (ring in RingRadii.indices) {
        for (spoke in 0 until SpokeCount) {
            val next = (spoke + 1) % SpokeCount
            val points = if (ring == 0) {
                listOf(center, vertices[0][spoke], vertices[0][next])
            } else {
                listOf(vertices[ring - 1][spoke], vertices[ring - 1][next], vertices[ring][next], vertices[ring][spoke])
            }
            val centroid = Offset(points.map { it.x }.average().toFloat(), points.map { it.y }.average().toFloat())
            shards += CrumbleShard(points, ring, centroid, shardShape(points))
        }
    }
    return shards
}

private fun shardShape(points: List<Offset>): Shape = GenericShape { size, _ ->
    points.forEachIndexed { index, point ->
        val x = point.x * size.width
        val y = point.y * size.height
        if (index == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

class CrumblePieceState {
    val translationX = Animatable(0f)
    val translationY = Animatable(0f)
    val rotation = Animatable(0f)
    val alpha = Animatable(1f)
}

class CrumbleState(
    val baseAlpha: Animatable<Float, *>,
    val shards: List<CrumbleShard>,
    val pieces: List<CrumblePieceState>,
)

@Composable
fun rememberCrumbleState(): CrumbleState {
    val baseAlpha = remember { Animatable(1f) }
    val shards = remember { buildShards() }
    val pieces = remember { List(shards.size) { CrumblePieceState() } }
    return remember { CrumbleState(baseAlpha, shards, pieces) }
}

@Composable
fun AnimateCrumble(isCrumbled: Boolean, state: CrumbleState) {
    val density = LocalDensity.current
    val animation = LocalScaledAnimation.current
    LaunchedEffect(isCrumbled) {
        if (isCrumbled) {
            launch { state.baseAlpha.animateTo(0f, tween(animation.short)) }
            state.pieces.forEachIndexed { index, piece ->
                val shard = state.shards[index]
                launch {
                    // The pieces at the impact point let go first, the rest follow as the cracks spread.
                    delay(shard.ring * animation.stagger)
                    val offsetX = shard.centroid.x - 0.5f
                    val offsetY = shard.centroid.y - 0.5f
                    val length = sqrt(offsetX * offsetX + offsetY * offsetY).coerceAtLeast(0.01f)
                    // Thrown outwards from the impact, then pulled down by gravity.
                    val throwDistancePx = with(density) { (14 + shard.ring * 8).dp.toPx() }
                    val driftX = offsetX / length * throwDistancePx
                    val fallDistancePx = with(density) { (60 + (index % 5) * 14).dp.toPx() } + offsetY / length * throwDistancePx
                    val rotationDegrees = (14f + (index % 4) * 9f) * if (offsetX >= 0f) 1f else -1f

                    launch { piece.translationY.animateTo(fallDistancePx, tween(animation.extraLong, easing = AccelerateEasing)) }
                    launch { piece.translationX.animateTo(driftX, tween(animation.extraLong)) }
                    launch { piece.rotation.animateTo(rotationDegrees, tween(animation.extraLong)) }
                    launch { piece.alpha.animateTo(0f, tween(animation.extraLong)) }
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
                .clip(state.shards[index].shape),
        ) {
            pieceContent()
        }
    }
}
