package com.limbe.hexamusicplayer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkScheme = darkColorScheme(
    primary = NeonMint,
    onPrimary = InkBlack,
    primaryContainer = StormBlue,
    onPrimaryContainer = Ice,
    secondary = SolarAmber,
    onSecondary = InkBlack,
    tertiary = CoralPulse,
    onTertiary = InkBlack,
    background = NightInk,
    onBackground = Ice,
    surface = StormBlue,
    onSurface = Ice,
    surfaceVariant = RoyalBlue,
    onSurfaceVariant = Steel
)

private val LightScheme = lightColorScheme(
    primary = RoyalBlue,
    onPrimary = CloudWhite,
    primaryContainer = Ice,
    onPrimaryContainer = InkBlack,
    secondary = SolarAmber,
    onSecondary = InkBlack,
    tertiary = CoralPulse,
    onTertiary = InkBlack,
    background = CloudWhite,
    onBackground = InkBlack,
    surface = Ice,
    onSurface = InkBlack,
    surfaceVariant = Color(0xFFD9E5F8),
    onSurfaceVariant = Color(0xFF3A4A66)
)

@Composable
fun HexaMusicTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = HexaTypography,
        content = content
    )
}
