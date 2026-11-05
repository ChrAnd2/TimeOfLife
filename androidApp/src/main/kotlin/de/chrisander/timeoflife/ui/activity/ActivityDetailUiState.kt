package de.chrisander.timeoflife.ui.activity

import de.chrisander.timeoflife.domain.model.activity.ActivitySchedule
import de.chrisander.timeoflife.widget.domain.model.LifeSegment

/** @property detail `null` while loading or when the activity doesn't exist (anymore). */
data class ActivityDetailUiState(
    val isLoading: Boolean = true,
    val detail: ActivityDetail? = null,
)

/**
 * All shares are fractions from 0 to 1.
 *
 * @property segment the activity or free time, for its name and color.
 */
data class ActivityDetail(
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

    /**
     * @property shareOfTime the share of every day the activity takes on average, e.g. 1/3 for 8 h a day.
     * @property age the user's age today in years.
     */
    data class Scheduled(
        val schedule: ActivitySchedule,
        val shareOfTime: Double,
        val age: Double,
    ) : ActivityCalculation

    /**
     * Free time has no schedule: it is whatever the activities leave of an average day.
     *
     * @property daySoFar an average day of the life so far: free time first, then the activities, largest first.
     * Each fraction is its share of the day.
     * @property dayAhead the same for the time ahead; empty once the life span is outlived.
     */
    data class FreeTime(
        val daySoFar: List<LifeSegment>,
        val dayAhead: List<LifeSegment>,
    ) : ActivityCalculation
}
