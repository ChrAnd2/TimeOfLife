package de.chrisander.timeoflife.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.chrisander.timeoflife.data.repository.UserProfileRepository
import de.chrisander.timeoflife.domain.model.profile.UserProfile
import de.chrisander.timeoflife.ui.navigation.HomeRoute
import de.chrisander.timeoflife.ui.navigation.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

class OnboardingViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val navigator: Navigator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onEvent(event: OnboardingEvent) {
        when(event) {
            is OnboardingEvent.BirthDateSelected -> _uiState.update { it.copy(birthDate = event.birthDate) }
            is OnboardingEvent.NameChanged -> _uiState.update { it.copy(name = event.name) }
            is OnboardingEvent.SexSelected -> _uiState.update { it.copy(sex = event.sex) }
            OnboardingEvent.Submit -> onSubmit()
            OnboardingEvent.DismissError -> _uiState.update { it.copy(showSaveError = false) }
        }
    }

    private fun onSubmit() {
        val state = _uiState.value
        if (!state.canSubmit) return
        val profile = UserProfile(
            name = state.name,
            sex = checkNotNull(state.sex),
            birthDate = checkNotNull(state.birthDate),
        )

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                userProfileRepository.saveUserProfile(profile)
                _uiState.update { it.copy(isSaving = false) }
                navigator.replaceAll(HomeRoute)
            } catch (_: IOException) {
                _uiState.update { it.copy(isSaving = false, showSaveError = true) }
            }
        }
    }
}
