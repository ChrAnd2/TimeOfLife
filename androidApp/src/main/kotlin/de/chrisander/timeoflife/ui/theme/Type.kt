package de.chrisander.timeoflife.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import de.chrisander.timeoflife.R

val Outfit = FontFamily(
    Font(R.font.outfit_light, FontWeight.Light),
    Font(R.font.outfit_regular, FontWeight.Normal),
    Font(R.font.outfit_medium, FontWeight.Medium),
    Font(R.font.outfit_semibold, FontWeight.SemiBold),
    Font(R.font.outfit_bold, FontWeight.Bold),
)

private val defaults = Typography()

private fun TextStyle.branded(weight: FontWeight? = fontWeight) =
    copy(fontFamily = Outfit, fontWeight = weight)

internal val BrandTypography = Typography(
    displayLarge = defaults.displayLarge.branded(FontWeight.SemiBold),
    displayMedium = defaults.displayMedium.branded(FontWeight.SemiBold),
    displaySmall = defaults.displaySmall.branded(FontWeight.SemiBold),
    headlineLarge = defaults.headlineLarge.branded(FontWeight.SemiBold),
    headlineMedium = defaults.headlineMedium.branded(FontWeight.SemiBold),
    headlineSmall = defaults.headlineSmall.branded(FontWeight.SemiBold),
    titleLarge = defaults.titleLarge.branded(FontWeight.SemiBold),
    titleMedium = defaults.titleMedium.branded(),
    titleSmall = defaults.titleSmall.branded(),
    bodyLarge = defaults.bodyLarge.branded(),
    bodyMedium = defaults.bodyMedium.branded(),
    bodySmall = defaults.bodySmall.branded(),
    labelLarge = defaults.labelLarge.branded(FontWeight.SemiBold),
    labelMedium = defaults.labelMedium.branded(),
    labelSmall = defaults.labelSmall.branded(),
)
