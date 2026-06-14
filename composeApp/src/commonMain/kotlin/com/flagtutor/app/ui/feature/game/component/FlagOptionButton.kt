package com.flagtutor.app.ui.feature.game.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.flagtutor.app.domain.model.Country
import kotlinx.coroutines.launch

private const val CRUMBLE_DURATION_MS = 400
private const val CRUMBLE_SCALE = 0.5f
private const val CRUMBLE_ROTATION_DEGREES = 25f

/**
 * A button showing a [Country] name as a flag-quiz option.
 *
 * When [isCrumbled] becomes true (the option was picked incorrectly), the button shrinks,
 * rotates and fades out, leaving it disabled so the player must pick a different option.
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
    val crumbleAlpha = remember { Animatable(1f) }
    val crumbleScale = remember { Animatable(1f) }
    val crumbleRotation = remember { Animatable(0f) }

    LaunchedEffect(isCrumbled) {
        if (isCrumbled) {
            launch { crumbleAlpha.animateTo(0f, tween(CRUMBLE_DURATION_MS)) }
            launch { crumbleScale.animateTo(CRUMBLE_SCALE, tween(CRUMBLE_DURATION_MS)) }
            launch { crumbleRotation.animateTo(CRUMBLE_ROTATION_DEGREES, tween(CRUMBLE_DURATION_MS)) }
        }
    }

    val colors = if (isCorrectAnswer) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        )
    } else {
        ButtonDefaults.buttonColors()
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        colors = colors,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = crumbleAlpha.value
                scaleX = crumbleScale.value
                scaleY = crumbleScale.value
                rotationZ = crumbleRotation.value
            },
    ) {
        Text(country.name)
    }
}
