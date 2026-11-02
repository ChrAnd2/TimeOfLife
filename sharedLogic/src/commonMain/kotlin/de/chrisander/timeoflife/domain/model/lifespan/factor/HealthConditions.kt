package de.chrisander.timeoflife.domain.model.lifespan.factor

/** Serious diagnosed conditions the user has. They count together, since their effects overlap. */
data class HealthConditions(
    val conditions: Set<HealthCondition> = emptySet(),
) : HealthFactor {

    /**
     * Emerging Risk Factors Collaboration 2015, at age 60: any 2 of diabetes, heart attack and stroke cost ~12 years,
     * all 3 ~15 years. Each condition carried a similar risk, so a single one is approximated as half of two.
     */
    override val yearsEffect: Double
        get() = when (conditions.size) {
            0 -> 0.0
            1 -> -6.0
            2 -> -12.0
            else -> -15.0
        }
}

enum class HealthCondition {
    DIABETES,
    MYOCARDIAL_INFARCTION,
    STROKE,
}
