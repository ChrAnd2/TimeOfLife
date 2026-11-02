package de.chrisander.timeoflife.domain.model.activity

import de.chrisander.timeoflife.domain.model.lifetime.DAYS_PER_MONTH
import kotlinx.datetime.DateTimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

/**
 * How often an activity happens and for how long: [duration] once every [interval],
 * e.g. "8 h every day", "3 h every 2 days", "40 h every week" or "10 h every 3 months".
 */
data class Recurrence(
    val duration: Duration,
    val interval: DateTimeUnit.DateBased,
) {
    init {
        require(!duration.isNegative()) { "Duration must not be negative, was $duration" }
        require(duration <= interval.averageLength) { "$duration does not fit into $interval" }
    }

    val fractionOfTime: Double
        get() = duration / interval.averageLength
}

private val DateTimeUnit.DateBased.averageLength: Duration
    get() = when (this) {
        is DateTimeUnit.DayBased -> days.days
        is DateTimeUnit.MonthBased -> (months * DAYS_PER_MONTH).days
    }
