package de.chrisander.timeoflife.ui.customactivityeditor.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * How far the duration slider moves per notch, depending on the current value. The steps grow with the value, so
 * short activities can be set precisely and a whole year still fits on one slider. Each [below] is a multiple of the
 * next entry's [step], so the notches line up with round values.
 */
enum class DurationStep(val below: Duration, val step: Duration) {
    MINUTES(1.hours, 5.minutes),
    QUARTER_HOURS(4.hours, 15.minutes),
    HALF_HOURS(12.hours, 30.minutes),
    HOURS(2.days, 1.hours),
    FOUR_HOURS(7.days, 4.hours),
    HALF_DAYS(31.days, 12.hours),
    DAYS(90.days, 1.days),
    WEEKS(Duration.INFINITE, 7.days),
}
