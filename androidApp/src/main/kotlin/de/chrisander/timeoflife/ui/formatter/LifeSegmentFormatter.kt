package de.chrisander.timeoflife.ui.formatter

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import de.chrisander.timeoflife.R
import de.chrisander.timeoflife.domain.model.LifeSegment
import de.chrisander.timeoflife.domain.model.activity.Activity
import de.chrisander.timeoflife.domain.model.activity.DefaultActivityType
import de.chrisander.timeoflife.ui.activity.formatter.ageRangeText
import de.chrisander.timeoflife.widget.WidgetColors

@Composable
internal fun LifeSegment.label(): String = when (this) {
    is LifeSegment.FreeTime -> stringResource(R.string.activity_free_time)
    is LifeSegment.ActivitySegment -> when (val activity = activity) {
        is Activity.CustomActivity -> activity.name
        is Activity.DefaultActivity -> stringResource(
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

internal val LifeSegment.color: Color
    get() = Color(WidgetColors.of(this))

/** E.g. "40 h a week · 18 to 67" for an activity, or what free time is. */
@Composable
internal fun LifeSegment.description(): String = when (this) {
    is LifeSegment.ActivitySegment -> "${activity.schedule.recurrence.text()} · ${activity.schedule.ageRangeText()}"
    is LifeSegment.FreeTime -> stringResource(R.string.activity_free_time_description)
}
