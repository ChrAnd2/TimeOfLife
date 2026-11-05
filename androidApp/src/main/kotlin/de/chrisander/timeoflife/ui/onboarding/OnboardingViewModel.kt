package de.chrisander.timeoflife.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.chrisander.timeoflife.data.repository.UserProfileRepository
import de.chrisander.timeoflife.domain.model.lifespan.factor.BodyMeasurements
import de.chrisander.timeoflife.domain.model.lifespan.factor.HealthConditions
import de.chrisander.timeoflife.domain.model.lifespan.factor.SmokingStatus
import de.chrisander.timeoflife.domain.model.profile.LifespanFactors
import de.chrisander.timeoflife.domain.model.profile.UserProfile
import de.chrisander.timeoflife.ui.healthsettings.HealthSettingsEvent
import de.chrisander.timeoflife.ui.healthsettings.HealthSettingsUiState
import de.chrisander.timeoflife.ui.healthsettings.model.SmokingChoice
import de.chrisander.timeoflife.ui.navigation.HomeRoute
import de.chrisander.timeoflife.ui.navigation.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.yearsUntil
import java.io.IOException
import java.util.Locale
import kotlin.time.Clock

class OnboardingViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val navigator: Navigator,
    clock: Clock,
) : ViewModel() {

    private val today = clock.todayIn(TimeZone.currentSystemDefault())

    private val _uiState = MutableStateFlow(
        validate(
            OnboardingUiState(
                health = HealthSettingsUiState(countryCode = Locale.getDefault().country.takeIf { it.length == 2 }),
            ),
        ),
    )
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.NameChanged -> updateState { it.copy(name = event.name) }
            is OnboardingEvent.SexSelected -> updateState { it.copy(sex = event.sex) }
            is OnboardingEvent.BirthDateSelected -> updateState { it.copy(birthDate = event.birthDate) }
            OnboardingEvent.Continue -> onContinue()
            OnboardingEvent.Back -> updateState { it.copy(step = OnboardingStep.PERSONAL) }
            is OnboardingEvent.Health -> updateState {
                it.copy(health = handleHealthSettingsEvent(it.health, event.event))
            }
            OnboardingEvent.Submit -> onSubmit()
            OnboardingEvent.DismissError -> updateState { it.copy(showSaveError = false) }
        }
    }

    private fun updateState(transform: (OnboardingUiState) -> OnboardingUiState) {
        _uiState.update { validate(transform(it)) }
    }

    private fun onContinue() {
        updateState { if (it.canContinue) it.copy(step = OnboardingStep.HEALTH) else it }
    }

    private fun onSubmit() {
        val state = _uiState.value
        if (!state.canSubmit) return
        val health = state.health
        val profile = UserProfile(
            name = state.name.trim(),
            sex = checkNotNull(state.sex),
            birthDate = checkNotNull(state.birthDate),
            lifespanFactors = LifespanFactors(
                countryCode = health.countryCode,
                body = bodyMeasurements(parseHeight(health.height), parseWeight(health.weight)),
                smoking = when (health.smoking) {
                    SmokingChoice.NEVER -> SmokingStatus.Never
                    SmokingChoice.FORMER -> SmokingStatus.Former(
                        quitAge = checkNotNull(parseQuitAge(health.quitAge, state.birthDate)),
                    )
                    SmokingChoice.CURRENT -> SmokingStatus.Current
                },
                alcohol = health.alcohol,
                physicalActivity = health.physicalActivity,
                conditions = HealthConditions(health.conditions),
            ),
        )

        updateState { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                userProfileRepository.saveUserProfile(profile)
                updateState { it.copy(isSaving = false) }
                navigator.replaceAll(HomeRoute)
            } catch (_: IOException) {
                updateState { it.copy(isSaving = false, showSaveError = true) }
            }
        }
    }

    private fun handleHealthSettingsEvent(
        health: HealthSettingsUiState,
        event: HealthSettingsEvent,
    ): HealthSettingsUiState = when (event) {
        is HealthSettingsEvent.CountrySelected -> health.copy(countryCode = event.countryCode)
        is HealthSettingsEvent.HeightChanged -> health.copy(height = sanitizeDecimal(event.height))
        is HealthSettingsEvent.WeightChanged -> health.copy(weight = sanitizeDecimal(event.weight))
        is HealthSettingsEvent.SmokingSelected -> health.copy(smoking = event.smoking)
        is HealthSettingsEvent.QuitAgeChanged -> health.copy(quitAge = event.quitAge.filter(Char::isDigit))
        is HealthSettingsEvent.AlcoholSelected -> health.copy(alcohol = event.alcohol)
        is HealthSettingsEvent.PhysicalActivitySelected -> health.copy(physicalActivity = event.level)
        is HealthSettingsEvent.ConditionToggled -> {
            val conditions = health.conditions
            val condition = event.condition
            health.copy(conditions = if (condition in conditions) conditions - condition else conditions + condition)
        }
    }

    private fun validate(state: OnboardingUiState): OnboardingUiState {
        val canContinue = state.name.isNotBlank() && state.sex != null && state.birthDate != null
        val health = validateHealth(state.health, state.birthDate)
        val isHealthValid = !health.heightError && !health.weightError &&
                !isQuitAgeMissing(health, state.birthDate)
        return state.copy(
            canContinue = canContinue,
            canSubmit = canContinue && isHealthValid && !state.isSaving,
            health = health,
        )
    }

    private fun validateHealth(
        health: HealthSettingsUiState,
        birthDate: LocalDate?
    ): HealthSettingsUiState {
        val height = parseHeight(health.height)
        val weight = parseWeight(health.weight)
        val heightError = health.height.isNotBlank() && height == null ||
                health.weight.isNotBlank() && health.height.isBlank()
        val weightError = health.weight.isNotBlank() && weight == null ||
                health.height.isNotBlank() && health.weight.isBlank()
        return health.copy(
            bmi = bodyMeasurements(height, weight)?.bmi,
            heightError = heightError,
            weightError = weightError,
            quitAgeError = health.quitAge.isNotEmpty() && isQuitAgeMissing(health, birthDate),
        )
    }

    private fun isQuitAgeMissing(health: HealthSettingsUiState, birthDate: LocalDate?): Boolean =
        health.smoking == SmokingChoice.FORMER && parseQuitAge(health.quitAge, birthDate) == null

    private fun parseQuitAge(quitAge: String, birthDate: LocalDate?): Int? {
        val age = birthDate?.yearsUntil(today) ?: return null
        return quitAge.toIntOrNull()?.takeIf { it in 1..age }
    }
}

private fun parseHeight(height: String): Double? =
    parseDecimal(height)?.takeIf { it in HEIGHT_RANGE_CM }

private fun parseWeight(weight: String): Double? =
    parseDecimal(weight)?.takeIf { it in WEIGHT_RANGE_KG }

private fun bodyMeasurements(height: Double?, weight: Double?): BodyMeasurements? =
    if (height != null && weight != null) BodyMeasurements(
        heightCm = height,
        weightKg = weight
    ) else null

/** Keeps digits and the first decimal separator, so a field only ever holds something like `"72.5"`. */
private fun sanitizeDecimal(input: String): String {
    val separator = input.indexOfFirst { it == '.' || it == ',' }
    return input.filterIndexed { index, char -> char.isDigit() || index == separator }
}

private fun parseDecimal(input: String): Double? = input.replace(',', '.').toDoubleOrNull()

private val HEIGHT_RANGE_CM = 50.0..250.0
private val WEIGHT_RANGE_KG = 20.0..400.0
