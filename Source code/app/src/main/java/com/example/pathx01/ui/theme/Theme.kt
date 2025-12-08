package com.example.pathx01.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    tertiary = DarkAccent,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurface,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = White,
    onSurface = White,
    onSurfaceVariant = Cream,
    error = Error,
    primaryContainer = DarkPrimary,
    secondaryContainer = DarkSecondary,
    tertiaryContainer = DarkAccent,
    onPrimaryContainer = White,
    onSecondaryContainer = DarkBackground,
    onTertiaryContainer = DarkBackground
)

private val LightColorScheme = lightColorScheme(
    primary = DarkGreen,
    secondary = Gold,
    tertiary = WarmOrange,
    background = AppBackground,
    surface = BackgroundCard,
    surfaceVariant = BackgroundSecondary,
    onPrimary = White,
    onSecondary = DarkGreen,
    onTertiary = DarkGreen,
    onBackground = Black,
    onSurface = Black,
    onSurfaceVariant = DarkGray,
    error = Error,
    primaryContainer = Peach,
    secondaryContainer = Cream,
    tertiaryContainer = LightBeige,
    onPrimaryContainer = DarkGreen,
    onSecondaryContainer = DarkGreen,
    onTertiaryContainer = DarkGreen
)

@Composable
fun PathXTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled to use our custom theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}