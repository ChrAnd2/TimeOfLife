package de.chrisander.timeoflife.ui.customactivities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.chrisander.timeoflife.data.repository.UserProfileRepository
import de.chrisander.timeoflife.domain.model.activity.Activity
import de.chrisander.timeoflife.domain.model.activity.ActivityId
import de.chrisander.timeoflife.ui.navigation.CustomActivityEditorRoute
import de.chrisander.timeoflife.ui.navigation.Navigator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

class CustomActivitiesViewModel(
    userProfileRepository: UserProfileRepository,
    private val navigator: Navigator,
) : ViewModel() {

    val uiState: StateFlow<CustomActivitiesUiState?> = userProfileRepository.userProfile
        .filterNotNull()
        .map { profile ->
            CustomActivitiesUiState(profile.activities.filterIsInstance<Activity.CustomActivity>())
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5.seconds),
            initialValue = null,
        )

    fun onActivityClick(activityId: ActivityId) {
        navigator.navigateTo(CustomActivityEditorRoute(activityId.value))
    }

    fun onAddClick() {
        navigator.navigateTo(CustomActivityEditorRoute(activityId = null))
    }

    fun onBack() {
        navigator.goBack()
    }
}
