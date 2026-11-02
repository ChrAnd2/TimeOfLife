package de.chrisander.timeoflife.data.datasource.dto

import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
internal data class ScheduleDto(
    val duration: Duration,
    val interval: IntervalDto,
    val fromAge: Int? = null,
    val untilAge: Int? = null,
)
