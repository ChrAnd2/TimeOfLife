package de.chrisander.timeoflife.data.datasource.mapper

import de.chrisander.timeoflife.data.datasource.dto.ActivityDto
import de.chrisander.timeoflife.data.datasource.dto.IntervalDto
import de.chrisander.timeoflife.data.datasource.dto.ScheduleDto
import de.chrisander.timeoflife.domain.model.activity.Activity
import de.chrisander.timeoflife.domain.model.activity.ActivityId
import de.chrisander.timeoflife.domain.model.activity.ActivitySchedule
import de.chrisander.timeoflife.domain.model.activity.Recurrence
import kotlinx.datetime.DateTimeUnit

internal fun Activity.toDto(): ActivityDto = when (this) {
    is Activity.DefaultActivity -> ActivityDto.Default(
        type = type,
        schedule = schedule.takeIf { it != type.defaultSchedule }?.toDto(),
    )

    is Activity.CustomActivity -> ActivityDto.Custom(
        id = id.value,
        name = name,
        schedule = schedule.toDto()
    )
}

internal fun ActivityDto.toDomain(): Activity = when (this) {
    is ActivityDto.Default -> Activity.DefaultActivity(
        type,
        schedule?.toDomain() ?: type.defaultSchedule
    )

    is ActivityDto.Custom -> Activity.CustomActivity(ActivityId(id), name, schedule.toDomain())
}

private fun ActivitySchedule.toDto() = ScheduleDto(
    duration = recurrence.duration,
    interval = when (val interval = recurrence.interval) {
        is DateTimeUnit.DayBased -> IntervalDto.Days(interval.days)
        is DateTimeUnit.MonthBased -> IntervalDto.Months(interval.months)
    },
    fromAge = fromAge,
    untilAge = untilAge,
)

private fun ScheduleDto.toDomain() = ActivitySchedule(
    recurrence = Recurrence(
        duration = duration,
        interval = when (interval) {
            is IntervalDto.Days -> DateTimeUnit.DAY * interval.days
            is IntervalDto.Months -> DateTimeUnit.MONTH * interval.months
        },
    ),
    fromAge = fromAge,
    untilAge = untilAge,
)
