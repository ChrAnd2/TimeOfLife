package de.chrisander.timeoflife.ui.customactivityeditor

import androidx.datastore.core.IOException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.chrisander.timeoflife.data.repository.UserProfileRepository
import de.chrisander.timeoflife.domain.model.activity.Activity
import de.chrisander.timeoflife.domain.model.activity.ActivityId
import de.chrisander.timeoflife.domain.model.activity.ActivitySchedule
import de.chrisander.timeoflife.domain.model.activity.Recurrence
import de.chrisander.timeoflife.domain.model.lifetime.DAYS_PER_MONTH
import de.chrisander.timeoflife.domain.model.profile.UserProfile
import de.chrisander.timeoflife.ui.customactivityeditor.model.DurationStep
import de.chrisander.timeoflife.ui.customactivityeditor.model.IntervalUnit
import de.chrisander.timeoflife.ui.navigation.CustomActivityEditorRoute
import de.chrisander.timeoflife.ui.navigation.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class CustomActivityEditorViewModel(
    route: CustomActivityEditorRoute,
    private val userProfileRepository: UserProfileRepository,
    private val navigator: Navigator,
) : ViewModel() {

    private val activityId = route.activityId?.let(::ActivityId)

    private val _uiState = MutableStateFlow(
        if (activityId == null) validate(CustomActivityEditorUiState()) else null,
    )

    val uiState: StateFlow<CustomActivityEditorUiState?> = _uiState.asStateFlow()

    init {
        if (activityId != null) {
            viewModelScope.launch {
                val activity = storedProfile().activities
                    .filterIsInstance<Activity.CustomActivity>()
                    .firstOrNull { it.id == activityId }
                if (activity == null) navigator.goBack() else _uiState.value =
                    validate(stateOf(activity))
            }
        }
    }

    fun onEvent(event: CustomActivityEditorEvent) {
        when (event) {
            is CustomActivityEditorEvent.NameChanged -> updateState { it.copy(name = event.name) }
            is CustomActivityEditorEvent.DurationSelected -> updateState {
                it.copy(durationMinutes = event.minutes)
            }

            is CustomActivityEditorEvent.EveryChanged -> updateState {
                it.copy(every = digits(event.every, MAX_NUMBER_DIGITS))
            }

            is CustomActivityEditorEvent.UnitSelected -> updateState { it.copy(unit = event.unit) }
            is CustomActivityEditorEvent.FromAgeChanged -> updateState {
                it.copy(fromAge = digits(event.fromAge, MAX_NUMBER_DIGITS))
            }

            is CustomActivityEditorEvent.UntilAgeChanged -> updateState {
                it.copy(untilAge = digits(event.untilAge, MAX_NUMBER_DIGITS))
            }

            CustomActivityEditorEvent.Save -> onSave()
            CustomActivityEditorEvent.Delete -> updateState { it.copy(showDeleteConfirmation = true) }
            CustomActivityEditorEvent.DismissDelete -> updateState { it.copy(showDeleteConfirmation = false) }
            CustomActivityEditorEvent.ConfirmDelete -> onConfirmDelete()
            CustomActivityEditorEvent.Back -> navigator.goBack()
            CustomActivityEditorEvent.DismissError -> updateState { it.copy(showSaveError = false) }
        }
    }

    private fun updateState(transform: (CustomActivityEditorUiState) -> CustomActivityEditorUiState) {
        _uiState.update { state -> state?.let { validate(transform(it)) } }
    }

    private fun onSave() {
        val state = _uiState.value ?: return
        if (!state.canSave) return
        val activity = Activity.CustomActivity(
            id = activityId ?: ActivityId("custom:${Uuid.random()}"),
            name = state.name.trim(),
            schedule = checkNotNull(scheduleOf(state)),
        )
        updateProfile { profile ->
            val activities = if (profile.activities.any { it.id == activity.id }) {
                profile.activities.map { if (it.id == activity.id) activity else it }
            } else {
                profile.activities + activity
            }
            profile.copy(activities = activities)
        }
    }

    private fun onConfirmDelete() {
        updateState { it.copy(showDeleteConfirmation = false) }
        if (activityId == null) return
        updateProfile { profile -> profile.copy(activities = profile.activities.filterNot { it.id == activityId }) }
    }

    private fun updateProfile(transform: (UserProfile) -> UserProfile) {
        updateState { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                userProfileRepository.saveUserProfile(transform(storedProfile()))
                updateState { it.copy(isSaving = false) }
                navigator.goBack()
            } catch (_: IOException) {
                updateState { it.copy(isSaving = false, showSaveError = true) }
            }
        }
    }

    private suspend fun storedProfile(): UserProfile =
        userProfileRepository.userProfile.filterNotNull().first()

    private fun validate(state: CustomActivityEditorUiState): CustomActivityEditorUiState {
        val every = everyOf(state)
        val maxMinutes = intervalMinutes(state.unit, every ?: 1)
        val duration = state.durationMinutes.coerceIn(MIN_DURATION_MINUTES, maxMinutes)
        val clamped = state.copy(
            durationMinutes = duration,
            durationOptions = (durationOptions(maxMinutes) + duration).distinct().sorted(),
        )
        return clamped.copy(
            recurrence = recurrenceOf(clamped),
            everyError = every == null,
            ageError = ageRangeOf(state) == null,
            canSave = state.name.isNotBlank() && scheduleOf(clamped) != null && !state.isSaving,
        )
    }
}

