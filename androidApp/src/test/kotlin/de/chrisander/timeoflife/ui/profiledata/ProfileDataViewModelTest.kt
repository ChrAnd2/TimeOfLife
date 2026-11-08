package de.chrisander.timeoflife.ui.profiledata

import de.chrisander.timeoflife.domain.model.activity.Activity
import de.chrisander.timeoflife.domain.model.activity.ActivityId
import de.chrisander.timeoflife.domain.model.activity.ActivitySchedule
import de.chrisander.timeoflife.domain.model.activity.Recurrence
import de.chrisander.timeoflife.domain.model.lifespan.factor.AlcoholConsumption
import de.chrisander.timeoflife.domain.model.lifespan.factor.BodyMeasurements
import de.chrisander.timeoflife.domain.model.lifespan.factor.HealthCondition
import de.chrisander.timeoflife.domain.model.lifespan.factor.HealthConditions
import de.chrisander.timeoflife.domain.model.lifespan.factor.SmokingStatus
import de.chrisander.timeoflife.domain.model.profile.LifespanFactors
import de.chrisander.timeoflife.domain.model.profile.LifespanSetting
import de.chrisander.timeoflife.domain.model.profile.Sex
import de.chrisander.timeoflife.domain.model.profile.UserProfile
import de.chrisander.timeoflife.testing.FakeUserProfileRepository
import de.chrisander.timeoflife.ui.healthsettings.HealthSettingsEvent
import de.chrisander.timeoflife.ui.healthsettings.HealthSettingsHandler
import de.chrisander.timeoflife.ui.healthsettings.model.SmokingChoice
import de.chrisander.timeoflife.ui.navigation.Navigator
import de.chrisander.timeoflife.ui.navigation.ProfileDataRoute
import de.chrisander.timeoflife.ui.navigation.ProfileRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileDataViewModelTest {

    private val profile = UserProfile(
        name = "Chris",
        sex = Sex.MALE,
        birthDate = LocalDate(1990, 5, 17),
        lifespanSetting = LifespanSetting.Manual(90.0),
        lifespanFactors = LifespanFactors(
            countryCode = "DE",
            body = BodyMeasurements(heightCm = 182.0, weightKg = 78.5),
            smoking = SmokingStatus.Former(quitAge = 28),
            alcohol = AlcoholConsumption.LOW,
            conditions = HealthConditions(setOf(HealthCondition.STROKE)),
        ),
        activities = listOf(
            Activity.CustomActivity(
                ActivityId("custom:1"),
                "Football",
                ActivitySchedule(Recurrence(3.hours, DateTimeUnit.WEEK)),
            ),
        ),
    )
    private val repository = FakeUserProfileRepository(profile)
    private val navigator = Navigator().apply {
        navigateTo(ProfileRoute)
        navigateTo(ProfileDataRoute)
    }
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

    private fun createViewModel() =
        ProfileDataViewModel(repository, navigator, HealthSettingsHandler(), clock)

    @Test
    fun startsWithTheStoredProfile() {
        val state = assertNotNull(createViewModel().uiState.value)

        assertEquals("Chris", state.name)
        assertEquals(Sex.MALE, state.sex)
        assertEquals("182", state.health.height)
        assertEquals("78.5", state.health.weight)
        assertEquals(SmokingChoice.FORMER, state.health.smoking)
        assertEquals("28", state.health.quitAge)
        assertTrue(state.canSave)
    }

    @Test
    fun savingUnchangedFormKeepsTheProfile() = runTest {
        createViewModel().onEvent(ProfileDataEvent.Save)

        assertEquals(profile, repository.profile.value)
        assertEquals(listOf(ProfileRoute), navigator.backStack.toList())
    }

    @Test
    fun saveKeepsActivitiesAndLifespanSetting() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(ProfileDataEvent.NameChanged(" Christian "))
        viewModel.onEvent(ProfileDataEvent.Health(HealthSettingsEvent.SmokingSelected(SmokingChoice.NEVER)))

        viewModel.onEvent(ProfileDataEvent.Save)

        val expected = profile.copy(
            name = "Christian",
            lifespanFactors = profile.lifespanFactors.copy(smoking = SmokingStatus.Never),
        )
        assertEquals(expected, repository.profile.value)
    }

    @Test
    fun blankNameCannotBeSaved() {
        val viewModel = createViewModel()

        viewModel.onEvent(ProfileDataEvent.NameChanged(" "))
        viewModel.onEvent(ProfileDataEvent.Save)

        assertFalse(assertNotNull(viewModel.uiState.value).canSave)
        assertEquals(profile, repository.profile.value)
    }

    @Test
    fun failedSaveShowsErrorAndStays() = runTest {
        repository.failOnSave = true
        val viewModel = createViewModel()

        viewModel.onEvent(ProfileDataEvent.Save)

        assertTrue(assertNotNull(viewModel.uiState.value).showSaveError)
        assertEquals(ProfileDataRoute, navigator.backStack.last())
    }
}
