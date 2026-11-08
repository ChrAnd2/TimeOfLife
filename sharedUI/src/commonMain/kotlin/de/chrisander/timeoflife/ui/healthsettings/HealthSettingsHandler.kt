package de.chrisander.timeoflife.ui.healthsettings

import de.chrisander.timeoflife.domain.model.lifespan.factor.BodyMeasurements
import de.chrisander.timeoflife.domain.model.lifespan.factor.HealthConditions
import de.chrisander.timeoflife.domain.model.lifespan.factor.SmokingStatus
import de.chrisander.timeoflife.domain.model.profile.LifespanFactors
import de.chrisander.timeoflife.ui.healthsettings.model.SmokingChoice
import kotlinx.datetime.LocalDate
import kotlinx.datetime.yearsUntil

/** Event handling, validation and mapping of [HealthSettings], shared by onboarding and profile editing. */
class HealthSettingsHandler {

    fun handleEvent(
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

    /** Fills in the BMI and the error flags. */
    fun validate(
        health: HealthSettingsUiState,
        birthDate: LocalDate?,
        today: LocalDate,
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
            quitAgeError = health.quitAge.isNotEmpty() && isQuitAgeMissing(
                health,
                birthDate,
                today
            ),
        )
    }

    /** @param health already passed through [validate]. */
    fun isValid(health: HealthSettingsUiState, birthDate: LocalDate?, today: LocalDate): Boolean =
        !health.heightError && !health.weightError && !isQuitAgeMissing(health, birthDate, today)

    /** Only call this when [isValid] is `true`. */
    fun toLifespanFactors(
        health: HealthSettingsUiState,
        birthDate: LocalDate,
        today: LocalDate,
    ): LifespanFactors = LifespanFactors(
        countryCode = health.countryCode,
        body = bodyMeasurements(parseHeight(health.height), parseWeight(health.weight)),
        smoking = when (health.smoking) {
            SmokingChoice.NEVER -> SmokingStatus.Never
            SmokingChoice.FORMER -> SmokingStatus.Former(
                quitAge = checkNotNull(parseQuitAge(health.quitAge, birthDate, today)),
            )

            SmokingChoice.CURRENT -> SmokingStatus.Current
        },
        alcohol = health.alcohol,
        physicalActivity = health.physicalActivity,
        conditions = HealthConditions(health.conditions),
    )

    fun fromLifespanFactors(factors: LifespanFactors): HealthSettingsUiState {
        val smoking = factors.smoking
        return HealthSettingsUiState(
            countryCode = factors.countryCode,
            height = factors.body?.heightCm?.let(::formatDecimal).orEmpty(),
            weight = factors.body?.weightKg?.let(::formatDecimal).orEmpty(),
            smoking = when (smoking) {
                SmokingStatus.Never -> SmokingChoice.NEVER
                is SmokingStatus.Former -> SmokingChoice.FORMER
                SmokingStatus.Current -> SmokingChoice.CURRENT
            },
            quitAge = (smoking as? SmokingStatus.Former)?.quitAge?.toString().orEmpty(),
            alcohol = factors.alcohol,
            physicalActivity = factors.physicalActivity,
            conditions = factors.conditions.conditions,
        )
    }

    private fun isQuitAgeMissing(
        health: HealthSettingsUiState,
        birthDate: LocalDate?,
        today: LocalDate
    ): Boolean =
        health.smoking == SmokingChoice.FORMER && parseQuitAge(
            health.quitAge,
            birthDate,
            today
        ) == null

    private fun parseQuitAge(quitAge: String, birthDate: LocalDate?, today: LocalDate): Int? {
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

/** The inverse of [sanitizeDecimal] and [parseDecimal]: `182.0` becomes `"182"`, `72.5` stays `"72.5"`. */
private fun formatDecimal(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()

private val HEIGHT_RANGE_CM = 50.0..250.0
private val WEIGHT_RANGE_KG = 20.0..400.0
