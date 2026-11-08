package de.chrisander.timeoflife.ui.onboarding

import androidx.datastore.core.IOException
import de.chrisander.timeoflife.data.repository.UserProfileRepository
import de.chrisander.timeoflife.domain.model.lifespan.factor.AlcoholConsumption
import de.chrisander.timeoflife.domain.model.lifespan.factor.BodyMeasurements
import de.chrisander.timeoflife.domain.model.lifespan.factor.HealthCondition
import de.chrisander.timeoflife.domain.model.lifespan.factor.HealthConditions
import de.chrisander.timeoflife.domain.model.lifespan.factor.PhysicalActivityLevel
import de.chrisander.timeoflife.domain.model.lifespan.factor.SmokingStatus
import de.chrisander.timeoflife.domain.model.profile.LifespanFactors
import de.chrisander.timeoflife.domain.model.profile.Sex
import de.chrisander.timeoflife.domain.model.profile.UserProfile
import de.chrisander.timeoflife.ui.healthsettings.HealthSettingsEvent
import de.chrisander.timeoflife.ui.healthsettings.HealthSettingsHandler
import de.chrisander.timeoflife.ui.healthsettings.model.SmokingChoice
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
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

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

    private val clock = object : Clock {
        override fun now(): Instant =
            LocalDateTime(2026, 11, 5, 12, 0).toInstant(TimeZone.currentSystemDefault())
    }

    private fun createViewModel() =
        OnboardingViewModel(repository, navigator, HealthSettingsHandler(), clock)

    private fun OnboardingViewModel.fillPersonal(name: String = "Chris", sex: Sex = Sex.MALE) {
        onEvent(OnboardingEvent.NameChanged(name))
        onEvent(OnboardingEvent.SexSelected(sex))
        onEvent(OnboardingEvent.BirthDateSelected(LocalDate(1990, 5, 17)))
    }

    @Test
    fun cannotContinueUntilAllFieldsAreFilled() {
        val viewModel = createViewModel()
        assertFalse(viewModel.uiState.value.canContinue)

        viewModel.onEvent(OnboardingEvent.NameChanged("Chris"))
        viewModel.onEvent(OnboardingEvent.SexSelected(Sex.MALE))
        viewModel.onEvent(OnboardingEvent.Continue)
        assertFalse(viewModel.uiState.value.canContinue)
        assertEquals(OnboardingStep.PERSONAL, viewModel.uiState.value.step)

        viewModel.onEvent(OnboardingEvent.BirthDateSelected(LocalDate(1990, 5, 17)))
        viewModel.onEvent(OnboardingEvent.Continue)
        assertEquals(OnboardingStep.HEALTH, viewModel.uiState.value.step)
    }

    @Test
    fun blankNameCannotContinue() {
        val viewModel = createViewModel()
        viewModel.fillPersonal(name = "   ")

        assertFalse(viewModel.uiState.value.canContinue)
    }

    @Test
    fun backReturnsToPersonalStepAndKeepsInput() {
        val viewModel = createViewModel()
        viewModel.fillPersonal()
        viewModel.onEvent(OnboardingEvent.Continue)
        viewModel.onEvent(OnboardingEvent.Health(HealthSettingsEvent.HeightChanged("180")))

        viewModel.onEvent(OnboardingEvent.Back)

        assertEquals(OnboardingStep.PERSONAL, viewModel.uiState.value.step)
        assertEquals("180", viewModel.uiState.value.health.height)
    }

    @Test
    fun submitWithDefaultsSavesProfileAndNavigatesHome() = runTest {
        val viewModel = createViewModel()
        viewModel.fillPersonal(sex = Sex.DIVERSE)
        viewModel.onEvent(OnboardingEvent.Continue)
        viewModel.onEvent(OnboardingEvent.Health(HealthSettingsEvent.CountrySelected(null)))

        viewModel.onEvent(OnboardingEvent.Submit)

        assertEquals(
            UserProfile("Chris", Sex.DIVERSE, LocalDate(1990, 5, 17)),
            repository.profile.value
        )
        assertEquals(listOf(HomeRoute), navigator.backStack.toList())
    }

    @Test
    fun submitSavesAllHealthFactors() = runTest {
        val viewModel = createViewModel()
        viewModel.fillPersonal()
        viewModel.onEvent(OnboardingEvent.Continue)
        viewModel.onEvent(OnboardingEvent.Health(HealthSettingsEvent.CountrySelected("DE")))
        viewModel.onEvent(OnboardingEvent.Health(HealthSettingsEvent.HeightChanged("180")))
        viewModel.onEvent(OnboardingEvent.Health(HealthSettingsEvent.WeightChanged("81,5")))
        viewModel.onEvent(OnboardingEvent.Health(HealthSettingsEvent.SmokingSelected(SmokingChoice.FORMER)))
        viewModel.onEvent(OnboardingEvent.Health(HealthSettingsEvent.QuitAgeChanged("28")))
        viewModel.onEvent(
            OnboardingEvent.Health(
                HealthSettingsEvent.AlcoholSelected(
                    AlcoholConsumption.MODERATE
                )
            )
        )
        viewModel.onEvent(
            OnboardingEvent.Health(
                HealthSettingsEvent.PhysicalActivitySelected(
                    PhysicalActivityLevel.HIGH
                )
            ),
        )
        viewModel.onEvent(
            OnboardingEvent.Health(
                HealthSettingsEvent.ConditionToggled(
                    HealthCondition.DIABETES
                )
            )
        )
        viewModel.onEvent(
            OnboardingEvent.Health(
                HealthSettingsEvent.ConditionToggled(
                    HealthCondition.STROKE
                )
            )
        )
        viewModel.onEvent(
            OnboardingEvent.Health(
                HealthSettingsEvent.ConditionToggled(
                    HealthCondition.STROKE
                )
            )
        )

        viewModel.onEvent(OnboardingEvent.Submit)

        val expected = LifespanFactors(
            countryCode = "DE",
            body = BodyMeasurements(heightCm = 180.0, weightKg = 81.5),
            smoking = SmokingStatus.Former(quitAge = 28),
            alcohol = AlcoholConsumption.MODERATE,
            physicalActivity = PhysicalActivityLevel.HIGH,
            conditions = HealthConditions(setOf(HealthCondition.DIABETES)),
        )
        assertEquals(expected, repository.profile.value?.lifespanFactors)
    }

    @Test
    fun onlyHeightOrWeightCannotBeSubmitted() {
        val viewModel = createViewModel()
        viewModel.fillPersonal()
        viewModel.onEvent(OnboardingEvent.Continue)

        viewModel.onEvent(OnboardingEvent.Health(HealthSettingsEvent.HeightChanged("180")))
        assertTrue(viewModel.uiState.value.health.weightError)
        assertFalse(viewModel.uiState.value.canSubmit)

        viewModel.onEvent(OnboardingEvent.Health(HealthSettingsEvent.WeightChanged("75")))
        assertTrue(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun quitAgeMustBeBetweenOneAndCurrentAge() {
        val viewModel = createViewModel()
        viewModel.fillPersonal()
        viewModel.onEvent(OnboardingEvent.Continue)
        viewModel.onEvent(OnboardingEvent.Health(HealthSettingsEvent.SmokingSelected(SmokingChoice.FORMER)))
        assertFalse(viewModel.uiState.value.canSubmit)

        viewModel.onEvent(OnboardingEvent.Health(HealthSettingsEvent.QuitAgeChanged("37")))
        assertFalse(viewModel.uiState.value.canSubmit)

        viewModel.onEvent(OnboardingEvent.Health(HealthSettingsEvent.QuitAgeChanged("36")))
        assertTrue(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun numberInputKeepsOnlyDigitsAndOneSeparator() {
        val viewModel = createViewModel()

        viewModel.onEvent(OnboardingEvent.Health(HealthSettingsEvent.WeightChanged("7a2.5.1")))

        assertEquals("72.51", viewModel.uiState.value.health.weight)
    }

    @Test
    fun failedSaveShowsError() = runTest {
        repository.failOnSave = true
        val viewModel = createViewModel()
        viewModel.fillPersonal()
        viewModel.onEvent(OnboardingEvent.Continue)

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
