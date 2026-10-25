package com.flagtutor.app.ui.feature.debug

import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.flagtutor.app.ui.theme.AppTheme

/**
 * The app's own debug page: the debug tools that are specific to FlagTutor, and a button that opens
 * the shared library's debug page (animation speed, logs, crashes). The library's page has no slot
 * for app-specific tools, so they live here.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppDebugPageContent(
    onOpenSdkDebug: () -> Unit,
    onCountriesOverviewClick: () -> Unit,
    onExcludedCountriesClick: () -> Unit,
    onFlagAttemptsClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debug") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.background,
                ),
            )
        },
        containerColor = AppTheme.colors.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DebugLink(Icons.Filled.BugReport, "SDK debug (speed, logs, crashes)", onOpenSdkDebug)
            DebugLink(Icons.Filled.List, "Countries Overview", onCountriesOverviewClick)
            DebugLink(Icons.Filled.Block, "Excluded Countries", onExcludedCountriesClick)
            DebugLink(Icons.Filled.Flag, "Flag Attempts", onFlagAttemptsClick)
        }
    }
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

@Preview
@Composable
private fun Preview() {
    AppTheme {
        AppDebugPageContent(
            onOpenSdkDebug = {},
            onCountriesOverviewClick = {},
            onExcludedCountriesClick = {},
            onFlagAttemptsClick = {},
            onBackClick = {},
        )
    }
}
