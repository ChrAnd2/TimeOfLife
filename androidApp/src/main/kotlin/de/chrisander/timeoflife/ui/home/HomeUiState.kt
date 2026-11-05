package de.chrisander.timeoflife.ui.home

import de.chrisander.timeoflife.domain.model.activity.ActivityId
import de.chrisander.timeoflife.widget.domain.model.LifeProgress
import de.chrisander.timeoflife.widget.domain.model.LifeSegment
import kotlinx.datetime.LocalDate

data class HomeUiState(
    val name: String = "",
    val timeOfDay: TimeOfDay = TimeOfDay.MORNING,
    val life: LifeOverview? = null,
)

enum class TimeOfDay {
    MORNING,
    AFTERNOON,
    EVENING,
    NIGHT,
    ;

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

/**
 * @property progress the lived share of life, drawn as the life clock.
 * @property activities every activity plus free time, by their time so far, largest first.
 */
data class LifeOverview(
    val progress: LifeProgress,
    val yearsLived: Double,
    val yearsRemaining: Double,
    val lifespanYears: Double,
    val expectedUntil: LocalDate,
    val yearsBeyondLifespan: Double,
    val activities: List<ActivityOverview>,
)

/** @property segment the activity or free time; its fraction is its share of the life lived so far. */
data class ActivityOverview(
    val segment: LifeSegment,
    val yearsSpent: Double,
    val yearsAhead: Double,
) {
    /** `null` for free time. */
    val activityId: ActivityId?
        get() = (segment as? LifeSegment.ActivitySegment)?.activity?.id
}
