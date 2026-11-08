package de.chrisander.timeoflife.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.chrisander.timeoflife.data.repository.UserProfileRepository
import de.chrisander.timeoflife.domain.model.activity.ActivityId
import de.chrisander.timeoflife.domain.usecase.GetLifeTimeBreakdownUseCase
import de.chrisander.timeoflife.ui.home.mapper.toLifeOverview
import de.chrisander.timeoflife.ui.navigation.ActivityDetailRoute
import de.chrisander.timeoflife.ui.navigation.Navigator
import de.chrisander.timeoflife.ui.navigation.ProfileRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

class HomeViewModel(
    userProfileRepository: UserProfileRepository,
    private val getLifeTimeBreakdown: GetLifeTimeBreakdownUseCase,
    private val clock: Clock,
    private val navigator: Navigator,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState?> = userProfileRepository.userProfile
        .filterNotNull()
        .map { profile ->
            val now = clock.now().toLocalDateTime(TimeZone.currentSystemDefault())
            HomeUiState(
                name = profile.name,
                timeOfDay = TimeOfDay.of(now.hour),
                life = getLifeTimeBreakdown(profile, now.date).toLifeOverview(),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5.seconds),
            initialValue = null,
        )

    /** @param activityId `null` opens free time. */
    fun onActivityClick(activityId: ActivityId?) {
        navigator.navigateTo(ActivityDetailRoute(activityId?.value))
    }

    fun onProfileClick() {
        navigator.navigateTo(ProfileRoute)
    }
}
