package de.chrisander.timeoflife.widget

import de.chrisander.timeoflife.domain.model.activity.Activity
import de.chrisander.timeoflife.domain.model.activity.DefaultActivityType
import de.chrisander.timeoflife.ui.theme.BrandPalette
import de.chrisander.timeoflife.widget.domain.model.LifeSegment

internal object WidgetColors {
    const val FACE = BrandPalette.PANEL.toInt()
    const val RIM = BrandPalette.RIM.toInt()
    const val FUTURE = BrandPalette.FUTURE.toInt()
    const val FREE_TIME = BrandPalette.LILAC.toInt()
    const val TEXT = BrandPalette.WHITE.toInt()
    const val TEXT_DIM = 0x99FFFFFF.toInt()
    const val HAND = BrandPalette.WHITE.toInt()

    private val customActivityPalette = intArrayOf(
        0xFF8EF0E2.toInt(),
        0xFFFFC08F.toInt(),
        0xFFFFE48A.toInt(),
        0xFFB6EE93.toInt(),
        0xFF9ADDFA.toInt(),
        0xFFFFADC0.toInt(),
    )

    fun of(segment: LifeSegment): Int = when (segment) {
        is LifeSegment.ActivitySegment -> of(segment.activity)
        is LifeSegment.FreeTime -> FREE_TIME
    }

    private fun of(activity: Activity): Int = when (activity) {
        is Activity.DefaultActivity -> when (activity.type) {
            DefaultActivityType.SLEEP -> BrandPalette.TEAL.toInt()
            DefaultActivityType.WORK -> BrandPalette.ORANGE.toInt()
            DefaultActivityType.EDUCATION -> BrandPalette.SUN.toInt()
            DefaultActivityType.COMMUTE -> BrandPalette.SAGE.toInt()
            DefaultActivityType.EATING -> BrandPalette.GREEN.toInt()
            DefaultActivityType.HOUSEHOLD -> BrandPalette.SKY.toInt()
            DefaultActivityType.PERSONAL_CARE -> BrandPalette.ROSE.toInt()
        }
        is Activity.CustomActivity ->
            customActivityPalette[Math.floorMod(
                activity.id.value.hashCode(),
                customActivityPalette.size
            )]
    }
}
