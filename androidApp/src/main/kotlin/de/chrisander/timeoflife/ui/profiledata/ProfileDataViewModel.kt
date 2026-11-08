package de.chrisander.timeoflife.ui.profiledata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.chrisander.timeoflife.data.repository.UserProfileRepository
import de.chrisander.timeoflife.ui.healthsettings.HealthSettingsHandler
import de.chrisander.timeoflife.ui.navigation.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import java.io.IOException
import kotlin.time.Clock

class ProfileDataViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val navigator: Navigator,
    private val healthSettingsHandler: HealthSettingsHandler,
    clock: Clock,
) : ViewModel() {

    private val today = clock.todayIn(TimeZone.currentSystemDefault())

    private val _uiState = MutableStateFlow<ProfileDataUiState?>(null)

    val uiState: StateFlow<ProfileDataUiState?> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = userProfileRepository.userProfile.filterNotNull().first()
            _uiState.value = validate(
                ProfileDataUiState(
                    name = profile.name,
                    sex = profile.sex,
                    birthDate = profile.birthDate,
                    health = healthSettingsHandler.fromLifespanFactors(profile.lifespanFactors),
                ),
            )
        }
    }

    fun onEvent(event: ProfileDataEvent) {
        when (event) {
            is ProfileDataEvent.NameChanged -> updateState { it.copy(name = event.name) }
            is ProfileDataEvent.SexSelected -> updateState { it.copy(sex = event.sex) }
            is ProfileDataEvent.BirthDateSelected -> updateState { it.copy(birthDate = event.birthDate) }
            is ProfileDataEvent.Health -> updateState {
                it.copy(health = healthSettingsHandler.handleEvent(it.health, event.event))
            }

            ProfileDataEvent.Save -> onSave()
            ProfileDataEvent.Back -> navigator.goBack()
            ProfileDataEvent.DismissError -> updateState { it.copy(showSaveError = false) }
        }
    }

    private fun updateState(transform: (ProfileDataUiState) -> ProfileDataUiState) {
        _uiState.update { state -> state?.let { validate(transform(it)) } }
    }

    private fun onSave() {
        val state = _uiState.value ?: return
        if (!state.canSave) return
        updateState { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val stored = userProfileRepository.userProfile.filterNotNull().first()
                userProfileRepository.saveUserProfile(
                    stored.copy(
                        name = state.name.trim(),
                        sex = state.sex,
                        birthDate = state.birthDate,
                        lifespanFactors = healthSettingsHandler.toLifespanFactors(
                            state.health,
                            state.birthDate,
                            today,
                        ),
                    ),
                )
                updateState { it.copy(isSaving = false) }
                navigator.goBack()
            } catch (_: IOException) {
                updateState { it.copy(isSaving = false, showSaveError = true) }
            }
        }
    }

    private fun validate(state: ProfileDataUiState): ProfileDataUiState {
        val health = healthSettingsHandler.validate(state.health, state.birthDate, today)
        val isValid = state.name.isNotBlank() &&
                healthSettingsHandler.isValid(health, state.birthDate, today)
        return state.copy(health = health, canSave = isValid && !state.isSaving)
    }
}
