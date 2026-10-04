package com.flagtutor.app.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier,
) = ComposeSearchField(value, onValueChange, placeholder, modifier)
