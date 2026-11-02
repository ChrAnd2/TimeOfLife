package de.chrisander.timeoflife.ui.onboarding

import de.chrisander.timeoflife.domain.model.profile.Sex
import kotlinx.datetime.LocalDate

sealed interface OnboardingEvent {
    data class NameChanged(val name: String): OnboardingEvent
    data class SexSelected(val sex: Sex): OnboardingEvent
    data class BirthDateSelected(val birthDate: LocalDate): OnboardingEvent
    data object Submit: OnboardingEvent
    data object DismissError: OnboardingEvent
}