package de.chrisander.timeoflife.domain.model.lifespan.factor

sealed interface HealthFactor {

    /** Years of life gained (positive) or lost (negative) compared with the baseline life expectancy. */
    val yearsEffect: Double
}
