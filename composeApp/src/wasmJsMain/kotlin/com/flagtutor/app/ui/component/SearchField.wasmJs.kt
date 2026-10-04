package com.flagtutor.app.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Experiment: use Compose's own text input on web, instead of the HTML <input> overlay that was used before.
@Composable
actual fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier,
) = ComposeSearchField(value, onValueChange, placeholder, modifier)
