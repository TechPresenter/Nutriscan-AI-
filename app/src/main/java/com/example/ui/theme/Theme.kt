package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkEmeraldPrimary,
    secondary = DarkTealSecondary,
    tertiary = DarkOrangeHighlight,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    primaryContainer = Color(0xFF064E3B), // Deep Forest Emerald
    onPrimaryContainer = Color(0xFFD1FAE5), // Soft Mint
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = DarkOnSurfaceVariant,
    outlineVariant = Color(0xFF334155),
    error = Color(0xFFF87171)
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    secondary = TealSecondary,
    tertiary = OrangeHighlight,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,
    primaryContainer = Color(0xFFD1FAE5), // Soft Mint Container
    onPrimaryContainer = Color(0xFF065F46), // Dark Emerald Forest
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = LightOnSurfaceVariant,
    outlineVariant = Color(0xFFE2E8F0),
    error = Color(0xFFEF4444)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
