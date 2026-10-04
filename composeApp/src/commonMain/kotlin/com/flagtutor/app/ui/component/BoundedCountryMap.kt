package com.flagtutor.app.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import com.flagtutor.app.ui.theme.AppTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

private const val MAX_WIDTH_FRACTION = 1f
private const val PLACEHOLDER_ASPECT_RATIO = 16f / 10f

/**
 * Shows an already-loaded country map as large as fits in the space it is given: up to
 * [MAX_WIDTH_FRACTION] of the width, but never taller than the available height. It is centred
 * in that space. The image has no background or border of its own: it carries its own.
 */
@Composable
fun BoundedCountryMap(
    bitmap: ImageBitmap?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val aspectRatio = bitmap?.let { it.width.toFloat() / it.height.toFloat() } ?: PLACEHOLDER_ASPECT_RATIO
        val mapWidth = minOf(maxWidth * MAX_WIDTH_FRACTION, maxHeight * aspectRatio)
        val shapedModifier = Modifier
            .width(mapWidth)
            .aspectRatio(aspectRatio)
            .clip(RoundedCornerShape(15.dp))
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }

        if (bitmap == null) {
            Box(modifier = shapedModifier, contentAlignment = Alignment.Center) {
                Text(
                    text = "No map data",
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.textSecondary,
                )
            }
        } else {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = shapedModifier,
            )
        }
    }
}
