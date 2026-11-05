package de.chrisander.timeoflife.ui.activity

import de.chrisander.timeoflife.domain.model.LifeSegment
import de.chrisander.timeoflife.domain.model.activity.ActivitySchedule

data class ActivityDetailUiState(
    val segment: LifeSegment,
    val yearsSpent: Double,
    val shareOfLived: Double,
    val yearsAhead: Double,
    val shareOfRemaining: Double,
    val yearsTotal: Double,
    val shareOfLifespan: Double,
    val lifespanYears: Double,
    val calculation: ActivityCalculation,
)

sealed interface ActivityCalculation {
    data class Scheduled(
        val schedule: ActivitySchedule,
        val shareOfTime: Double,
        val age: Double,
    ) : ActivityCalculation

    data class FreeTime(
        val daySoFar: List<LifeSegment>,
        val dayAhead: List<LifeSegment>,
    ) : ActivityCalculation
}
