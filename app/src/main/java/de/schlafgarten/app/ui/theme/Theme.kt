package de.schlafgarten.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Green = Color(0xFF4CAF6D)
private val GreenContainer = Color(0xFFA6E6BB)
private val DarkBackground = Color(0xFF101916)
private val DarkSurface = Color(0xFF18241F)
private val DarkSurfaceVariant = Color(0xFF22312A)

private val DarkColors = darkColorScheme(
    primary = Green,
    onPrimary = Color(0xFF06210F),
    primaryContainer = Color(0xFF1E3D2B),
    onPrimaryContainer = GreenContainer,
    secondary = Color(0xFF8FCB6B),
    background = DarkBackground,
    onBackground = Color(0xFFE2E9E4),
    surface = DarkSurface,
    onSurface = Color(0xFFE2E9E4),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFA9B5AD),
    outline = Color(0xFF4C5A52),
    error = Color(0xFFFF7B72),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF2E7D4F),
    onPrimary = Color.White,
    primaryContainer = GreenContainer,
    onPrimaryContainer = Color(0xFF06210F),
    background = Color(0xFFFAFDF7),
    onBackground = Color(0xFF17201B),
    surface = Color.White,
    onSurface = Color(0xFF17201B),
    surfaceVariant = Color(0xFFE2E9E4),
    onSurfaceVariant = Color(0xFF3F4A43),
    outline = Color(0xFF9AA69E),
    error = Color(0xFFBA1A1A),
)

@Composable
fun SchlafgartenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
