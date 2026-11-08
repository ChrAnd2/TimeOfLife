package de.chrisander.timeoflife.ui.formatter

import androidx.compose.runtime.Composable
import de.chrisander.timeoflife.domain.model.activity.Recurrence
import de.chrisander.timeoflife.resources.Res
import de.chrisander.timeoflife.resources.every_days
import de.chrisander.timeoflife.resources.every_months
import de.chrisander.timeoflife.resources.every_weeks
import de.chrisander.timeoflife.resources.every_years
import kotlinx.datetime.DateTimeUnit
import org.jetbrains.compose.resources.pluralStringResource

/** E.g. "8 h a day" or "10 h every 3 months". */
@Composable
internal fun Recurrence.text(): String {
    val length = durationText(duration.inWholeMinutes.toInt())
    val every = when (val interval = interval) {
        is DateTimeUnit.DayBased -> interval.days.let {
            if (it % DAYS_PER_WEEK == 0) {
                pluralStringResource(
                    Res.plurals.every_weeks,
                    it / DAYS_PER_WEEK,
                    it / DAYS_PER_WEEK
                )
            } else {
                pluralStringResource(Res.plurals.every_days, it, it)
            }
        }

        is DateTimeUnit.MonthBased -> interval.months.let {
            if (it % MONTHS_PER_YEAR == 0) {
                pluralStringResource(
                    Res.plurals.every_years,
                    it / MONTHS_PER_YEAR,
                    it / MONTHS_PER_YEAR
                )
            } else {
                pluralStringResource(Res.plurals.every_months, it, it)
            }
        }
    }
    return "$length $every"
}

private const val DAYS_PER_WEEK = 7
private const val MONTHS_PER_YEAR = 12
