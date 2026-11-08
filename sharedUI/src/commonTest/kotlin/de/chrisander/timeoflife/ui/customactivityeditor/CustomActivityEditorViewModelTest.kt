package de.chrisander.timeoflife.ui.customactivityeditor

import de.chrisander.timeoflife.domain.model.activity.Activity
import de.chrisander.timeoflife.domain.model.activity.ActivityId
import de.chrisander.timeoflife.domain.model.activity.ActivitySchedule
import de.chrisander.timeoflife.domain.model.activity.Recurrence
import de.chrisander.timeoflife.domain.model.profile.Sex
import de.chrisander.timeoflife.domain.model.profile.UserProfile
import de.chrisander.timeoflife.testing.FakeUserProfileRepository
import de.chrisander.timeoflife.ui.customactivityeditor.model.IntervalUnit
import de.chrisander.timeoflife.ui.navigation.CustomActivitiesRoute
import de.chrisander.timeoflife.ui.navigation.CustomActivityEditorRoute
import de.chrisander.timeoflife.ui.navigation.Navigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class CustomActivityEditorViewModelTest {

    private val piano = Activity.CustomActivity(
        ActivityId("custom:piano"),
        "Piano",
        ActivitySchedule(
            Recurrence(90.minutes, DateTimeUnit.DAY * 14),
            fromAge = 10,
            untilAge = 18
        ),
    )
    private val profile = UserProfile("Chris", Sex.MALE, LocalDate(1990, 5, 17)).let {
        it.copy(activities = it.activities + piano)
    }
    private val repository = FakeUserProfileRepository(profile)
    private val navigator = Navigator().apply { navigateTo(CustomActivitiesRoute) }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(activityId: String? = null): CustomActivityEditorViewModel {
        val route = CustomActivityEditorRoute(activityId)
        navigator.navigateTo(route)
        return CustomActivityEditorViewModel(route, repository, navigator)
    }

    private fun CustomActivityEditorViewModel.state() = assertNotNull(uiState.value)

    @Test
    fun addsNewActivityAfterTheExistingOnes() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(CustomActivityEditorEvent.NameChanged(" Football "))
        viewModel.onEvent(CustomActivityEditorEvent.UnitSelected(IntervalUnit.WEEK))
        viewModel.onEvent(CustomActivityEditorEvent.DurationSelected(180))
        viewModel.onEvent(CustomActivityEditorEvent.FromAgeChanged("8"))

        viewModel.onEvent(CustomActivityEditorEvent.Save)

        val activities = assertNotNull(repository.profile.value).activities
        assertEquals(profile.activities, activities.dropLast(1))
        val added = activities.last() as Activity.CustomActivity
        assertEquals("Football", added.name)
        assertEquals(
            ActivitySchedule(Recurrence(3.hours, DateTimeUnit.WEEK), fromAge = 8),
            added.schedule
        )
        assertEquals(listOf(CustomActivitiesRoute), navigator.backStack.toList())
    }

    @Test
    fun editingFillsTheFormFromTheActivity() {
        val state = createViewModel(piano.id.value).state()

        assertFalse(state.isNew)
        assertEquals("Piano", state.name)
        assertEquals(90, state.durationMinutes)
        assertEquals("2", state.every)
        assertEquals(IntervalUnit.WEEK, state.unit)
        assertEquals("10", state.fromAge)
        assertEquals("18", state.untilAge)
    }

    @Test
    fun savingAnEditReplacesTheActivityInPlace() = runTest {
        val viewModel = createViewModel(piano.id.value)
        viewModel.onEvent(CustomActivityEditorEvent.UntilAgeChanged(""))
        viewModel.onEvent(CustomActivityEditorEvent.Save)

        val expected = piano.copy(schedule = piano.schedule.copy(untilAge = null))
        assertEquals(
            profile.activities.dropLast(1) + expected,
            repository.profile.value?.activities
        )
    }

    @Test
    fun deleteRemovesTheActivityAfterConfirmation() = runTest {
        val viewModel = createViewModel(piano.id.value)

        viewModel.onEvent(CustomActivityEditorEvent.Delete)
        assertTrue(viewModel.state().showDeleteConfirmation)
        assertEquals(profile, repository.profile.value)

        viewModel.onEvent(CustomActivityEditorEvent.ConfirmDelete)
        assertEquals(profile.activities.dropLast(1), repository.profile.value?.activities)
        assertEquals(listOf(CustomActivitiesRoute), navigator.backStack.toList())
    }

    @Test
    fun durationOptionsGetCoarserAndEndAtTheInterval() {
        val viewModel = createViewModel()
        viewModel.onEvent(CustomActivityEditorEvent.UnitSelected(IntervalUnit.DAY))

        val options = viewModel.state().durationOptions
        assertEquals(listOf(5, 10, 15), options.take(3))
        assertEquals(listOf(55, 60, 75, 90), options.subList(10, 14))
        assertEquals(24 * 60, options.last())
        assertEquals(options.sorted().distinct(), options)
    }

    @Test
    fun summaryShowsDurationOnceEveryInterval() {
        val viewModel = createViewModel()
        viewModel.onEvent(CustomActivityEditorEvent.UnitSelected(IntervalUnit.DAY))
        viewModel.onEvent(CustomActivityEditorEvent.EveryChanged("4"))
        viewModel.onEvent(CustomActivityEditorEvent.DurationSelected(180))

        assertEquals(Recurrence(3.hours, DateTimeUnit.DAY * 4), viewModel.state().recurrence)

        viewModel.onEvent(CustomActivityEditorEvent.EveryChanged(""))
        assertEquals(null, viewModel.state().recurrence)
    }

    @Test
    fun shorterIntervalClampsTheDuration() {
        val viewModel = createViewModel()
        viewModel.onEvent(CustomActivityEditorEvent.NameChanged("Reading"))
        viewModel.onEvent(CustomActivityEditorEvent.UnitSelected(IntervalUnit.WEEK))
        viewModel.onEvent(CustomActivityEditorEvent.DurationSelected(40 * 60))

        viewModel.onEvent(CustomActivityEditorEvent.UnitSelected(IntervalUnit.DAY))

        assertEquals(24 * 60, viewModel.state().durationMinutes)
        assertTrue(viewModel.state().canSave)
    }

    @Test
    fun durationOffTheGridIsKeptWhenEditing() {
        repository.profile.value = profile.copy(
            activities = listOf(
                piano.copy(schedule = ActivitySchedule(Recurrence(47.minutes, DateTimeUnit.DAY))),
            ),
        )

        val state = createViewModel(piano.id.value).state()

        assertEquals(47, state.durationMinutes)
        assertTrue(47 in state.durationOptions)
    }

    @Test
    fun startAgeMustBeBeforeEndAge() {
        val viewModel = createViewModel()
        viewModel.onEvent(CustomActivityEditorEvent.NameChanged("Reading"))

        viewModel.onEvent(CustomActivityEditorEvent.FromAgeChanged("20"))
        viewModel.onEvent(CustomActivityEditorEvent.UntilAgeChanged("20"))
        assertTrue(viewModel.state().ageError)
        assertFalse(viewModel.state().canSave)

        viewModel.onEvent(CustomActivityEditorEvent.UntilAgeChanged("21"))
        assertFalse(viewModel.state().ageError)
        assertTrue(viewModel.state().canSave)
    }

    @Test
    fun yearIntervalIsStoredAsTwelveMonths() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(CustomActivityEditorEvent.NameChanged("Holiday"))
        viewModel.onEvent(CustomActivityEditorEvent.UnitSelected(IntervalUnit.YEAR))
        viewModel.onEvent(CustomActivityEditorEvent.DurationSelected(336 * 60))

        viewModel.onEvent(CustomActivityEditorEvent.Save)

        val added = repository.profile.value?.activities?.last() as Activity.CustomActivity
        assertEquals(Recurrence(336.hours, DateTimeUnit.MONTH * 12), added.schedule.recurrence)
    }
}
