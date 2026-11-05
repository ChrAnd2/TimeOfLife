package de.chrisander.timeoflife.domain.model

import de.chrisander.timeoflife.domain.model.activity.Activity

sealed interface LifeSegment {
    val fraction: Double

    data class ActivitySegment(val activity: Activity, override val fraction: Double) : LifeSegment
    data class FreeTime(override val fraction: Double) : LifeSegment
}
