package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SasaPrimary,
    onPrimary = Color.White,
    primaryContainer = SasaPrimaryContainer,
    secondary = SasaSecondary,
    onSecondary = Color.Black,
    secondaryContainer = SasaSecondaryContainer,
    background = SasaDarkBackground,
    onBackground = SasaTextPrimary,
    surface = SasaDarkSurface,
    onSurface = SasaTextPrimary,
    surfaceVariant = SasaCardBackground,
    onSurfaceVariant = SasaTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = SasaPrimary,
    onPrimary = Color.White,
    primaryContainer = SasaPrimaryContainer,
    secondary = SasaSecondary,
    background = Color(0xFFF6F5FB),
    onBackground = Color(0xFF1B1926),
    surface = Color.White,
    onSurface = Color(0xFF1B1926)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to futuristic dark AI theme
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

