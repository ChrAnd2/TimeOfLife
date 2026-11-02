package de.chrisander.timeoflife.data.datasource.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface SmokingDto {
    @Serializable
    @SerialName("never")
    data object Never : SmokingDto

    @Serializable
    @SerialName("current")
    data object Current : SmokingDto

    @Serializable
    @SerialName("former")
    data class Former(val quitAge: Int) : SmokingDto
}
