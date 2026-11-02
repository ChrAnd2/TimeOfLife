package de.chrisander.timeoflife.domain.model.lifespan.factor

/** Height and weight, which together give the body mass index. */
data class BodyMeasurements(
    val heightCm: Double,
    val weightKg: Double,
) : HealthFactor {
    init {
        require(heightCm > 0) { "Height must be positive, was $heightCm" }
        require(weightKg > 0) { "Weight must be positive, was $weightKg" }
    }

    /** Body mass index in kg/m². */
    val bmi: Double
        get() {
            val heightM = heightCm / 100
            return weightKg / (heightM * heightM)
        }

    /**
     * Prospective Studies Collaboration 2009: BMI 30–35 costs 2–4 years, BMI 40–45 costs 8–10 years.
     * The values for < 18.5, 25–30 and 35–40 are not given in years by the study and are interpolated placeholders.
     */
    override val yearsEffect: Double
        get() = when {
            bmi < 18.5 -> -1.0
            bmi < 25 -> 0.0
            bmi < 30 -> -1.0
            bmi < 35 -> -3.0
            bmi < 40 -> -6.0
            else -> -9.0
        }
}
