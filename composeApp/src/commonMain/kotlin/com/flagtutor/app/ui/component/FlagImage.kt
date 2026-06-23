package com.flagtutor.app.ui.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun FlagImage(flagUrl: String, modifier: Modifier = Modifier, contentScale: ContentScale = ContentScale.Fit) {
    AsyncImage(
        model = flagUrl,
        contentDescription = null,
        contentScale = contentScale,
        modifier = modifier.clip(RoundedCornerShape(5.dp)),
    )
}
