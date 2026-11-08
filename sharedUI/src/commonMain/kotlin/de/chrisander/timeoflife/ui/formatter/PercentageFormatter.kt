package de.chrisander.timeoflife.ui.formatter

/** A share from 0 to 1 as a percent number with at most one decimal, e.g. `"33.3"`. */
internal fun formatPercent(share: Double): String =
    formatDecimal(share * PERCENT, minFractionDigits = 0, maxFractionDigits = 1)

private const val PERCENT = 100
