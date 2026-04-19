package com.line98.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.line98.game.core.GameMode
import com.line98.game.data.UserSettings

@Composable
fun MenuScreen(
    settings: UserSettings,
    onStartClassic: () -> Unit,
    onStartPowerUp: () -> Unit,
    onSettings: () -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Column(
                modifier = Modifier.widthIn(max = 420.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Line 98",
                    style = MaterialTheme.typography.displayMedium,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Classic best: ${settings.classicHighScore}",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Power-up best: ${settings.powerUpHighScore}",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Last mode: ${settings.lastMode.displayName()}",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onStartClassic,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Start Classic mode" },
                ) {
                    Text("Classic")
                }
                Button(
                    onClick = onStartPowerUp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Start Power-up mode" },
                ) {
                    Text("Power-up")
                }
                OutlinedButton(
                    onClick = onSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Open settings" },
                ) {
                    Text("Settings")
                }
            }
        }
    }
}

private fun GameMode.displayName(): String =
    when (this) {
        GameMode.Classic -> "Classic"
        GameMode.PowerUp -> "Power-up"
    }
