package com.line98.game.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CleanArcadeColors = darkColorScheme(
    primary = Color(0xFF35C2A1),
    secondary = Color(0xFFFFCE47),
    tertiary = Color(0xFFE84A5F),
    background = Color(0xFF101114),
    surface = Color(0xFF181A1F),
    onPrimary = Color(0xFF061512),
    onSecondary = Color(0xFF1D1700),
    onTertiary = Color.White,
    onBackground = Color(0xFFF7F7F2),
    onSurface = Color(0xFFF7F7F2),
)

@Composable
fun Line98Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CleanArcadeColors,
        content = content,
    )
}
