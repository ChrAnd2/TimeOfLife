package de.chrisander.timeoflife.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import de.chrisander.timeoflife.brand.BrandPalette

internal val LightColors = lightColorScheme(
    primary = Color(0xFF00796E),
    onPrimary = Color(BrandPalette.WHITE),
    primaryContainer = Color(0xFFC9F5EC),
    onPrimaryContainer = Color(0xFF00201C),
    secondary = Color(0xFFB34A00),
    onSecondary = Color(BrandPalette.WHITE),
    secondaryContainer = Color(0xFFFFE2CC),
    onSecondaryContainer = Color(0xFF3A1500),
    tertiary = Color(0xFF7A5C00),
    onTertiary = Color(BrandPalette.WHITE),
    tertiaryContainer = Color(0xFFFFEFB3),
    onTertiaryContainer = Color(0xFF261A00),
    background = Color(BrandPalette.MIST),
    onBackground = Color(BrandPalette.INK),
    surface = Color(BrandPalette.MIST),
    onSurface = Color(BrandPalette.INK),
    surfaceVariant = Color(0xFFE1EAEA),
    onSurfaceVariant = Color(0xFF4A6266),
    surfaceContainerLowest = Color(BrandPalette.WHITE),
    surfaceContainerLow = Color(0xFFEEF3F3),
    surfaceContainer = Color(0xFFE8EEEE),
    surfaceContainerHigh = Color(0xFFE2E9E9),
    surfaceContainerHighest = Color(0xFFDCE4E4),
    outline = Color(0xFF74898C),
    outlineVariant = Color(0xFFC6D3D1),
    error = Color(0xFFBA1A1A),
    onError = Color(BrandPalette.WHITE),
)

internal val DarkColors = darkColorScheme(
    primary = Color(BrandPalette.TEAL_LIGHT),
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF00504A),
    onPrimaryContainer = Color(0xFFC9F5EC),
    secondary = Color(0xFFFFA366),
    onSecondary = Color(0xFF4A1F00),
    secondaryContainer = Color(0xFF7A3300),
    onSecondaryContainer = Color(0xFFFFE2CC),
    tertiary = Color(0xFFFFD54F),
    onTertiary = Color(0xFF3D2E00),
    tertiaryContainer = Color(0xFF5C4500),
    onTertiaryContainer = Color(0xFFFFEFB3),
    background = Color(BrandPalette.DEEP),
    onBackground = Color(0xFFE3F1EF),
    surface = Color(BrandPalette.DEEP),
    onSurface = Color(0xFFE3F1EF),
    surfaceVariant = Color(BrandPalette.FUTURE),
    onSurfaceVariant = Color(0xFFA7C2C5),
    surfaceContainerLowest = Color(0xFF061214),
    surfaceContainerLow = Color(0xFF0E2023),
    surfaceContainer = Color(BrandPalette.PANEL),
    surfaceContainerHigh = Color(BrandPalette.PANEL_HIGH),
    surfaceContainerHighest = Color(BrandPalette.FUTURE),
    outline = Color(0xFF75908F),
    outlineVariant = Color(BrandPalette.RIM),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

/** Cards are white on the light background and a raised panel on the dark one. */
val ColorScheme.card: Color
    get() = if (background.luminance() > HALF_LUMINANCE) surfaceContainerLowest else surfaceContainer

private const val HALF_LUMINANCE = 0.5f
