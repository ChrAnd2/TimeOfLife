package de.chrisander.timeoflife.ui.profiledata

import de.chrisander.timeoflife.domain.model.profile.Sex
import de.chrisander.timeoflife.ui.healthsettings.HealthSettingsUiState
import kotlinx.datetime.LocalDate

data class ProfileDataUiState(
    val name: String,
    val sex: Sex,
    val birthDate: LocalDate,
    val health: HealthSettingsUiState,
    val canSave: Boolean = false,
    val isSaving: Boolean = false,
    val showSaveError: Boolean = false,
)