private fun stateOf(activity: Activity.CustomActivity): CustomActivityEditorUiState {
    val schedule = activity.schedule
    val (unit, every) = when (val interval = schedule.recurrence.interval) {
        is DateTimeUnit.DayBased -> if (interval.days % DAYS_PER_WEEK == 0) {
            IntervalUnit.WEEK to interval.days / DAYS_PER_WEEK
        } else {
            IntervalUnit.DAY to interval.days
        }

        is DateTimeUnit.MonthBased -> if (interval.months % MONTHS_PER_YEAR == 0) {
            IntervalUnit.YEAR to interval.months / MONTHS_PER_YEAR
        } else {
            IntervalUnit.MONTH to interval.months
        }
    }
    return CustomActivityEditorUiState(
        isNew = false,
        name = activity.name,
        durationMinutes = schedule.recurrence.duration.inWholeMinutes.toInt(),
        every = every.toString(),
        unit = unit,
        fromAge = schedule.fromAge?.toString().orEmpty(),
        untilAge = schedule.untilAge?.toString().orEmpty(),
    )
}

private fun recurrenceOf(state: CustomActivityEditorUiState): Recurrence? {
    val every = everyOf(state)
    if (every == null || state.durationMinutes > intervalMinutes(state.unit, every)) return null
    val interval = when (state.unit) {
        IntervalUnit.DAY -> DateTimeUnit.DAY * every
        IntervalUnit.WEEK -> DateTimeUnit.DAY * (every * DAYS_PER_WEEK)
        IntervalUnit.MONTH -> DateTimeUnit.MONTH * every
        IntervalUnit.YEAR -> DateTimeUnit.MONTH * (every * MONTHS_PER_YEAR)
    }
    return Recurrence(state.durationMinutes.minutes, interval)
}

private fun scheduleOf(state: CustomActivityEditorUiState): ActivitySchedule? {
    val recurrence = recurrenceOf(state)
    val ageRange = ageRangeOf(state)
    return if (recurrence != null && ageRange != null) {
        ActivitySchedule(recurrence, ageRange.first, ageRange.second)
    } else {
        null
    }
}

private fun durationOptions(maxMinutes: Int): List<Int> = buildList {
    var value = MIN_DURATION_MINUTES.minutes
    while (value <= maxMinutes.minutes) {
        add(value.inWholeMinutes.toInt())
        value += DurationStep.entries.first { value < it.below }.step
    }
}

private fun everyOf(state: CustomActivityEditorUiState): Int? =
    state.every.toIntOrNull()?.takeIf { it >= 1 }

private fun ageRangeOf(state: CustomActivityEditorUiState): Pair<Int?, Int?>? {
    val fromAge = state.fromAge.toIntOrNull()
    val untilAge = state.untilAge.toIntOrNull()
    val isEmpty = untilAge == 0 || fromAge != null && untilAge != null && fromAge >= untilAge
    return if (isEmpty) null else fromAge to untilAge
}

private fun intervalMinutes(unit: IntervalUnit, every: Int): Int {
    val days = when (unit) {
        IntervalUnit.DAY -> every.toDouble()
        IntervalUnit.WEEK -> every * DAYS_PER_WEEK.toDouble()
        IntervalUnit.MONTH -> every * DAYS_PER_MONTH
        IntervalUnit.YEAR -> every * MONTHS_PER_YEAR * DAYS_PER_MONTH
    }
    return days.days.inWholeMinutes.toInt()
}

private fun digits(input: String, maxLength: Int): String =
    input.filter(Char::isDigit).take(maxLength)

private const val MIN_DURATION_MINUTES = 5
private const val MAX_NUMBER_DIGITS = 3
private const val DAYS_PER_WEEK = 7
private const val MONTHS_PER_YEAR = 12
