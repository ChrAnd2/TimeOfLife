package de.chrisander.timeoflife.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.chrisander.timeoflife.data.repository.UserProfileRepository
import de.chrisander.timeoflife.domain.model.activity.ActivityId
import de.chrisander.timeoflife.domain.usecase.GetLifeTimeBreakdownUseCase
import de.chrisander.timeoflife.ui.navigation.ActivityDetailRoute
import de.chrisander.timeoflife.ui.navigation.Navigator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

class ActivityDetailViewModel(
    route: ActivityDetailRoute,
    userProfileRepository: UserProfileRepository,
    private val getLifeTimeBreakdown: GetLifeTimeBreakdownUseCase,
    private val clock: Clock,
    private val navigator: Navigator,
) : ViewModel() {

    private val activityId = route.activityId?.let(::ActivityId)

    val uiState: StateFlow<ActivityDetailUiState> = userProfileRepository.userProfile
        .filterNotNull()
        .map { profile ->
            val today = clock.todayIn(TimeZone.currentSystemDefault())
            ActivityDetailUiState(
                isLoading = false,
                detail = getLifeTimeBreakdown(profile, today).activityDetail(activityId),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5.seconds),
            initialValue = ActivityDetailUiState(),
        )

    fun onBack() {
        navigator.goBack()
    }
}
