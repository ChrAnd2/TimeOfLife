package de.chrisander.timeoflife.domain.model.lifetime

import de.chrisander.timeoflife.domain.model.activity.Activity
import de.chrisander.timeoflife.domain.model.lifespan.Lifespan
import kotlinx.datetime.LocalDate
import kotlin.time.Duration

data class LifeTimeBreakdown(
    val lifespan: Lifespan,
    val expectedDeathDate: LocalDate,
    val lived: Duration,
    val remaining: Duration,
    val beyondLifespan: Duration,
    val activities: List<ActivityTime>,
    val freeTimeRemaining: Duration,
) {
    val total: Duration
        get() = lived + remaining

    val hasOutlivedLifespan: Boolean
        get() = beyondLifespan > Duration.ZERO
}

data class ActivityTime(
    val activity: Activity,
    val spent: Duration,
    val remaining: Duration,
)
