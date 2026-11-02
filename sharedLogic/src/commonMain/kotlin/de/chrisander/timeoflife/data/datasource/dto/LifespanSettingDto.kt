package de.chrisander.timeoflife.data.datasource.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface LifespanSettingDto {
    @Serializable
    @SerialName("estimated")
    data object Estimated : LifespanSettingDto

    @Serializable
    @SerialName("manual")
    data class Manual(val years: Double) : LifespanSettingDto
}
