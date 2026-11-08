package de.chrisander.timeoflife.ui.onboarding

import de.chrisander.timeoflife.domain.model.profile.Sex
import de.chrisander.timeoflife.ui.healthsettings.HealthSettingsEvent
import kotlinx.datetime.LocalDate

sealed interface OnboardingEvent {
    data class NameChanged(val name: String) : OnboardingEvent
    data class SexSelected(val sex: Sex) : OnboardingEvent
    data class BirthDateSelected(val birthDate: LocalDate) : OnboardingEvent
    data object Continue : OnboardingEvent
    data object Back : OnboardingEvent
    data class Health(val event: HealthSettingsEvent) : OnboardingEvent
    data object Submit : OnboardingEvent
    data object DismissError : OnboardingEvent
}
