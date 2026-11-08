package de.chrisander.timeoflife.ui.formatter

import androidx.compose.runtime.Composable
import de.chrisander.timeoflife.resources.Res
import de.chrisander.timeoflife.resources.duration_hours
import de.chrisander.timeoflife.resources.duration_hours_minutes
import de.chrisander.timeoflife.resources.duration_minutes
import de.chrisander.timeoflife.resources.home_activity_years
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

internal fun formatYears(years: Double): String =
    formatDecimal(years, minFractionDigits = 1, maxFractionDigits = 1)

internal fun formatDate(date: LocalDate): String =
    formatLongDate(date)

@Composable
internal fun yearsText(years: Double): String {
    val shown = formatYears(years)
    return pluralStringResource(
        Res.plurals.home_activity_years,
        if (shown == formatYears(1.0)) 1 else 2,
        shown
    )
}

/** E.g. "8 h", "45 min" or "5 h 43 min". */
@Composable
internal fun durationText(minutes: Int): String {
    val hours = minutes / MINUTES_PER_HOUR
    val restMinutes = minutes % MINUTES_PER_HOUR
    return when {
        restMinutes == 0 -> stringResource(Res.string.duration_hours, hours)
        hours == 0 -> stringResource(Res.string.duration_minutes, restMinutes)
        else -> stringResource(Res.string.duration_hours_minutes, hours, restMinutes)
    }
}

/** A share of the day as time, e.g. "5 h 43 min" for 40 h a week. */
@Composable
internal fun perDayText(shareOfDay: Double): String =
    durationText((shareOfDay * MINUTES_PER_DAY).roundToInt())

private const val MINUTES_PER_HOUR = 60
private const val MINUTES_PER_DAY = 24 * 60
