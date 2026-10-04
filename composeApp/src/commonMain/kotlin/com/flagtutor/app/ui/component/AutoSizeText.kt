package com.flagtutor.app.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

private const val SHRINK_FACTOR = 0.92f

/**
 * Text that always stays on one line: it starts at [style]'s font size and shrinks in steps until
 * it fits the width it is given (never below [minFontSize]). It is not drawn until it has settled,
 * so no oversized frame shows.
 */
@Composable
fun AutoSizeText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    minFontSize: TextUnit = 12.sp,
) {
    var fontSize by remember(text, style) { mutableStateOf(style.fontSize) }
    var isSettled by remember(text, style) { mutableStateOf(false) }
    Box(modifier = modifier) {
        Text(
            text = text,
            style = style,
            color = color,
            fontSize = fontSize,
            maxLines = 1,
            softWrap = false,
            onTextLayout = { result ->
                if (result.didOverflowWidth && fontSize > minFontSize) {
                    val shrunk = fontSize * SHRINK_FACTOR
                    fontSize = if (shrunk < minFontSize) minFontSize else shrunk
                } else {
                    isSettled = true
                }
            },
            modifier = Modifier.drawWithContent { if (isSettled) drawContent() },
        )
    }
}
