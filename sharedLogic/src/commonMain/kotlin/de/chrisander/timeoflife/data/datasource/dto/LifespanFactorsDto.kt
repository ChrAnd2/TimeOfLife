package de.chrisander.timeoflife.data.datasource.dto

import de.chrisander.timeoflife.domain.model.lifespan.factor.AlcoholConsumption
import de.chrisander.timeoflife.domain.model.lifespan.factor.HealthCondition
import de.chrisander.timeoflife.domain.model.lifespan.factor.PhysicalActivityLevel
import kotlinx.serialization.Serializable

@Serializable
internal data class LifespanFactorsDto(
    val countryCode: String? = null,
    val heightCm: Double? = null,
    val weightKg: Double? = null,
    val smoking: SmokingDto = SmokingDto.Never,
    val alcohol: AlcoholConsumption = AlcoholConsumption.NONE,
    val physicalActivity: PhysicalActivityLevel = PhysicalActivityLevel.NONE,
    val conditions: Set<HealthCondition> = emptySet(),
)
