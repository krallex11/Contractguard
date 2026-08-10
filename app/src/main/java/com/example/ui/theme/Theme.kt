package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SleekDarkColorScheme = darkColorScheme(
    primary = SleekLimeGreenPrimary,
    onPrimary = SleekLimeGreenOnPrimary,
    primaryContainer = SleekLimeGreenContainer,
    onPrimaryContainer = SleekLimeGreenPrimary,
    secondary = SleekBluePrimary,
    onSecondary = Color.Black,
    tertiary = SleekGreenSecure,
    background = SleekDarkBackground,
    onBackground = SleekTextWhite,
    surface = SleekDarkSurface,
    onSurface = SleekTextWhite,
    surfaceVariant = SleekDarkSurfaceVariant,
    onSurfaceVariant = SleekTextMuted,
    outline = SleekBorderOutline,
    error = SleekRedAlert
)

@Composable
fun ContractGuardTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SleekDarkColorScheme,
        typography = Typography,
        content = content
    )
}

