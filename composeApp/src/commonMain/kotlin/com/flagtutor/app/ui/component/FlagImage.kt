package com.flagtutor.app.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

/**
 * Displays the flag image at [flagUrl].
 */
@Composable
fun FlagImage(flagUrl: String, modifier: Modifier = Modifier, contentScale: ContentScale = ContentScale.Fit) {
    AsyncImage(
        model = flagUrl,
        contentDescription = null,
        contentScale = contentScale,
        modifier = modifier,
    )
}
