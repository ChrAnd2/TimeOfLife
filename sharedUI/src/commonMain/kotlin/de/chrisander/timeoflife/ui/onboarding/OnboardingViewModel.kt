package de.chrisander.timeoflife.ui.onboarding

import androidx.datastore.core.IOException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.chrisander.timeoflife.data.repository.UserProfileRepository
import de.chrisander.timeoflife.domain.model.profile.UserProfile
import de.chrisander.timeoflife.ui.healthsettings.HealthSettingsHandler
import de.chrisander.timeoflife.ui.healthsettings.HealthSettingsUiState
import de.chrisander.timeoflife.ui.healthsettings.deviceCountryCode
import de.chrisander.timeoflife.ui.navigation.HomeRoute
import de.chrisander.timeoflife.ui.navigation.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

class OnboardingViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val navigator: Navigator,
    private val healthSettingsHandler: HealthSettingsHandler,
    clock: Clock,
) : ViewModel() {

    private val today = clock.todayIn(TimeZone.currentSystemDefault())

    private val _uiState = MutableStateFlow(
        validate(
            OnboardingUiState(
                health = HealthSettingsUiState(countryCode = deviceCountryCode()),
            ),
        ),
    )
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.NameChanged -> updateState { it.copy(name = event.name) }
            is OnboardingEvent.SexSelected -> updateState { it.copy(sex = event.sex) }
            is OnboardingEvent.BirthDateSelected -> updateState { it.copy(birthDate = event.birthDate) }
            OnboardingEvent.Continue -> onContinue()
            OnboardingEvent.Back -> updateState { it.copy(step = OnboardingStep.PERSONAL) }
            is OnboardingEvent.Health -> updateState {
                it.copy(health = healthSettingsHandler.handleEvent(it.health, event.event))
            }
            OnboardingEvent.Submit -> onSubmit()
            OnboardingEvent.DismissError -> updateState { it.copy(showSaveError = false) }
        }
    }

    private fun updateState(transform: (OnboardingUiState) -> OnboardingUiState) {
        _uiState.update { validate(transform(it)) }
    }

    private fun onContinue() {
        updateState { if (it.canContinue) it.copy(step = OnboardingStep.HEALTH) else it }
    }

    private fun onSubmit() {
        val state = _uiState.value
        if (!state.canSubmit) return
        val birthDate = checkNotNull(state.birthDate)
        val profile = UserProfile(
            name = state.name.trim(),
            sex = checkNotNull(state.sex),
            birthDate = birthDate,
            lifespanFactors = healthSettingsHandler.toLifespanFactors(
                state.health,
                birthDate,
                today
            ),
        )

        updateState { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                userProfileRepository.saveUserProfile(profile)
                updateState { it.copy(isSaving = false) }
                navigator.replaceAll(HomeRoute)
            } catch (_: IOException) {
                updateState { it.copy(isSaving = false, showSaveError = true) }
            }
        }
    }

    private fun validate(state: OnboardingUiState): OnboardingUiState {
        val canContinue = state.name.isNotBlank() && state.sex != null && state.birthDate != null
        val health = healthSettingsHandler.validate(state.health, state.birthDate, today)
        val isHealthValid = healthSettingsHandler.isValid(health, state.birthDate, today)
        return state.copy(
            canContinue = canContinue,
            canSubmit = canContinue && isHealthValid && !state.isSaving,
            health = health,
        )
    }
}
