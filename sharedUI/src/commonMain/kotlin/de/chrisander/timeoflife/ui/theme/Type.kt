package de.chrisander.timeoflife.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import de.chrisander.timeoflife.resources.Res
import de.chrisander.timeoflife.resources.outfit_bold
import de.chrisander.timeoflife.resources.outfit_light
import de.chrisander.timeoflife.resources.outfit_medium
import de.chrisander.timeoflife.resources.outfit_regular
import de.chrisander.timeoflife.resources.outfit_semibold
import org.jetbrains.compose.resources.Font

@Composable
internal fun outfit() = FontFamily(
    Font(Res.font.outfit_light, FontWeight.Light),
    Font(Res.font.outfit_regular, FontWeight.Normal),
    Font(Res.font.outfit_medium, FontWeight.Medium),
    Font(Res.font.outfit_semibold, FontWeight.SemiBold),
    Font(Res.font.outfit_bold, FontWeight.Bold),
)

@Composable
internal fun brandTypography(): Typography {
    val outfit = outfit()
    val defaults = Typography()
    fun TextStyle.branded(weight: FontWeight? = fontWeight) =
        copy(fontFamily = outfit, fontWeight = weight)
    return Typography(
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
}
