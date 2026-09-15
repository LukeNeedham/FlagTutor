package com.flagtutor.app.ui.component

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity

@Composable
fun FlagImage(alpha2Code: String, modifier: Modifier = Modifier, contentScale: ContentScale = ContentScale.Fit) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val sizePx = minOf(constraints.maxWidth, constraints.maxHeight).coerceAtLeast(1)
        val fontSize = with(LocalDensity.current) { (sizePx * 0.65f).toSp() }
        Text(
            text = alpha2ToFlagEmoji(alpha2Code),
            fontSize = fontSize,
        )
    }
}

private fun alpha2ToFlagEmoji(code: String): String =
    code.uppercase().map { c -> String(Character.toChars(0x1F1E6 + (c.code - 'A'.code))) }.joinToString("")
