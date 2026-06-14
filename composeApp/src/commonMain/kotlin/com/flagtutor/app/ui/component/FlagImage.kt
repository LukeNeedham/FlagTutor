package com.flagtutor.app.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Displays the round flag icon for the given [alpha2Code] (ISO 3166-1 alpha-2, e.g. "fr").
 */
@Composable
expect fun FlagImage(alpha2Code: String, modifier: Modifier = Modifier)
