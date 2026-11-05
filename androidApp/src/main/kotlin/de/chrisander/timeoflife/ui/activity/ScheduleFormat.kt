package de.chrisander.timeoflife.ui.activity

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import de.chrisander.timeoflife.R
import de.chrisander.timeoflife.domain.model.activity.ActivitySchedule
import de.chrisander.timeoflife.domain.model.activity.Recurrence
import de.chrisander.timeoflife.widget.domain.model.LifeSegment
import kotlinx.datetime.DateTimeUnit
import java.text.NumberFormat

/** E.g. "8 h a day" or "10 h every 3 months". */
@Composable
internal fun Recurrence.text(): String {
    val length = durationText(duration.inWholeMinutes.toInt())
    val every = when (val interval = interval) {
        is DateTimeUnit.DayBased -> interval.days.let {
            if (it % DAYS_PER_WEEK == 0) {
                pluralStringResource(R.plurals.every_weeks, it / DAYS_PER_WEEK, it / DAYS_PER_WEEK)
            } else {
                pluralStringResource(R.plurals.every_days, it, it)
            }
        }

        is DateTimeUnit.MonthBased -> interval.months.let {
            if (it % MONTHS_PER_YEAR == 0) {
                pluralStringResource(R.plurals.every_years, it / MONTHS_PER_YEAR, it / MONTHS_PER_YEAR)
            } else {
                pluralStringResource(R.plurals.every_months, it, it)
            }
        }
    }
    return "$length $every"
}

/** E.g. "8 h", "45 min" or "5 h 43 min". */
@Composable
internal fun durationText(minutes: Int): String {
    val hours = minutes / MINUTES_PER_HOUR
    val restMinutes = minutes % MINUTES_PER_HOUR
    return when {
        restMinutes == 0 -> stringResource(R.string.duration_hours, hours)
        hours == 0 -> stringResource(R.string.duration_minutes, restMinutes)
        else -> stringResource(R.string.duration_hours_minutes, hours, restMinutes)
    }
}

/** E.g. "18 to 67" or "since birth". */
@Composable
internal fun ActivitySchedule.ageRangeText(): String {
    val from = fromAge?.takeIf { it > 0 }
    val untilAge = untilAge
    return when {
        from != null && untilAge != null -> stringResource(R.string.schedule_age_range, from, untilAge)
        from != null -> stringResource(R.string.schedule_from_age, from)
        untilAge != null -> stringResource(R.string.schedule_until_age, untilAge)
        else -> stringResource(R.string.schedule_whole_life)
    }
}

/** E.g. "40 h a week · 18 to 67" for an activity, or what free time is. */
@Composable
internal fun LifeSegment.description(): String = when (this) {
    is LifeSegment.ActivitySegment -> "${activity.schedule.recurrence.text()} · ${activity.schedule.ageRangeText()}"
    is LifeSegment.FreeTime -> stringResource(R.string.activity_free_time_description)
}

/** A share from 0 to 1 as a percent number with at most one decimal, e.g. `"33.3"`. */
internal fun formatPercent(share: Double): String =
    NumberFormat.getNumberInstance().apply { maximumFractionDigits = 1 }.format(share * PERCENT)

private const val MINUTES_PER_HOUR = 60
private const val DAYS_PER_WEEK = 7
private const val MONTHS_PER_YEAR = 12
private const val PERCENT = 100
