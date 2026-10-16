package com.flagtutor.app.ui.feature.debug

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.flagtutor.app.ui.theme.AppTheme

/**
 * The app-specific entries of the debug page, passed as `extraContent` to the shared library's
 * `DebugPage` (which provides animation speed, logs and crashes itself).
 */
@Composable
fun ColumnScope.AppDebugLinks(
    onCountriesOverviewClick: () -> Unit,
    onExcludedCountriesClick: () -> Unit,
    onFlagAttemptsClick: () -> Unit,
) {
    DebugLink(Icons.Filled.List, "Countries Overview", onCountriesOverviewClick)
    DebugLink(Icons.Filled.Block, "Excluded Countries", onExcludedCountriesClick)
    DebugLink(Icons.Filled.Flag, "Flag Attempts", onFlagAttemptsClick)
}

@Composable
private fun DebugLink(icon: ImageVector, label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = AppTheme.shapes.large,
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
        Text(label)
    }
}
