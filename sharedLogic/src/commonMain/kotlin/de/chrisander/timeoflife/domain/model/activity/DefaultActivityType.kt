package de.chrisander.timeoflife.domain.model.activity

import kotlinx.datetime.DateTimeUnit
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

enum class DefaultActivityType(val defaultSchedule: ActivitySchedule) {
    SLEEP(ActivitySchedule(Recurrence(8.hours, DateTimeUnit.DAY))),
    WORK(ActivitySchedule(Recurrence(40.hours, DateTimeUnit.WEEK), fromAge = 18, untilAge = 67)),
    EDUCATION(
        ActivitySchedule(
            Recurrence(30.hours, DateTimeUnit.WEEK),
            fromAge = 6,
            untilAge = 18
        )
    ),
    COMMUTE(ActivitySchedule(Recurrence(5.hours, DateTimeUnit.WEEK), fromAge = 6, untilAge = 67)),
    EATING(ActivitySchedule(Recurrence(90.minutes, DateTimeUnit.DAY))),
    HOUSEHOLD(ActivitySchedule(Recurrence(2.hours, DateTimeUnit.DAY), fromAge = 18)),
    PERSONAL_CARE(ActivitySchedule(Recurrence(45.minutes, DateTimeUnit.DAY))),
}
