package de.chrisander.timeoflife.ui.healthsettings

import de.chrisander.timeoflife.domain.model.lifespan.factor.AlcoholConsumption
import de.chrisander.timeoflife.domain.model.lifespan.factor.HealthCondition
import de.chrisander.timeoflife.domain.model.lifespan.factor.PhysicalActivityLevel
import de.chrisander.timeoflife.ui.healthsettings.model.SmokingChoice

sealed interface HealthSettingsEvent {
    data class CountrySelected(val countryCode: String?) : HealthSettingsEvent
    data class HeightChanged(val height: String) : HealthSettingsEvent
    data class WeightChanged(val weight: String) : HealthSettingsEvent
    data class SmokingSelected(val smoking: SmokingChoice) : HealthSettingsEvent
    data class QuitAgeChanged(val quitAge: String) : HealthSettingsEvent
    data class AlcoholSelected(val alcohol: AlcoholConsumption) : HealthSettingsEvent
    data class PhysicalActivitySelected(val level: PhysicalActivityLevel) : HealthSettingsEvent
    data class ConditionToggled(val condition: HealthCondition) : HealthSettingsEvent
}
