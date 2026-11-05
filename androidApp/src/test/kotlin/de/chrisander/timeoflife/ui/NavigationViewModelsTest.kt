package de.chrisander.timeoflife.ui

import de.chrisander.timeoflife.data.repository.UserProfileRepository
import de.chrisander.timeoflife.domain.model.profile.Sex
import de.chrisander.timeoflife.domain.model.profile.UserProfile
import de.chrisander.timeoflife.ui.navigation.HomeRoute
import de.chrisander.timeoflife.ui.navigation.Navigator
import de.chrisander.timeoflife.ui.navigation.OnboardingRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class NavigationViewModelsTest {

    private val profile = UserProfile("Chris", Sex.MALE, LocalDate(1990, 5, 17))
    private val repository = FakeUserProfileRepository()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startsWithOnboardingWithoutProfile() {
        val navigator = Navigator()
        MainViewModel(repository, navigator)

        assertEquals(listOf(OnboardingRoute), navigator.backStack.toList())
    }

    @Test
    fun startsWithHomeWhenProfileExists() {
        repository.profile.value = profile

        val navigator = Navigator()
        MainViewModel(repository, navigator)

        assertEquals(listOf(HomeRoute), navigator.backStack.toList())
    }

    private class FakeUserProfileRepository : UserProfileRepository {
        val profile = MutableStateFlow<UserProfile?>(null)

        override val userProfile: Flow<UserProfile?> = profile
        override val isOnboardingCompleted: Flow<Boolean> = profile.map { it != null }

        override suspend fun saveUserProfile(profile: UserProfile) {
            this.profile.value = profile
        }
    }
}
