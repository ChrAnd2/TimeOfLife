package de.chrisander.timeoflife.ui.formatter

import java.text.NumberFormat

/** A share from 0 to 1 as a percent number with at most one decimal, e.g. `"33.3"`. */
internal fun formatPercent(share: Double): String =
    NumberFormat.getNumberInstance().apply { maximumFractionDigits = 1 }.format(share * PERCENT)

private const val PERCENT = 100
