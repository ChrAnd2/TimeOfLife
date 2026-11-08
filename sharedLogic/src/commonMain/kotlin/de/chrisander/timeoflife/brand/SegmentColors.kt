package de.chrisander.timeoflife.brand

import de.chrisander.timeoflife.domain.model.activity.Activity
import de.chrisander.timeoflife.domain.model.activity.DefaultActivityType
import de.chrisander.timeoflife.domain.model.progress.LifeSegment

object SegmentColors {

    private val customActivityPalette = longArrayOf(
        0xFF8EF0E2,
        0xFFFFC08F,
        0xFFFFE48A,
        0xFFB6EE93,
        0xFF9ADDFA,
        0xFFFFADC0,
    )

    fun of(segment: LifeSegment): Long = when (segment) {
        is LifeSegment.ActivitySegment -> of(segment.activity)
        is LifeSegment.FreeTime -> BrandPalette.LILAC
    }

    fun of(activity: Activity): Long = when (activity) {
        is Activity.DefaultActivity -> when (activity.type) {
            DefaultActivityType.SLEEP -> BrandPalette.TEAL
            DefaultActivityType.WORK -> BrandPalette.ORANGE
            DefaultActivityType.EDUCATION -> BrandPalette.SUN
            DefaultActivityType.COMMUTE -> BrandPalette.SAGE
            DefaultActivityType.EATING -> BrandPalette.GREEN
            DefaultActivityType.HOUSEHOLD -> BrandPalette.SKY
            DefaultActivityType.PERSONAL_CARE -> BrandPalette.ROSE
        }

        is Activity.CustomActivity ->
            customActivityPalette[activity.id.value.hashCode().mod(customActivityPalette.size)]
    }
}
