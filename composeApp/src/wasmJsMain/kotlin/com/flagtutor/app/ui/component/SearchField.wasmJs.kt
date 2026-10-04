package com.flagtutor.app.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.flagtutor.app.ui.theme.AppTheme
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import kotlin.math.roundToInt

private const val INPUT_CLASS = "app-search-input"

/**
 * Compose draws the field's chrome, while the text itself is a real HTML `<input>` laid over it: mobile browsers
 * only show the soft keyboard for a native element focused by a tap, which the Compose canvas can't provide.
 */
@Composable
actual fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier,
) {
    val density = LocalDensity.current.density
    val textColor = AppTheme.colors.text.toCss()
    val placeholderColor = AppTheme.colors.textSecondary.toCss()
    val caretColor = AppTheme.colors.primary.toCss()
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    var bounds by remember { mutableStateOf<Rect?>(null) }

    val input = remember {
        installPlaceholderStyle()
        (document.createElement("input") as HTMLInputElement).apply {
            className = INPUT_CLASS
            type = "text"
            setAttribute("enterkeyhint", "search")
            setAttribute("autocomplete", "off")
            setAttribute("autocapitalize", "off")
            setAttribute("spellcheck", "false")
            style.apply {
                position = "fixed"
                setProperty("border", "none")
                setProperty("outline", "none")
                setProperty("background", "transparent")
                setProperty("padding", "0")
                setProperty("margin", "0")
                setProperty("font-size", "16px") // 16px or more stops iOS Safari zooming in on focus.
                setProperty("font-family", "system-ui, -apple-system, Roboto, sans-serif")
                setProperty("z-index", "10")
            }
            addEventListener("input", { currentOnValueChange(this.value) })
        }
    }

    DisposableEffect(input) {
        // Not <body>: Compose attaches a shadow root to it, which stops any other children from rendering.
        document.documentElement?.appendChild(input)
        onDispose { input.remove() }
    }

    input.placeholder = placeholder
    input.setAttribute("aria-label", placeholder)
    if (input.value != value) input.value = value
    input.style.setProperty("color", textColor)
    input.style.setProperty("caret-color", caretColor)
    input.style.setProperty("--app-search-placeholder", placeholderColor)
    val rect = bounds
    if (rect == null) {
        input.style.setProperty("display", "none")
    } else {
        input.style.apply {
            setProperty("display", "block")
            setProperty("left", "${rect.left / density}px")
            setProperty("top", "${rect.top / density}px")
            setProperty("width", "${rect.width / density}px")
            setProperty("height", "${rect.height / density}px")
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(1.dp, AppTheme.colors.divider, AppTheme.shapes.large)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = AppTheme.colors.textSecondary,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .onGloballyPositioned { bounds = it.boundsInWindow() },
        )
        if (value.isNotEmpty()) {
            IconButton(onClick = { onValueChange("") }, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Clear search",
                    tint = AppTheme.colors.textSecondary,
                )
            }
        }
    }
}

private fun Color.toCss(): String =
    "rgba(${(red * 255).roundToInt()}, ${(green * 255).roundToInt()}, ${(blue * 255).roundToInt()}, $alpha)"

private fun installPlaceholderStyle() {
    val id = "app-search-input-style"
    if (document.getElementById(id) != null) return
    val style = document.createElement("style")
    style.id = id
    style.textContent = ".$INPUT_CLASS::placeholder { color: var(--app-search-placeholder); opacity: 1; }"
    document.head?.appendChild(style)
}
