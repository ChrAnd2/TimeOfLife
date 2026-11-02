package de.chrisander.timeoflife.data.datasource.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface IntervalDto {
    @Serializable
    @SerialName("days")
    data class Days(val days: Int) : IntervalDto

    @Serializable
    @SerialName("months")
    data class Months(val months: Int) : IntervalDto
}
