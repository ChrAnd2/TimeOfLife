package de.chrisander.timeoflife.ui.home.mapper

import de.chrisander.timeoflife.domain.mapper.toLifeProgress
import de.chrisander.timeoflife.domain.model.lifetime.DAYS_PER_YEAR
import de.chrisander.timeoflife.domain.model.lifetime.LifeTimeBreakdown
import de.chrisander.timeoflife.domain.model.progress.LifeSegment
import de.chrisander.timeoflife.ui.home.ActivityOverview
import de.chrisander.timeoflife.ui.home.LifeOverview
import kotlin.time.Duration
import kotlin.time.DurationUnit

fun LifeTimeBreakdown.toLifeOverview(): LifeOverview = LifeOverview(
    progress = toLifeProgress(),
    yearsLived = lived.inYears(),
    yearsRemaining = remaining.inYears(),
    lifespanYears = lifespan.years,
    expectedUntil = expectedDeathDate,
    yearsBeyondLifespan = beyondLifespan.inYears(),
    activities = activityOverviews(),
)

private fun LifeTimeBreakdown.activityOverviews(): List<ActivityOverview> {
    val activities = activities.map {
        ActivityOverview(
            LifeSegment.ActivitySegment(it.activity, it.spent shareOf lived),
            it.spent.inYears(),
            it.remaining.inYears(),
        )
    }
    val freeTimeSpent = freeTimeSpent
    val freeTime = ActivityOverview(
        LifeSegment.FreeTime(freeTimeSpent shareOf lived),
        freeTimeSpent.inYears(),
        freeTimeRemaining.inYears(),
    )
    return (activities + freeTime)
        .filter { it.yearsSpent > 0 || it.yearsAhead > 0 }
        .sortedByDescending { it.yearsSpent }
}

internal val LifeTimeBreakdown.freeTimeSpent: Duration
    get() {
        val activitiesSpent = activities.fold(Duration.ZERO) { sum, activity -> sum + activity.spent }
        return (lived - activitiesSpent).coerceAtLeast(Duration.ZERO)
    }

internal infix fun Duration.shareOf(whole: Duration): Double =
    if (whole > Duration.ZERO) this / whole else 0.0

internal fun Duration.inYears(): Double = toDouble(DurationUnit.DAYS) / DAYS_PER_YEAR
