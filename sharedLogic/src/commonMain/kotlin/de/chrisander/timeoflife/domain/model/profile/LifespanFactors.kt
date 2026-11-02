package de.chrisander.timeoflife.domain.model.profile

import de.chrisander.timeoflife.domain.model.lifespan.factor.AlcoholConsumption
import de.chrisander.timeoflife.domain.model.lifespan.factor.BodyMeasurements
import de.chrisander.timeoflife.domain.model.lifespan.factor.HealthConditions
import de.chrisander.timeoflife.domain.model.lifespan.factor.HealthFactor
import de.chrisander.timeoflife.domain.model.lifespan.factor.PhysicalActivityLevel
import de.chrisander.timeoflife.domain.model.lifespan.factor.SmokingStatus

data class LifespanFactors(
    /** ISO 3166-1 alpha-2 country code of where the user lives, e.g. `"DE"`. `null` uses the global average. */
    val countryCode: String? = null,
    val body: BodyMeasurements? = null,
    val smoking: SmokingStatus = SmokingStatus.Never,
    val alcohol: AlcoholConsumption = AlcoholConsumption.NONE,
    val physicalActivity: PhysicalActivityLevel = PhysicalActivityLevel.NONE,
    val conditions: HealthConditions = HealthConditions(),
) {
    val healthFactors: List<HealthFactor>
        get() = listOfNotNull(smoking, alcohol, physicalActivity, body, conditions)
}
