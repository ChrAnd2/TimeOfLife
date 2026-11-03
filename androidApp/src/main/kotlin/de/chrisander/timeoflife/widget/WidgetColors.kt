package de.chrisander.timeoflife.widget

import de.chrisander.timeoflife.domain.model.activity.Activity
import de.chrisander.timeoflife.domain.model.activity.DefaultActivityType
import de.chrisander.timeoflife.widget.domain.model.LifeSegment

internal object WidgetColors {
    const val FACE = 0xFF1C1D22.toInt()
    const val RIM = 0xFF2E3038.toInt()
    const val FUTURE = 0xFF2A2C33.toInt()
    const val FREE_TIME = 0xFF8E939E.toInt()
    const val TEXT = 0xFFFFFFFF.toInt()
    const val TEXT_DIM = 0x99FFFFFF.toInt()
    const val HAND = 0xFFFFFFFF.toInt()

    private val customActivityPalette = intArrayOf(
        0xFFE57373.toInt(),
        0xFFBA68C8.toInt(),
        0xFF4DB6AC.toInt(),
        0xFFFFD54F.toInt(),
        0xFF7986CB.toInt(),
        0xFFA1887F.toInt(),
    )

    fun of(segment: LifeSegment): Int = when (segment) {
        is LifeSegment.ActivitySegment -> of(segment.activity)
        is LifeSegment.FreeTime -> FREE_TIME
    }

    private fun of(activity: Activity): Int = when (activity) {
        is Activity.DefaultActivity -> when (activity.type) {
            DefaultActivityType.SLEEP -> 0xFF5C6BC0.toInt()
            DefaultActivityType.WORK -> 0xFFEF6C00.toInt()
            DefaultActivityType.EDUCATION -> 0xFF26A69A.toInt()
            DefaultActivityType.COMMUTE -> 0xFF78909C.toInt()
            DefaultActivityType.EATING -> 0xFF9CCC65.toInt()
            DefaultActivityType.HOUSEHOLD -> 0xFF8D6E63.toInt()
            DefaultActivityType.PERSONAL_CARE -> 0xFF42A5F5.toInt()
        }
        // Derived from the id, so a custom activity keeps its color when others are added or removed.
        is Activity.CustomActivity ->
            customActivityPalette[Math.floorMod(
                activity.id.value.hashCode(),
                customActivityPalette.size
            )]
    }
}
