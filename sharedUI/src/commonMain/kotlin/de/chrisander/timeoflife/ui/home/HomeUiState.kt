package de.chrisander.timeoflife.ui.home

import de.chrisander.timeoflife.domain.model.activity.ActivityId
import de.chrisander.timeoflife.domain.model.progress.LifeProgress
import de.chrisander.timeoflife.domain.model.progress.LifeSegment
import kotlinx.datetime.LocalDate

data class HomeUiState(
    val name: String = "",
    val timeOfDay: TimeOfDay = TimeOfDay.MORNING,
    val life: LifeOverview,
)

enum class TimeOfDay {
    MORNING,
    AFTERNOON,
    EVENING,
    NIGHT;

    companion object {
        fun of(hour: Int): TimeOfDay = when (hour) {
            in MORNING_START until AFTERNOON_START -> MORNING
            in AFTERNOON_START until EVENING_START -> AFTERNOON
            in EVENING_START until NIGHT_START -> EVENING
            else -> NIGHT
        }

        private const val MORNING_START = 5
        private const val AFTERNOON_START = 12
        private const val EVENING_START = 18
        private const val NIGHT_START = 23
    }
}

data class LifeOverview(
    val progress: LifeProgress,
    val yearsLived: Double,
    val yearsRemaining: Double,
    val lifespanYears: Double,
    val expectedUntil: LocalDate,
    val yearsBeyondLifespan: Double,
    val activities: List<ActivityOverview>,
)

data class ActivityOverview(
    val segment: LifeSegment,
    val yearsSpent: Double,
    val yearsAhead: Double,
) {
    /** `null` for free time. */
    val activityId: ActivityId?
        get() = (segment as? LifeSegment.ActivitySegment)?.activity?.id
}
