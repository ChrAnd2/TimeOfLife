package de.chrisander.timeoflife.ui.formatter

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import de.chrisander.timeoflife.R
import de.chrisander.timeoflife.domain.model.activity.Recurrence
import kotlinx.datetime.DateTimeUnit

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
                pluralStringResource(
                    R.plurals.every_years,
                    it / MONTHS_PER_YEAR,
                    it / MONTHS_PER_YEAR
                )
            } else {
                pluralStringResource(R.plurals.every_months, it, it)
            }
        }
    }
    return "$length $every"
}

private const val DAYS_PER_WEEK = 7
private const val MONTHS_PER_YEAR = 12
