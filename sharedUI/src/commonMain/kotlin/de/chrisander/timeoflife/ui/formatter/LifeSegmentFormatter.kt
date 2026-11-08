package de.chrisander.timeoflife.ui.formatter

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import de.chrisander.timeoflife.brand.SegmentColors
import de.chrisander.timeoflife.domain.model.activity.Activity
import de.chrisander.timeoflife.domain.model.activity.DefaultActivityType
import de.chrisander.timeoflife.domain.model.progress.LifeSegment
import de.chrisander.timeoflife.resources.Res
import de.chrisander.timeoflife.resources.activity_commute
import de.chrisander.timeoflife.resources.activity_eating
import de.chrisander.timeoflife.resources.activity_education
import de.chrisander.timeoflife.resources.activity_free_time
import de.chrisander.timeoflife.resources.activity_free_time_description
import de.chrisander.timeoflife.resources.activity_household
import de.chrisander.timeoflife.resources.activity_personal_care
import de.chrisander.timeoflife.resources.activity_sleep
import de.chrisander.timeoflife.resources.activity_work
import de.chrisander.timeoflife.ui.activity.formatter.ageRangeText
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LifeSegment.label(): String = when (this) {
    is LifeSegment.FreeTime -> stringResource(Res.string.activity_free_time)
    is LifeSegment.ActivitySegment -> when (val activity = activity) {
        is Activity.CustomActivity -> activity.name
        is Activity.DefaultActivity -> stringResource(
            when (activity.type) {
                DefaultActivityType.SLEEP -> Res.string.activity_sleep
                DefaultActivityType.WORK -> Res.string.activity_work
                DefaultActivityType.EDUCATION -> Res.string.activity_education
                DefaultActivityType.COMMUTE -> Res.string.activity_commute
                DefaultActivityType.EATING -> Res.string.activity_eating
                DefaultActivityType.HOUSEHOLD -> Res.string.activity_household
                DefaultActivityType.PERSONAL_CARE -> Res.string.activity_personal_care
            },
        )
    }
}

internal val LifeSegment.color: Color
    get() = Color(SegmentColors.of(this))

/** E.g. "40 h a week · 18 to 67" for an activity, or what free time is. */
@Composable
internal fun LifeSegment.description(): String = when (this) {
    is LifeSegment.ActivitySegment -> "${activity.schedule.recurrence.text()} · ${activity.schedule.ageRangeText()}"
    is LifeSegment.FreeTime -> stringResource(Res.string.activity_free_time_description)
}
