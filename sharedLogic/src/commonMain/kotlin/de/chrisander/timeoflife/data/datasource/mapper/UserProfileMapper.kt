package de.chrisander.timeoflife.data.datasource.mapper

import de.chrisander.timeoflife.data.datasource.dto.UserProfileDto
import de.chrisander.timeoflife.domain.model.profile.UserProfile

internal fun UserProfile.toDto() = UserProfileDto(
    name = name,
    sex = sex,
    birthDate = birthDate,
    lifespanSetting = lifespanSetting.toDto(),
    lifespanFactors = lifespanFactors.toDto(),
    activities = activities.map { it.toDto() },
)

internal fun UserProfileDto.toDomain() = UserProfile(
    name = name,
    sex = sex,
    birthDate = birthDate,
    lifespanSetting = lifespanSetting.toDomain(),
    lifespanFactors = lifespanFactors.toDomain(),
    activities = activities.map { it.toDomain() },
)
