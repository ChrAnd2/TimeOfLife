package de.chrisander.timeoflife.ui.onboarding

import de.chrisander.timeoflife.data.repository.UserProfileRepository
import de.chrisander.timeoflife.domain.model.Sex
import de.chrisander.timeoflife.domain.model.UserProfile
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
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import java.io.IOException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val repository = FakeUserProfileRepository()
    private val navigator = Navigator().apply { navigateTo(OnboardingRoute) }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = OnboardingViewModel(repository, navigator)

    @Test
    fun cannotSubmitUntilAllFieldsAreFilled() {
        val viewModel = createViewModel()
        assertFalse(viewModel.uiState.value.canSubmit)

        viewModel.onEvent(OnboardingEvent.NameChanged("Chris"))
        viewModel.onEvent(OnboardingEvent.SexSelected(Sex.MALE))
        assertFalse(viewModel.uiState.value.canSubmit)

        viewModel.onEvent(
            OnboardingEvent.BirthDateSelected(LocalDate(1990, 5, 17))
        )
        assertTrue(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun blankNameCannotBeSubmitted() {
        val viewModel = createViewModel()
        viewModel.onEvent(OnboardingEvent.NameChanged("   "))
        viewModel.onEvent(OnboardingEvent.SexSelected(Sex.FEMALE))
        viewModel.onEvent(
            OnboardingEvent.BirthDateSelected(LocalDate(1990, 5, 17))
        )

        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun submitSavesProfileAndNavigatesHome() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(OnboardingEvent.NameChanged("Chris"))
        viewModel.onEvent(OnboardingEvent.SexSelected(Sex.DIVERSE))
        viewModel.onEvent(
            OnboardingEvent.BirthDateSelected(LocalDate(1990, 5, 17))
        )

        viewModel.onEvent(OnboardingEvent.Submit)

        assertEquals(UserProfile("Chris", Sex.DIVERSE, LocalDate(1990, 5, 17)), repository.profile.value)
        assertEquals(listOf(HomeRoute), navigator.backStack.toList())
    }

    @Test
    fun failedSaveShowsError() = runTest {
        repository.failOnSave = true
        val viewModel = createViewModel()
        viewModel.onEvent(OnboardingEvent.NameChanged("Chris"))
        viewModel.onEvent(OnboardingEvent.SexSelected(Sex.MALE))
        viewModel.onEvent(
            OnboardingEvent.BirthDateSelected(LocalDate(1990, 5, 17))
        )

        viewModel.onEvent(OnboardingEvent.Submit)

        assertTrue(viewModel.uiState.value.showSaveError)
        assertFalse(viewModel.uiState.value.isSaving)
        assertEquals(listOf(OnboardingRoute), navigator.backStack.toList())
    }

    private class FakeUserProfileRepository : UserProfileRepository {
        val profile = MutableStateFlow<UserProfile?>(null)
        var failOnSave = false

        override val userProfile: Flow<UserProfile?> = profile
        override val isOnboardingCompleted: Flow<Boolean> = profile.map { it != null }

        override suspend fun saveUserProfile(profile: UserProfile) {
            if (failOnSave) throw IOException("disk full")
            this.profile.value = profile
        }
    }
}
