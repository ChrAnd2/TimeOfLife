package de.chrisander.timeoflife.ui.profiledata

import de.chrisander.timeoflife.domain.model.profile.Sex
import de.chrisander.timeoflife.ui.healthsettings.HealthSettingsEvent
import kotlinx.datetime.LocalDate

sealed interface ProfileDataEvent {
    data class NameChanged(val name: String) : ProfileDataEvent
    data class SexSelected(val sex: Sex) : ProfileDataEvent
    data class BirthDateSelected(val birthDate: LocalDate) : ProfileDataEvent
    data class Health(val event: HealthSettingsEvent) : ProfileDataEvent
    data object Save : ProfileDataEvent
    data object Back : ProfileDataEvent
    data object DismissError : ProfileDataEvent
}
