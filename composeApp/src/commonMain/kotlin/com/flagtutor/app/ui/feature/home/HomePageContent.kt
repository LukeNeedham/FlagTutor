package com.flagtutor.app.ui.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import com.flagtutor.app.ui.theme.AppTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flagtutor.app.ui.component.AppLogo

@Composable
fun HomePageContent(
    title: String,
    subtitle: String,
    onGuessCountryClick: () -> Unit,
    onCreditsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onDebugClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppTheme.colors.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                AppLogo(size = 112.dp)
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = title,
                    style = AppTheme.typography.displaySmall,
                    color = AppTheme.colors.onBackground,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    style = AppTheme.typography.bodyLarge,
                    color = AppTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }

            Button(
                onClick = onGuessCountryClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = AppTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.onBackground,
                    contentColor = AppTheme.colors.background,
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            ) {
                Icon(imageVector = Icons.Filled.Public, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Guess Country", style = AppTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = AppTheme.shapes.large,
                colors = ButtonDefaults.textButtonColors(contentColor = AppTheme.colors.onBackground),
            ) {
                Icon(imageVector = Icons.Filled.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Settings", style = AppTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                onClick = onCreditsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = AppTheme.shapes.large,
                colors = ButtonDefaults.textButtonColors(contentColor = AppTheme.colors.onBackground),
            ) {
                Icon(imageVector = Icons.Filled.Info, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Credits", style = AppTheme.typography.titleMedium)
            }
            if (onDebugClick != null) {
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = onDebugClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = AppTheme.shapes.large,
                    colors = ButtonDefaults.textButtonColors(contentColor = AppTheme.colors.onBackground),
                ) {
                    Icon(imageVector = Icons.Filled.Build, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Debug", style = AppTheme.typography.titleMedium)
                }
            }
        }
    }
}
