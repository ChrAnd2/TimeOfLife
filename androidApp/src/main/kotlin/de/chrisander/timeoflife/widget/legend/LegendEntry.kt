package de.chrisander.timeoflife.widget.legend

import android.content.Context
import de.chrisander.timeoflife.R
import de.chrisander.timeoflife.domain.model.LifeProgress
import de.chrisander.timeoflife.domain.model.LifeSegment
import de.chrisander.timeoflife.domain.model.activity.Activity
import de.chrisander.timeoflife.domain.model.activity.DefaultActivityType
import de.chrisander.timeoflife.widget.WidgetColors
import kotlin.math.roundToInt

data class LegendEntry(
    val color: Int,
    val label: String,
    val percent: Int,
)

fun LifeProgress.legendEntries(labelOf: (LifeSegment) -> String): List<LegendEntry> {
    if (progress <= 0.0) return emptyList()
    return segments
        .sortedByDescending { it.fraction }
        .map {
            LegendEntry(
                color = WidgetColors.of(it),
                label = labelOf(it),
                percent = (it.fraction / progress * 100).roundToInt()
            )
        }
}

fun Context.legendLabel(segment: LifeSegment): String = when (segment) {
    is LifeSegment.FreeTime -> getString(R.string.activity_free_time)
    is LifeSegment.ActivitySegment -> when (val activity = segment.activity) {
        is Activity.CustomActivity -> activity.name
        is Activity.DefaultActivity -> getString(
            when (activity.type) {
                DefaultActivityType.SLEEP -> R.string.activity_sleep
                DefaultActivityType.WORK -> R.string.activity_work
                DefaultActivityType.EDUCATION -> R.string.activity_education
                DefaultActivityType.COMMUTE -> R.string.activity_commute
                DefaultActivityType.EATING -> R.string.activity_eating
                DefaultActivityType.HOUSEHOLD -> R.string.activity_household
                DefaultActivityType.PERSONAL_CARE -> R.string.activity_personal_care
            },
        )
    }
}
