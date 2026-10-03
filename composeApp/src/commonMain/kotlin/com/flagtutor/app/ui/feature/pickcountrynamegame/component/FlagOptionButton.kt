package com.flagtutor.app.ui.feature.pickcountrynamegame.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.flagtutor.app.ui.theme.AppTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flagtutor.app.domain.model.Country
import com.flagtutor.app.ui.component.AnimateCrumble
import com.flagtutor.app.ui.component.CrumblePieces
import com.flagtutor.app.ui.component.rememberCrumbleState
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
    isCrumbled: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = AppTheme.shapes.large,
    containerColor: Color? = null,
    contentColor: Color? = null,
) {
    val crumbleState = rememberCrumbleState()
    AnimateCrumble(isCrumbled, crumbleState)

    val activeContainerColor = containerColor ?: AppTheme.colors.option
    val activeContentColor = when {
        containerColor != null -> contentColor ?: AppTheme.colors.onPrimary
        else -> AppTheme.colors.onOption
    }
    // The button is disabled once the answer is revealed, but it must keep its colours (it animates
    // out straight away). Only a crumbled (wrong) option takes on the default disabled look.
    val colors = ButtonDefaults.buttonColors(
        containerColor = activeContainerColor,
        contentColor = activeContentColor,
        disabledContainerColor = if (isCrumbled) Color.Unspecified else activeContainerColor,
        disabledContentColor = if (isCrumbled) Color.Unspecified else activeContentColor,
    )

    val backgroundColor = AppTheme.colors.background
    val border = if (isSimilarColor(activeContainerColor, backgroundColor)) {
        BorderStroke(BorderWidth, AppTheme.colors.onBackground)
    } else null

    Box(modifier = modifier) {
        Button(
            onClick = onClick,
            enabled = enabled,
            colors = colors,
            shape = shape,
            border = border,
            contentPadding = PaddingValues(vertical = 16.dp, horizontal = 24.dp),
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = crumbleState.baseAlpha.value },
        ) {
            Text(country.name, textAlign = TextAlign.Center)
        }

        if (isCrumbled) {
            val pieceContainerColor = AppTheme.colors.text.copy(alpha = 0.12f)
            val pieceContentColor = AppTheme.colors.text.copy(alpha = 0.38f)

            CrumblePieces(
                state = crumbleState,
                shape = shape,
                modifier = Modifier.matchParentSize(),
            ) {
                Surface(
                    color = pieceContainerColor,
                    contentColor = pieceContentColor,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(country.name, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}
