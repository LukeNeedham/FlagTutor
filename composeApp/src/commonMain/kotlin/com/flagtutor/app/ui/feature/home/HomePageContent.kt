package com.flagtutor.app.ui.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
    onGuessTheFlagClick: () -> Unit,
    onAboutClick: () -> Unit,
    onDebugClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
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
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onGuessCountryClick,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    shape = MaterialTheme.shapes.large,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                ) {
                    Icon(imageVector = Icons.Filled.Public, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Guess Country", style = MaterialTheme.typography.titleMedium)
                }
                Button(
                    onClick = onGuessTheFlagClick,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    shape = MaterialTheme.shapes.large,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                ) {
                    Icon(imageVector = Icons.Filled.Flag, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Guess Flag", style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onAboutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large,
            ) {
                Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "About", style = MaterialTheme.typography.titleMedium)
            }
            if (onDebugClick != null) {
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = onDebugClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Icon(imageVector = Icons.Filled.Build, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Debug", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
