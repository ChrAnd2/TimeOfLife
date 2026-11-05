package de.chrisander.timeoflife.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import de.chrisander.timeoflife.R
import de.chrisander.timeoflife.domain.model.activity.Activity
import de.chrisander.timeoflife.domain.model.activity.DefaultActivityType
import de.chrisander.timeoflife.widget.WidgetColors
import de.chrisander.timeoflife.widget.domain.model.LifeSegment

/** Same colors as the widgets, so an activity looks the same everywhere. */
internal val LifeSegment.color: Color
    get() = Color(WidgetColors.of(this))

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
