package de.chrisander.timeoflife.data.datasource.dto

import de.chrisander.timeoflife.domain.model.activity.DefaultActivityType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface ActivityDto {
    /** `null` means the type's current default, so changed defaults in code reach existing users. */
    @Serializable
    @SerialName("default")
    data class Default(val type: DefaultActivityType, val schedule: ScheduleDto? = null) :
        ActivityDto

    @Serializable
    @SerialName("custom")
    data class Custom(val id: String, val name: String, val schedule: ScheduleDto) : ActivityDto
}
