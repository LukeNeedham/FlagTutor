package com.flagtutor.app.ui.feature.debug

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.flagtutor.app.ui.theme.AppTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.flagtutor.app.domain.model.Country
import com.flagtutor.app.domain.util.GoogleMapsLinkBuilder
import com.flagtutor.app.ui.component.CountryMapHighlight
import com.flagtutor.app.ui.component.FlagImage
import com.flagtutor.app.ui.util.LocalScaledAnimation
import kotlin.math.round

private enum class DebugImageType { FLAG, MAP }

private data class EnlargedImage(val alpha2Code: String, val type: DebugImageType)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountriesOverviewPageContent(
    countries: List<Country>,
    attemptStatsByCountry: Map<String, FlagAttemptStats>,
    isLoading: Boolean,
    isError: Boolean,
    onMoreInfo: (String) -> Unit,
    onPlay: (String) -> Unit,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
) {
    var enlargedImage by remember { mutableStateOf<EnlargedImage?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val filteredCountries = remember(countries, searchQuery) {
        val query = searchQuery.trim()
        if (query.isEmpty()) countries else countries.filter { it.name.contains(query, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Countries Overview") },
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
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
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
                    Column(modifier = Modifier.fillMaxSize()) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            placeholder = { Text("Search countries") },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
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
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(filteredCountries, key = { it.alpha2Code }) { country ->
                                CountryOverviewRow(
                                    country = country,
                                    attemptStats = attemptStatsByCountry[country.alpha2Code],
                                    onMoreInfo = onMoreInfo,
                                    onPlay = onPlay,
                                    onFlagClick = {
                                        enlargedImage = EnlargedImage(country.alpha2Code, DebugImageType.FLAG)
                                    },
                                    onMapClick = {
                                        enlargedImage = EnlargedImage(country.alpha2Code, DebugImageType.MAP)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    enlargedImage?.let { image ->
        Dialog(
            onDismissRequest = { enlargedImage = null },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.95f)
                    .clickable { enlargedImage = null },
                contentAlignment = Alignment.Center,
            ) {
                when (image.type) {
                    DebugImageType.FLAG -> FlagImage(
                        alpha2Code = image.alpha2Code,
                        modifier = Modifier.fillMaxSize(),
                    )

                    DebugImageType.MAP -> CountryMapHighlight(
                        alpha2Code = image.alpha2Code,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun CountryOverviewRow(
    country: Country,
    attemptStats: FlagAttemptStats?,
    onMoreInfo: (String) -> Unit,
    onPlay: (String) -> Unit,
    onFlagClick: () -> Unit,
    onMapClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.card, AppTheme.shapes.medium)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FlagImage(
                alpha2Code = country.alpha2Code,
                modifier = Modifier
                    .height(40.dp)
                    .aspectRatio(3f / 2f)
                    .clickable(onClick = onFlagClick),
            )
            Text(
                text = country.name,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text,
                modifier = Modifier.weight(1f),
            )
            if (country.wikipediaUrl.isNotEmpty()) {
                IconButton(
                    onClick = { onMoreInfo(country.wikipediaUrl) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "More info",
                        tint = AppTheme.colors.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            IconButton(
                onClick = { onMoreInfo(GoogleMapsLinkBuilder.searchUrl(country.name)) },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = "Open in maps",
                    tint = AppTheme.colors.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
            IconButton(
                onClick = { onPlay(country.alpha2Code) },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Open in game",
                    tint = AppTheme.colors.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            var isExpanded by remember { mutableStateOf(false) }
            Text(
                text = country.flagDescription,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text,
                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .clickable { isExpanded = !isExpanded }
                    .animateContentSize(tween(LocalScaledAnimation.current.short)),
            )
            CountryMapHighlight(
                alpha2Code = country.alpha2Code,
                modifier = Modifier
                    .height(40.dp)
                    .aspectRatio(16f / 10f)
                    .clipToBounds()
                    .clickable(onClick = onMapClick),
            )
        }
        HorizontalDivider(color = AppTheme.colors.divider)
        if (attemptStats == null) {
            Text(
                text = "No attempts yet",
                style = AppTheme.typography.labelSmall,
                color = AppTheme.colors.textSecondary,
            )
        } else {
            Row(modifier = Modifier.fillMaxWidth()) {
                val totalGuesses = attemptStats.totalAttempts + attemptStats.totalIncorrectAnswers
                val accuracyPercent = round(attemptStats.totalAttempts * 100.0 / totalGuesses).toInt()
                StatItem("Attempts", attemptStats.totalAttempts.toString(), Modifier.weight(1f))
                StatItem("Incorrect", attemptStats.totalIncorrectAnswers.toString(), Modifier.weight(1f))
                StatItem("Accuracy", "$accuracyPercent%", Modifier.weight(1f))
                StatItem("Streak", attemptStats.currentStreak.toString(), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.text,
        )
        Text(
            text = label,
            style = AppTheme.typography.labelSmall,
            color = AppTheme.colors.textSecondary,
        )
    }
}
