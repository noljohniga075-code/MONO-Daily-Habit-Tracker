package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MonoColorScheme = darkColorScheme(
    primary = MonoTextPrimary,
    onPrimary = MonoDarkBackground,
    primaryContainer = MonoCardSurfaceElevated,
    onPrimaryContainer = MonoTextPrimary,
    secondary = MonoSilver,
    onSecondary = MonoDarkBackground,
    secondaryContainer = MonoCardBorder,
    onSecondaryContainer = MonoTextSecondary,
    tertiary = MonoTextSecondary,
    onTertiary = MonoDarkBackground,
    background = MonoDarkBackground,
    onBackground = MonoTextPrimary,
    surface = MonoCardSurface,
    onSurface = MonoTextPrimary,
    surfaceVariant = MonoCardSurfaceElevated,
    onSurfaceVariant = MonoTextSecondary,
    outline = MonoCardBorder,
    outlineVariant = MonoCardBorderActive
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // MONO strictly adheres to its signature luxury monochrome aesthetic
    MaterialTheme(
        colorScheme = MonoColorScheme,
        typography = Typography,
        content = content
    )
}
