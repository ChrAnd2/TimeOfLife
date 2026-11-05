package de.chrisander.timeoflife.ui.activity.formatter

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.chrisander.timeoflife.R
import de.chrisander.timeoflife.domain.model.activity.ActivitySchedule
import de.chrisander.timeoflife.ui.activity.ActivityCalculation

/** E.g. "18 to 67" or "since birth". */
@Composable
internal fun ActivitySchedule.ageRangeText(): String {
    val from = fromAge?.takeIf { it > 0 }
    val untilAge = untilAge
    return when {
        from != null && untilAge != null -> stringResource(
            R.string.schedule_age_range,
            from,
            untilAge
        )

        from != null -> stringResource(R.string.schedule_from_age, from)
        untilAge != null -> stringResource(R.string.schedule_until_age, untilAge)
        else -> stringResource(R.string.schedule_whole_life)
    }
}

/** The schedule's age range as a sentence, e.g. "From age 18 to 67" or "Your whole life". */
@Composable
internal fun ActivityCalculation.Scheduled.whenText(): String {
    val from = schedule.fromAge?.takeIf { it > 0 }
    val until = schedule.untilAge
    return when {
        from != null && until != null -> stringResource(R.string.when_age_range, from, until)
        from != null -> stringResource(R.string.when_from_age, from)
        until != null -> stringResource(R.string.when_until_age, until)
        else -> stringResource(R.string.when_whole_life)
    }
}
