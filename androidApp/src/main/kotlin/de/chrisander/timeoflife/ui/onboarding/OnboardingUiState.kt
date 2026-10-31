package de.chrisander.timeoflife.ui.onboarding

import de.chrisander.timeoflife.domain.model.Sex
import kotlinx.datetime.LocalDate

data class OnboardingUiState(
    val name: String = "",
    val sex: Sex? = null,
    val birthDate: LocalDate? = null,
    val isSaving: Boolean = false,
    val showSaveError: Boolean = false,
) {
    val canSubmit: Boolean
        get() = name.isNotBlank() && sex != null && birthDate != null && !isSaving
}
