package de.chrisander.timeoflife.ui.profile

import de.chrisander.timeoflife.domain.model.activity.Activity
import de.chrisander.timeoflife.domain.model.activity.ActivityId
import de.chrisander.timeoflife.domain.model.activity.ActivitySchedule
import de.chrisander.timeoflife.domain.model.activity.Recurrence
import de.chrisander.timeoflife.domain.model.profile.Sex
import de.chrisander.timeoflife.domain.model.profile.UserProfile
import de.chrisander.timeoflife.testing.FakeUserProfileRepository
import de.chrisander.timeoflife.ui.navigation.CustomActivitiesRoute
import de.chrisander.timeoflife.ui.navigation.Navigator
import de.chrisander.timeoflife.ui.navigation.ProfileDataRoute
import de.chrisander.timeoflife.ui.navigation.ProfileRoute
import de.chrisander.timeoflife.ui.profile.model.ProfileSetting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val football = Activity.CustomActivity(
        ActivityId("custom:1"),
        "Football",
        ActivitySchedule(Recurrence(3.hours, DateTimeUnit.WEEK)),
    )
    private val profile = UserProfile("Chris", Sex.MALE, LocalDate(1990, 5, 17)).let {
        it.copy(activities = it.activities + football)
    }
    private val repository = FakeUserProfileRepository(profile)
    private val navigator = Navigator().apply { navigateTo(ProfileRoute) }
    private val clock = object : Clock {
        override fun now(): Instant =
            LocalDateTime(2026, 11, 5, 12, 0).toInstant(TimeZone.currentSystemDefault())
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun showsNameAgeAndCustomActivityCount() = runTest {
        val viewModel = ProfileViewModel(repository, clock, navigator)

        assertEquals(
            ProfileUiState(
                name = "Chris",
                birthDate = LocalDate(1990, 5, 17),
                age = 36,
                customActivityCount = 1
            ),
            viewModel.uiState.filterNotNull().first(),
        )
    }

    @Test
    fun settingsOpenTheirScreens() {
        val viewModel = ProfileViewModel(repository, clock, navigator)

        viewModel.onSettingClick(ProfileSetting.PERSONAL_DATA)
        assertEquals(ProfileDataRoute, navigator.backStack.last())

        navigator.goBack()
        viewModel.onSettingClick(ProfileSetting.CUSTOM_ACTIVITIES)
        assertEquals(CustomActivitiesRoute, navigator.backStack.last())
    }
}
