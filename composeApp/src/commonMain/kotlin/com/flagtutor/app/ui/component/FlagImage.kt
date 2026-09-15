package com.flagtutor.app.ui.component

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.flagtutor.app.ui.util.decodeImageBitmap
import flagtutor.composeapp.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun FlagImage(alpha2Code: String, modifier: Modifier = Modifier, contentScale: ContentScale = ContentScale.Fit) {
    var bitmap by remember(alpha2Code) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(alpha2Code) {
        bitmap = withContext(Dispatchers.Default) {
            val bytes = Res.readBytes("files/flags/$alpha2Code.png")
            decodeImageBitmap(bytes)
        }
    }

    bitmap?.let {
        Image(
            bitmap = it,
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier,
        )
    }
}
