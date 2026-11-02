package de.chrisander.timeoflife.domain.model.activity

/**
 * When and how often an activity happens over a lifetime.
 *
 * @property recurrence how often and for how long, e.g. "8 h every day".
 * @property fromAge age in years at which the activity starts, inclusive. `null` means from birth.
 * @property untilAge age in years at which the activity ends, exclusive. `null` means until the end of life.
 */
data class ActivitySchedule(
    val recurrence: Recurrence,
    val fromAge: Int? = null,
    val untilAge: Int? = null,
) {
    init {
        require(fromAge == null || fromAge >= 0) { "fromAge must not be negative, was $fromAge" }
        if (fromAge != null && untilAge != null) {
            require(fromAge < untilAge) { "fromAge ($fromAge) must be before untilAge ($untilAge)" }
        }
    }
}
