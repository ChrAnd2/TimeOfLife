package de.chrisander.timeoflife.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.chrisander.timeoflife.data.repository.UserProfileRepository
import de.chrisander.timeoflife.domain.model.activity.Activity
import de.chrisander.timeoflife.ui.navigation.CustomActivitiesRoute
import de.chrisander.timeoflife.ui.navigation.Navigator
import de.chrisander.timeoflife.ui.navigation.ProfileDataRoute
import de.chrisander.timeoflife.ui.profile.model.ProfileSetting
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

class ProfileViewModel(
    userProfileRepository: UserProfileRepository,
    private val clock: Clock,
    private val navigator: Navigator,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState?> = userProfileRepository.userProfile
        .filterNotNull()
        .map { profile ->
            val today = clock.todayIn(TimeZone.currentSystemDefault())
            ProfileUiState(
                name = profile.name,
                birthDate = profile.birthDate,
                age = profile.birthDate.yearsUntil(today),
                customActivityCount = profile.activities.count { it is Activity.CustomActivity },
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5.seconds),
            initialValue = null,
        )

    fun onSettingClick(setting: ProfileSetting) {
        navigator.navigateTo(
            when (setting) {
                ProfileSetting.PERSONAL_DATA -> ProfileDataRoute
                ProfileSetting.CUSTOM_ACTIVITIES -> CustomActivitiesRoute
            },
        )
    }

    fun onBack() {
        navigator.goBack()
    }
}
