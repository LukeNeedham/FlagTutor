package com.flagtutor.app.ui.feature.credits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.flagtutor.app.ui.theme.AppTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private const val COMMONS_LICENSING_URL = "https://commons.wikimedia.org/wiki/Commons:Licensing"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditsPageContent(
    onLinkClick: (String) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Credits") },
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
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Country maps",
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.onBackground,
            )
            Text(
                text = "Each country's map image is sourced from its Wikipedia article and hosted on " +
                    "Wikimedia Commons. These images are created by Wikipedia contributors and are " +
                    "licensed under Creative Commons Attribution-ShareAlike or the GNU Free " +
                    "Documentation License.",
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.textSecondary,
            )
            Text(
                text = "For the author and full license of a specific map, open that country's " +
                    "Wikipedia page (available from its flag screen) and view the image's file " +
                    "description page.",
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.textSecondary,
            )
            TextButton(onClick = { onLinkClick(COMMONS_LICENSING_URL) }) {
                Text(
                    text = "commons.wikimedia.org/wiki/Commons:Licensing",
                    style = AppTheme.typography.bodyMedium,
                )
            }
        }
    }
}
