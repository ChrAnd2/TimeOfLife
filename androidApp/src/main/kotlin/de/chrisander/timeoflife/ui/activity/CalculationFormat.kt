package de.chrisander.timeoflife.ui.activity

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.chrisander.timeoflife.R
import kotlin.math.roundToInt

/** The schedule's age range as a sentence, e.g. "From age 18 to 67" or "Your whole life". */
@Composable
internal fun ActivityCalculation.Scheduled.whenText(): String {
    val from = schedule.fromAge?.takeIf { it > 0 }
    val until = schedule.untilAge
    return when {
        from != null && until != null -> stringResource(R.string.when_age_range, from, until)
        from != null -> stringResource(R.string.when_from_age, from)
        until != null -> stringResource(R.string.when_until_age, until)
        else -> stringResource(R.string.when_whole_life)
    }
}

/** A share of the day as time, e.g. "5 h 43 min" for 40 h a week. */
@Composable
internal fun perDayText(shareOfDay: Double): String =
    durationText((shareOfDay * MINUTES_PER_DAY).roundToInt())

private const val MINUTES_PER_DAY = 24 * 60
