package de.chrisander.timeoflife.ui.activity

import de.chrisander.timeoflife.domain.model.activity.ActivityId
import de.chrisander.timeoflife.domain.model.lifetime.ActivityTime
import de.chrisander.timeoflife.domain.model.lifetime.LifeTimeBreakdown
import de.chrisander.timeoflife.ui.home.freeTimeSpent
import de.chrisander.timeoflife.ui.home.inYears
import de.chrisander.timeoflife.ui.home.shareOf
import de.chrisander.timeoflife.widget.domain.model.LifeSegment
import kotlin.time.Duration

/** @param activityId `null` for free time. Returns `null` if there is no such activity. */
fun LifeTimeBreakdown.activityDetail(activityId: ActivityId?): ActivityDetail? =
    if (activityId == null) {
        freeTimeDetail()
    } else {
        activities.firstOrNull { it.activity.id == activityId }?.let(::activityDetail)
    }

private fun LifeTimeBreakdown.activityDetail(time: ActivityTime): ActivityDetail = detail(
    segment = LifeSegment.ActivitySegment(time.activity, time.spent shareOf lived),
    spent = time.spent,
    ahead = time.remaining,
    calculation = ActivityCalculation.Scheduled(
        schedule = time.activity.schedule,
        shareOfTime = time.activity.schedule.recurrence.fractionOfTime,
        age = lived.inYears(),
    ),
)

private fun LifeTimeBreakdown.freeTimeDetail(): ActivityDetail {
    val spent = freeTimeSpent
    return detail(
        segment = LifeSegment.FreeTime(spent shareOf lived),
        spent = spent,
        ahead = freeTimeRemaining,
        calculation = ActivityCalculation.FreeTime,
    )
}

private fun LifeTimeBreakdown.detail(
    segment: LifeSegment,
    spent: Duration,
    ahead: Duration,
    calculation: ActivityCalculation,
) = ActivityDetail(
    segment = segment,
    yearsSpent = spent.inYears(),
    shareOfLived = spent shareOf lived,
    yearsAhead = ahead.inYears(),
    shareOfRemaining = ahead shareOf remaining,
    yearsTotal = (spent + ahead).inYears(),
    shareOfLifespan = (spent + ahead) shareOf total,
    lifespanYears = lifespan.years,
    calculation = calculation,
)
