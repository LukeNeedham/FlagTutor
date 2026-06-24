package com.flagtutor.app.ui.feature.pickflaggame.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.flagtutor.app.domain.model.Country
import com.flagtutor.app.ui.component.AnimateCrumble
import com.flagtutor.app.ui.component.CrumblePieces
import com.flagtutor.app.ui.component.rememberCrumbleState

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
    val crumbleState = rememberCrumbleState()
    AnimateCrumble(isCrumbled, crumbleState)

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
                .graphicsLayer { alpha = crumbleState.baseAlpha.value },
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
            CrumblePieces(
                state = crumbleState,
                shape = shape,
                modifier = Modifier.matchParentSize(),
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
