package de.chrisander.timeoflife.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import de.chrisander.timeoflife.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

internal fun formatYears(years: Double): String =
    NumberFormat.getNumberInstance().apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
    }.format(years)

internal fun formatDate(date: LocalDate): String =
    date.toJavaLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))

/** "1.0 year" or "10.6 years", decided by the number as shown. */
@Composable
internal fun yearsText(years: Double): String {
    val shown = formatYears(years)
    return pluralStringResource(R.plurals.home_activity_years, if (shown == formatYears(1.0)) 1 else 2, shown)
}
