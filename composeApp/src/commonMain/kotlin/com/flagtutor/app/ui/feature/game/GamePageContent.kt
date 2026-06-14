package com.flagtutor.app.ui.feature.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.flagtutor.app.domain.model.Country
import com.flagtutor.app.ui.component.FlagImage
import com.flagtutor.app.ui.feature.game.component.FlagOptionButton

@Composable
fun GamePageContent(
    flag: Country,
    options: List<Country>,
    incorrectAlpha2Codes: Set<String>,
    isAnswerRevealed: Boolean,
    onOptionSelected: (Country) -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Which country's flag is this?",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(24.dp))
            FlagImage(
                alpha2Code = flag.alpha2Code,
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape),
            )
            Spacer(modifier = Modifier.height(32.dp))
            options.forEach { country ->
                // Keyed so each option gets a fresh crumble animation state when the flag changes.
                key(country.alpha2Code) {
                    FlagOptionButton(
                        country = country,
                        isCorrectAnswer = isAnswerRevealed && country.alpha2Code == flag.alpha2Code,
                        isCrumbled = country.alpha2Code in incorrectAlpha2Codes,
                        enabled = !isAnswerRevealed && country.alpha2Code !in incorrectAlpha2Codes,
                        onClick = { onOptionSelected(country) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}
