package com.flagtutor.app.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.flagtutor.app.ui.util.decodeImageBitmap
import flagtutor.composeapp.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun CountryMapHighlight(
    alpha2Code: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    var bitmap by remember(alpha2Code) { mutableStateOf<ImageBitmap?>(null) }
    var notFound by remember(alpha2Code) { mutableStateOf(false) }

    LaunchedEffect(alpha2Code) {
        bitmap = null
        notFound = false
        try {
            bitmap = withContext(Dispatchers.Default) {
                val bytes = Res.readBytes("files/maps/${alpha2Code.lowercase()}.png")
                decodeImageBitmap(bytes)
            }
        } catch (_: Exception) {
            notFound = true
        }
    }

    val bmp = bitmap
    val aspectRatio = bmp?.let { it.width.toFloat() / it.height.toFloat() } ?: (16f / 10f)
    val shapedModifier = modifier
        .aspectRatio(aspectRatio)
        .clip(RoundedCornerShape(10.dp))
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }

    when {
        notFound -> Box(modifier = shapedModifier, contentAlignment = Alignment.Center) {
            Text(
                text = "No map data",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        bmp != null -> Image(
            bitmap = bmp,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = shapedModifier,
        )
        else -> Box(modifier = shapedModifier)
    }
}
