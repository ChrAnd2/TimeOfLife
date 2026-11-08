package de.chrisander.timeoflife.ui.onboarding

import de.chrisander.timeoflife.domain.model.profile.Sex
import de.chrisander.timeoflife.ui.healthsettings.HealthSettingsUiState
import kotlinx.datetime.LocalDate

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.PERSONAL,
    val name: String = "",
    val sex: Sex? = null,
    val birthDate: LocalDate? = null,
    val health: HealthSettingsUiState = HealthSettingsUiState(),
    val canContinue: Boolean = false,
    val canSubmit: Boolean = false,
    val isSaving: Boolean = false,
    val showSaveError: Boolean = false,
)

enum class OnboardingStep {
    PERSONAL,
    HEALTH,
}
