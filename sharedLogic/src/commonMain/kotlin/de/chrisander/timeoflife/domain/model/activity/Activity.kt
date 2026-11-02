package de.chrisander.timeoflife.domain.model.activity

import kotlin.jvm.JvmInline

@JvmInline
value class ActivityId(val value: String)

/** Something that regularly takes up the user's time, like sleeping or a hobby. */
sealed interface Activity {
    val id: ActivityId
    val schedule: ActivitySchedule

    data class DefaultActivity(
        val type: DefaultActivityType,
        override val schedule: ActivitySchedule = type.defaultSchedule,
    ) : Activity {
        override val id: ActivityId = ActivityId("default:${type.name}")
    }

    data class CustomActivity(
        override val id: ActivityId,
        val name: String,
        override val schedule: ActivitySchedule,
    ) : Activity
}
