package de.chrisander.timeoflife.data.datasource.dto

import de.chrisander.timeoflife.domain.model.profile.Sex
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/** Root of the stored profile JSON. Changing a field or `@SerialName` in any DTO breaks reading stored profiles. */
@Serializable
internal data class UserProfileDto(
    val name: String,
    val sex: Sex,
    val birthDate: LocalDate,
    val lifespanSetting: LifespanSettingDto,
    val lifespanFactors: LifespanFactorsDto,
    val activities: List<ActivityDto>,
)
