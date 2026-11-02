package de.chrisander.timeoflife.domain.usecase

import de.chrisander.timeoflife.domain.model.activity.Activity
import de.chrisander.timeoflife.domain.model.lifespan.Lifespan
import de.chrisander.timeoflife.domain.model.lifetime.ActivityTime
import de.chrisander.timeoflife.domain.model.lifetime.DAYS_PER_YEAR
import de.chrisander.timeoflife.domain.model.lifetime.LifeTimeBreakdown
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

class LifeTimeCalculator {

    fun calculate(
        birthDate: LocalDate,
        lifespan: Lifespan,
        activities: List<Activity>,
        today: LocalDate,
    ): LifeTimeBreakdown {
        // Everything is measured in days since birth.
        val lifespanDays = (lifespan.years * DAYS_PER_YEAR).roundToLong()
        val livedDays = birthDate.daysUntil(today).toLong().coerceAtLeast(0)
        // Users can outlive the life span; their time lived still counts up to today.
        val lastDay = max(lifespanDays, livedDays)

        val activityTimes = activities.map { activity ->
            val schedule = activity.schedule
            val startDay = schedule.fromAge?.let { birthDate.dayOfAge(it) } ?: 0
            val endDay = min(schedule.untilAge?.let { birthDate.dayOfAge(it) } ?: lastDay, lastDay)
            val fraction = schedule.recurrence.fractionOfTime
            ActivityTime(
                activity = activity,
                spent = overlapDays(startDay, endDay, 0, livedDays).days * fraction,
                remaining = overlapDays(startDay, endDay, livedDays, lifespanDays).days * fraction,
            )
        }

        val remaining = (lifespanDays - livedDays).coerceAtLeast(0).days
        return LifeTimeBreakdown(
            lifespan = lifespan,
            expectedDeathDate = birthDate.plus(lifespanDays, DateTimeUnit.DAY),
            lived = livedDays.days,
            remaining = remaining,
            beyondLifespan = (livedDays - lifespanDays).coerceAtLeast(0).days,
            activities = activityTimes,
            // Activities with more than 24 h per day combined would make this negative.
            freeTimeRemaining = (remaining - activityTimes.fold(Duration.ZERO) { sum, it -> sum + it.remaining })
                .coerceAtLeast(Duration.ZERO),
        )
    }

    private fun LocalDate.dayOfAge(years: Int): Long =
        daysUntil(plus(years, DateTimeUnit.YEAR)).toLong()

    private fun overlapDays(start: Long, end: Long, rangeStart: Long, rangeEnd: Long): Long =
        max(0, min(end, rangeEnd) - max(start, rangeStart))
}
