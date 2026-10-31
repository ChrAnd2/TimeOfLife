package de.chrisander.timeoflife.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.chrisander.timeoflife.data.repository.UserProfileRepository
import de.chrisander.timeoflife.ui.navigation.HomeRoute
import de.chrisander.timeoflife.ui.navigation.Navigator
import de.chrisander.timeoflife.ui.navigation.OnboardingRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    userProfileRepository: UserProfileRepository,
    navigator: Navigator,
) : ViewModel() {
    val isLoading: StateFlow<Boolean> = flow {
        if (navigator.backStack.isEmpty()) {
            val isOnboardingCompleted = userProfileRepository.isOnboardingCompleted.first()
            navigator.replaceAll(if (isOnboardingCompleted) HomeRoute else OnboardingRoute)
        }
        emit(false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = true,
    )
}
