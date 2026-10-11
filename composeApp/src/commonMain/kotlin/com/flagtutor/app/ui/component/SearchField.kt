package com.flagtutor.app.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.flagtutor.app.ui.theme.AppTheme

/**
 * A single-line text field for filtering a list. On the web this is backed by a real HTML input, because the
 * canvas based text input of Compose can't bring up the soft keyboard on mobile browsers.
 */
@Composable
expect fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
)

/** The [SearchField] built from regular Compose text input, for platforms where that works. */
@Composable
fun ComposeSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Filled.Close, contentDescription = "Clear search")
                }
            }
        },
        singleLine = true,
        shape = AppTheme.shapes.large,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AppTheme.colors.text,
            unfocusedTextColor = AppTheme.colors.text,
            focusedBorderColor = AppTheme.colors.primary,
            unfocusedBorderColor = AppTheme.colors.divider,
            focusedLeadingIconColor = AppTheme.colors.textSecondary,
            unfocusedLeadingIconColor = AppTheme.colors.textSecondary,
            focusedTrailingIconColor = AppTheme.colors.textSecondary,
            unfocusedTrailingIconColor = AppTheme.colors.textSecondary,
            focusedPlaceholderColor = AppTheme.colors.textSecondary,
            unfocusedPlaceholderColor = AppTheme.colors.textSecondary,
            cursorColor = AppTheme.colors.primary,
        ),
    )
}
