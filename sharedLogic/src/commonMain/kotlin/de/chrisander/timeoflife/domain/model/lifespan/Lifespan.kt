package de.chrisander.timeoflife.domain.model.lifespan

import de.chrisander.timeoflife.domain.model.lifespan.factor.HealthFactor

sealed interface Lifespan {
    val years: Double

    data class Manual(override val years: Double) : Lifespan

    data class Estimated(
        override val years: Double,
        val baselineYears: Double,
        val adjustments: List<HealthFactor>,
    ) : Lifespan
}
