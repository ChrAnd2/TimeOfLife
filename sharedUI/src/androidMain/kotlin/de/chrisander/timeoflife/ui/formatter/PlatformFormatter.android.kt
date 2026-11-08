package de.chrisander.timeoflife.ui.formatter

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

internal actual fun formatDecimal(
    value: Double,
    minFractionDigits: Int,
    maxFractionDigits: Int
): String =
    NumberFormat.getNumberInstance().apply {
        minimumFractionDigits = minFractionDigits
        maximumFractionDigits = maxFractionDigits
    }.format(value)

internal actual fun formatLongDate(date: LocalDate): String =
    date.toJavaLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
