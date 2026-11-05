package de.chrisander.timeoflife.ui.home

import de.chrisander.timeoflife.data.datasource.StaticBaselineLifeExpectancyProvider
import de.chrisander.timeoflife.data.repository.UserProfileRepository
import de.chrisander.timeoflife.domain.model.activity.ActivityId
import de.chrisander.timeoflife.domain.model.profile.LifespanSetting
import de.chrisander.timeoflife.domain.model.profile.Sex
import de.chrisander.timeoflife.domain.model.profile.UserProfile
import de.chrisander.timeoflife.domain.usecase.GetLifeTimeBreakdownUseCase
import de.chrisander.timeoflife.domain.usecase.LifeTimeCalculator
import de.chrisander.timeoflife.domain.usecase.LifespanEstimator
import de.chrisander.timeoflife.domain.usecase.StudyBasedLifespanEstimator
import de.chrisander.timeoflife.ui.navigation.ActivityDetailRoute
import de.chrisander.timeoflife.ui.navigation.Navigator
import de.chrisander.timeoflife.widget.domain.model.LifeSegment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.math.abs
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

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
    fun showsNameAndTimeOfDay() = runTest {
        repository.profile.value = profile

        val uiState = loadUiState(at = LocalDateTime(2026, 5, 16, 19, 30))

        assertEquals("Chris", uiState.name)
        assertEquals(TimeOfDay.EVENING, uiState.timeOfDay)
    }

    @Test
    fun livedAndRemainingYearsAddUpToTheLifespan() = runTest {
        repository.profile.value = profile.copy(lifespanSetting = LifespanSetting.Manual(80.0))

        val life = loadUiState(at = LocalDateTime(2026, 5, 16, 9, 0)).life!!

        assertEquals(80.0, life.lifespanYears)
        // The calculator works in whole days.
        assertClose(80.0, life.yearsLived + life.yearsRemaining, tolerance = ONE_DAY_IN_YEARS)
        assertEquals(0.0, life.yearsBeyondLifespan)
    }

    @Test
    fun activitiesSplitTheTimeLivedAndAheadAndAreSortedByTheirTimeSoFar() = runTest {
        repository.profile.value = profile

        val life = loadUiState(at = LocalDateTime(2026, 5, 16, 9, 0)).life!!

        assertEquals(life.activities.sortedByDescending { it.yearsSpent }, life.activities)
        assertClose(1.0, life.activities.sumOf { it.segment.fraction })
        assertClose(life.yearsLived, life.activities.sumOf { it.yearsSpent })
        assertClose(life.yearsRemaining, life.activities.sumOf { it.yearsAhead })
        assertTrue(life.activities.any { it.segment is LifeSegment.FreeTime })
    }

    @Test
    fun outlivedLifespanHasBonusYearsAndNothingAhead() = runTest {
        repository.profile.value = profile.copy(lifespanSetting = LifespanSetting.Manual(30.0))

        val life = loadUiState(at = LocalDateTime(2026, 5, 16, 9, 0)).life!!

        assertTrue(life.yearsBeyondLifespan > 5.9)
        assertEquals(0.0, life.yearsRemaining)
        assertTrue(life.activities.isNotEmpty())
        assertTrue(life.activities.all { it.yearsAhead == 0.0 })
    }

    @Test
    fun clickingAnActivityOpensItsDetails() {
        val navigator = Navigator()
        val viewModel = homeViewModel(LocalDateTime(2026, 5, 16, 9, 0), navigator)

        viewModel.onActivityClick(ActivityId("default:SLEEP"))
        viewModel.onActivityClick(null)

        assertEquals(
            listOf(ActivityDetailRoute("default:SLEEP"), ActivityDetailRoute(null)),
            navigator.backStack.toList(),
        )
    }

    @Test
    fun timeOfDayFollowsTheHour() {
        assertEquals(TimeOfDay.NIGHT, TimeOfDay.of(4))
        assertEquals(TimeOfDay.MORNING, TimeOfDay.of(5))
        assertEquals(TimeOfDay.AFTERNOON, TimeOfDay.of(12))
        assertEquals(TimeOfDay.EVENING, TimeOfDay.of(18))
        assertEquals(TimeOfDay.NIGHT, TimeOfDay.of(23))
    }

    private suspend fun loadUiState(at: LocalDateTime): HomeUiState =
        homeViewModel(at).uiState.first { it.life != null }

    private fun homeViewModel(at: LocalDateTime, navigator: Navigator = Navigator()): HomeViewModel {
        val clock = object : Clock {
            override fun now(): Instant = at.toInstant(TimeZone.currentSystemDefault())
        }
        val estimator: LifespanEstimator =
            StudyBasedLifespanEstimator(StaticBaselineLifeExpectancyProvider)
        val getLifeTimeBreakdown = GetLifeTimeBreakdownUseCase(estimator, LifeTimeCalculator())
        return HomeViewModel(repository, getLifeTimeBreakdown, clock, navigator)
    }

    private fun assertClose(expected: Double, actual: Double, tolerance: Double = 1e-6) =
        assertTrue(abs(expected - actual) < tolerance, "expected $expected but was $actual")

    private class FakeUserProfileRepository : UserProfileRepository {
        val profile = MutableStateFlow<UserProfile?>(null)
        override val userProfile: Flow<UserProfile?> = profile
        override val isOnboardingCompleted: Flow<Boolean> = profile.map { it != null }

        override suspend fun saveUserProfile(profile: UserProfile) {
            this.profile.value = profile
        }
    }
}

private const val ONE_DAY_IN_YEARS = 1 / 365.0
