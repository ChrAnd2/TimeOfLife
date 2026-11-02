package de.chrisander.timeoflife.domain.model.lifespan.factor

/**
 * Alcohol intake in grams of pure alcohol per week (one standard drink is about 10 g in the EU, 14 g in the US).
 *
 * Effect: Wood et al. 2018, at age 40, compared with < 100 g/week: ~0.5 / 1–2 / 4–5 years lost.
 */
enum class AlcoholConsumption(override val yearsEffect: Double) : HealthFactor {
    NONE(0.0),

    /** Below 100 g/week. */
    LOW(0.0),

    /** 100–200 g/week. */
    MODERATE(-0.5),

    /** 200–350 g/week. */
    HIGH(-1.5),

    /** More than 350 g/week. */
    VERY_HIGH(-4.5),
}
