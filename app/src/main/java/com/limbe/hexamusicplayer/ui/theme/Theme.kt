package com.limbe.hexamusicplayer.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.limbe.hexamusicplayer.domain.model.DarkModeMode

private val DarkScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = PureWhite,
    primaryContainer = DarkElevatedSurface,
    onPrimaryContainer = TextPrimaryDark,
    secondary = AccentAmber,
    onSecondary = PureBlack,
    tertiary = AccentCoral,
    onTertiary = PureWhite,
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkElevatedSurface,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkBorder
)

private val LightScheme = lightColorScheme(
    primary = AccentBlue,
    onPrimary = PureWhite,
    primaryContainer = LightElevatedSurface,
    onPrimaryContainer = TextPrimaryLight,
    secondary = AccentAmber,
    onSecondary = PureWhite,
    tertiary = AccentCoral,
    onTertiary = PureWhite,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightElevatedSurface,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightBorder
)

@Composable
fun HexaMusicTheme(
    darkModeMode: DarkModeMode = DarkModeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (darkModeMode) {
        DarkModeMode.SYSTEM -> isSystemInDarkTheme()
        DarkModeMode.LIGHT -> false
        DarkModeMode.DARK -> true
    }
    val colorScheme = if (darkTheme) DarkScheme else LightScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HexaTypography,
        content = content
    )
}
