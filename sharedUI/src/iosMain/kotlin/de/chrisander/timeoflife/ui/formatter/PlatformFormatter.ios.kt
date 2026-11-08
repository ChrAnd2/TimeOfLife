package de.chrisander.timeoflife.ui.formatter

import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import platform.Foundation.NSCalendar
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterLongStyle
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle

internal actual fun formatDecimal(
    value: Double,
    minFractionDigits: Int,
    maxFractionDigits: Int
): String {
    val formatter = NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterDecimalStyle
        minimumFractionDigits = minFractionDigits.toULong()
        maximumFractionDigits = maxFractionDigits.toULong()
    }
    return formatter.stringFromNumber(NSNumber(double = value)) ?: value.toString()
}

internal actual fun formatLongDate(date: LocalDate): String {
    val components = NSDateComponents().apply {
        year = date.year.toLong()
        month = date.month.number.toLong()
        day = date.day.toLong()
    }
    val nsDate = NSCalendar.currentCalendar.dateFromComponents(components) ?: return date.toString()
    val formatter = NSDateFormatter().apply {
        dateStyle = NSDateFormatterLongStyle
        timeStyle = NSDateFormatterNoStyle
    }
    return formatter.stringFromDate(nsDate)
}
