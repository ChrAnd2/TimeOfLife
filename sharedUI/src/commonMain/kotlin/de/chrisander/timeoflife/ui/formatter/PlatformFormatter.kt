package de.chrisander.timeoflife.ui.formatter

import kotlinx.datetime.LocalDate

internal expect fun formatDecimal(
    value: Double,
    minFractionDigits: Int,
    maxFractionDigits: Int
): String

internal expect fun formatLongDate(date: LocalDate): String
