package com.flagtutor.app.ui.component

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.lukeneedham.circleflagsandroid.FlagProvider

@Composable
actual fun FlagImage(alpha2Code: String, modifier: Modifier) {
    val context = LocalContext.current
    val resId = FlagProvider.getFlagResIdFromCountryAlpha2Code(alpha2Code, context)
        .takeIf { it != 0 }
        ?: FlagProvider.getUnknownFlagResId()

    Image(
        painter = painterResource(id = resId),
        contentDescription = null,
        modifier = modifier,
    )
}
