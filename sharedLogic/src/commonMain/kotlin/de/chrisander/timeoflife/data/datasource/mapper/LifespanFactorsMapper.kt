package de.chrisander.timeoflife.data.datasource.mapper

import de.chrisander.timeoflife.data.datasource.dto.LifespanFactorsDto
import de.chrisander.timeoflife.data.datasource.dto.SmokingDto
import de.chrisander.timeoflife.domain.model.lifespan.factor.BodyMeasurements
import de.chrisander.timeoflife.domain.model.lifespan.factor.HealthConditions
import de.chrisander.timeoflife.domain.model.lifespan.factor.SmokingStatus
import de.chrisander.timeoflife.domain.model.profile.LifespanFactors

internal fun LifespanFactors.toDto() = LifespanFactorsDto(
    countryCode = countryCode,
    heightCm = body?.heightCm,
    weightKg = body?.weightKg,
    smoking = smoking.toDto(),
    alcohol = alcohol,
    physicalActivity = physicalActivity,
    conditions = conditions.conditions,
)

internal fun LifespanFactorsDto.toDomain() = LifespanFactors(
    countryCode = countryCode,
    body = if (heightCm != null && weightKg != null) BodyMeasurements(heightCm, weightKg) else null,
    smoking = smoking.toDomain(),
    alcohol = alcohol,
    physicalActivity = physicalActivity,
    conditions = HealthConditions(conditions),
)

private fun SmokingStatus.toDto(): SmokingDto = when (this) {
    SmokingStatus.Never -> SmokingDto.Never
    SmokingStatus.Current -> SmokingDto.Current
    is SmokingStatus.Former -> SmokingDto.Former(quitAge)
}

private fun SmokingDto.toDomain(): SmokingStatus = when (this) {
    SmokingDto.Never -> SmokingStatus.Never
    SmokingDto.Current -> SmokingStatus.Current
    is SmokingDto.Former -> SmokingStatus.Former(quitAge)
}
