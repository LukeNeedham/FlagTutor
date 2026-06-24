package com.flagtutor.app.ui.feature.pickcountrynamegame.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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

@Composable
fun FlagOptionButton(
    country: Country,
    isCorrectAnswer: Boolean,
    isCrumbled: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    containerColor: Color? = null,
    contentColor: Color? = null,
) {
    val crumbleState = rememberCrumbleState()
    AnimateCrumble(isCrumbled, crumbleState)

    val colors = when {
        isCorrectAnswer -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary,
        )
        containerColor != null -> ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor ?: MaterialTheme.colorScheme.onPrimary,
        )
        else -> ButtonDefaults.filledTonalButtonColors()
    }

    Box(modifier = modifier) {
        Button(
            onClick = onClick,
            enabled = enabled,
            colors = colors,
            shape = shape,
            contentPadding = PaddingValues(vertical = 16.dp, horizontal = 24.dp),
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = crumbleState.baseAlpha.value },
        ) {
            if (isCorrectAnswer) {
                Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(country.name, textAlign = TextAlign.Center)
        }

        if (isCrumbled) {
            val pieceContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            val pieceContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

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
