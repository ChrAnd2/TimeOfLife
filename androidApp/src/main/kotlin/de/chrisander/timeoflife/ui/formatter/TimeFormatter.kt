package de.chrisander.timeoflife.ui.formatter

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import de.chrisander.timeoflife.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.roundToInt

internal fun formatYears(years: Double): String =
    NumberFormat.getNumberInstance().apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
    }.format(years)

internal fun formatDate(date: LocalDate): String =
    date.toJavaLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))

@Composable
internal fun yearsText(years: Double): String {
    val shown = formatYears(years)
    return pluralStringResource(
        R.plurals.home_activity_years,
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
        restMinutes == 0 -> stringResource(R.string.duration_hours, hours)
        hours == 0 -> stringResource(R.string.duration_minutes, restMinutes)
        else -> stringResource(R.string.duration_hours_minutes, hours, restMinutes)
    }
}

/** A share of the day as time, e.g. "5 h 43 min" for 40 h a week. */
@Composable
internal fun perDayText(shareOfDay: Double): String =
    durationText((shareOfDay * MINUTES_PER_DAY).roundToInt())

private const val MINUTES_PER_HOUR = 60
private const val MINUTES_PER_DAY = 24 * 60
