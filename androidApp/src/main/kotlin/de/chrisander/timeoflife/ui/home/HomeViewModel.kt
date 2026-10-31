package de.chrisander.timeoflife.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.chrisander.timeoflife.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.yearsUntil
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

class HomeViewModel(
    userProfileRepository: UserProfileRepository,
    private val clock: Clock,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = userProfileRepository.userProfile
        .filterNotNull()
        .map { profile ->
            HomeUiState(
                name = profile.name,
                age = profile.birthDate.yearsUntil(clock.todayIn(TimeZone.currentSystemDefault())),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5.seconds),
            initialValue = HomeUiState(),
        )
}

