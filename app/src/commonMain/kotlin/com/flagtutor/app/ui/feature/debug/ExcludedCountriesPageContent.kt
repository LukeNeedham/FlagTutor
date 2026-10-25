package com.flagtutor.app.ui.feature.debug

import com.flagtutor.app.ui.theme.FlagTutorTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flagtutor.app.ui.theme.AppTheme

/** Lists the countries that have an ISO 3166-1 code but that the app leaves out: no official flag, or the parent country's flag. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExcludedCountriesPageContent(
    countries: List<ExcludedCountry>,
    isLoading: Boolean,
    isError: Boolean,
    onMoreInfo: (String) -> Unit,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Excluded Countries") },
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
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(color = AppTheme.colors.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading countries…",
                            style = AppTheme.typography.bodyLarge,
                            color = AppTheme.colors.textSecondary,
                        )
                    }
                }

                isError -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WifiOff,
                            contentDescription = null,
                            tint = AppTheme.colors.error,
                            modifier = Modifier.height(48.dp),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Couldn't load countries.",
                            style = AppTheme.typography.bodyLarge,
                            color = AppTheme.colors.textSecondary,
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onRetry, shape = AppTheme.shapes.large) {
                            Icon(imageVector = Icons.Filled.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(countries, key = { it.country.alpha2Code }) { excluded ->
                            ExcludedCountryRow(excluded = excluded, onMoreInfo = onMoreInfo)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExcludedCountryRow(excluded: ExcludedCountry, onMoreInfo: (String) -> Unit) {
    val country = excluded.country
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.card, AppTheme.shapes.medium)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = country.name,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = country.alpha2Code.uppercase(),
                style = AppTheme.typography.labelSmall,
                color = AppTheme.colors.textSecondary,
            )
            if (country.flagWikipediaUrl.isNotEmpty()) {
                IconButton(
                    onClick = { onMoreInfo(country.flagWikipediaUrl) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Flag article",
                        tint = AppTheme.colors.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
        Text(
            text = excluded.reason,
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.text,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    FlagTutorTheme {
        ExcludedCountriesPageContent(
            countries = emptyList(),
            isLoading = true,
            isError = false,
            onMoreInfo = {},
            onRetry = {},
            onBackClick = {},
        )
    }
}
