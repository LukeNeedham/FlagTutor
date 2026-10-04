package com.flagtutor.app.ui.feature.pickcountrynamegame.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import com.flagtutor.app.ui.util.clickableNoRipple
import com.flagtutor.app.ui.theme.AppTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import com.flagtutor.app.ui.util.LocalScaledAnimation
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flagtutor.app.domain.model.Country
import kotlin.math.max
import kotlin.math.sqrt

private val BorderWidth = 2.dp
private const val SimilarColorThreshold = 0.12f

/** True when two colors are close enough that the button would blend into the background. */
private fun isSimilarColor(a: Color, b: Color): Boolean {
    val dr = a.red - b.red
    val dg = a.green - b.green
    val db = a.blue - b.blue
    return sqrt((dr * dr + dg * dg + db * db) / 3f) < SimilarColorThreshold
}

@Composable
fun FlagOptionButton(
    country: Country,
    isErased: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = AppTheme.shapes.large,
    containerColor: Color? = null,
    contentColor: Color? = null,
) {
    val animation = LocalScaledAnimation.current
    // Where the button was last touched: the ripple that erases it spreads out from here.
    var touchPoint by remember { mutableStateOf<Offset?>(null) }
    val rippleProgress = remember { Animatable(if (isErased) 1f else 0f) }
    LaunchedEffect(isErased) {
        if (isErased) rippleProgress.animateTo(1f, tween(animation.long))
    }

    val activeContainerColor = containerColor ?: AppTheme.colors.option
    val activeContentColor = when {
        containerColor != null -> contentColor ?: AppTheme.colors.onPrimary
        else -> AppTheme.colors.onOption
    }
    val backgroundColor = AppTheme.colors.background
    val border = if (isSimilarColor(activeContainerColor, backgroundColor)) {
        BorderStroke(BorderWidth, AppTheme.colors.onBackground)
    } else null

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            // Watch for the touch without consuming it, so the button still gets its click.
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        event.changes.firstOrNull { it.pressed }?.let { touchPoint = it.position }
                    }
                }
            }
            .clip(shape)
            // Paint the background colour over the button in a circle growing from the touch point, until
            // the whole button is covered and so effectively erased.
            .drawWithContent {
                val progress = rippleProgress.value
                // Once fully erased, draw nothing at all. The circle and the button's own edge are both
                // anti-aliased, so painting the circle over the button would leave a faint sliver of the
                // border showing at the corners.
                if (progress >= 1f) return@drawWithContent
                drawContent()
                if (progress > 0f) {
                    val origin = touchPoint ?: Offset(size.width / 2f, size.height / 2f)
                    val farthestX = max(origin.x, size.width - origin.x)
                    val farthestY = max(origin.y, size.height - origin.y)
                    val fullRadius = sqrt(farthestX * farthestX + farthestY * farthestY)
                    drawCircle(color = backgroundColor, radius = fullRadius * progress, center = origin)
                }
            }
            .clickableNoRipple(enabled = enabled, onClick = onClick)
            .background(activeContainerColor, shape)
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .padding(vertical = 16.dp, horizontal = 24.dp),
    ) {
        Text(
            text = country.name,
            color = activeContentColor,
            style = AppTheme.typography.titleSmall,
            textAlign = TextAlign.Center,
        )
    }
}
