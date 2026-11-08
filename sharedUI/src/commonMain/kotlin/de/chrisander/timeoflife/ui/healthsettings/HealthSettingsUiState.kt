package de.chrisander.timeoflife.ui.healthsettings

import de.chrisander.timeoflife.domain.model.lifespan.factor.AlcoholConsumption
import de.chrisander.timeoflife.domain.model.lifespan.factor.HealthCondition
import de.chrisander.timeoflife.domain.model.lifespan.factor.PhysicalActivityLevel
import de.chrisander.timeoflife.ui.healthsettings.model.SmokingChoice

data class HealthSettingsUiState(
    val countryCode: String? = null,
    val height: String = "",
    val weight: String = "",
    val bmi: Double? = null,
    val smoking: SmokingChoice = SmokingChoice.NEVER,
    val quitAge: String = "",
    val alcohol: AlcoholConsumption = AlcoholConsumption.NONE,
    val physicalActivity: PhysicalActivityLevel = PhysicalActivityLevel.NONE,
    val conditions: Set<HealthCondition> = emptySet(),
    val heightError: Boolean = false,
    val weightError: Boolean = false,
    val quitAgeError: Boolean = false,
)
