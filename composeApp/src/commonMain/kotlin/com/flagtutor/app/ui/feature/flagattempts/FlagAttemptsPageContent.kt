package com.flagtutor.app.ui.feature.flagattempts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.flagtutor.app.ui.theme.AppTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flagtutor.app.domain.model.FlagAttempt
import com.flagtutor.app.ui.util.formatTimestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlagAttemptsPageContent(
    attempts: List<FlagAttempt>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flag Attempts (${attempts.size})") },
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
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = AppTheme.colors.primary)
                }
            }

            attempts.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No flag attempts recorded",
                        style = AppTheme.typography.bodyLarge,
                        color = AppTheme.colors.textSecondary,
                    )
                }
            }

            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    item { FlagAttemptHeaderRow() }
                    item { HorizontalDivider(color = AppTheme.colors.divider) }
                    items(attempts) { attempt ->
                        FlagAttemptRow(attempt)
                        HorizontalDivider(color = AppTheme.colors.divider)
                    }
                }
            }
        }
    }
}

@Composable
private fun FlagAttemptHeaderRow() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Country",
            style = AppTheme.typography.labelMedium,
            color = AppTheme.colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "Guesses",
            style = AppTheme.typography.labelMedium,
            color = AppTheme.colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "Timestamp",
            style = AppTheme.typography.labelMedium,
            color = AppTheme.colors.textSecondary,
            modifier = Modifier.weight(2f),
        )
    }
}

@Composable
private fun FlagAttemptRow(attempt: FlagAttempt) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = attempt.alpha2Code.uppercase(),
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.text,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = attempt.guessCount.toString(),
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.text,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = formatTimestamp(attempt.timestamp),
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.textSecondary,
            modifier = Modifier.weight(2f),
        )
    }
}
