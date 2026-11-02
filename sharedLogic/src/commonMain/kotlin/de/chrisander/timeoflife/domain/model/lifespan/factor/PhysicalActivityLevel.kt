package de.chrisander.timeoflife.domain.model.lifespan.factor

/**
 * Leisure time physical activity of moderate to vigorous intensity, e.g. brisk walking.
 *
 * Effect: Moore et al. 2012, compared with no leisure time activity: +1.8 / +3.4 / +4.5 years.
 */
enum class PhysicalActivityLevel(override val yearsEffect: Double) : HealthFactor {
    NONE(0.0),

    /** Up to 150 minutes per week. */
    LOW(1.8),

    /** 150–450 minutes per week, i.e. meets the WHO recommendation. */
    MODERATE(3.4),

    /** More than 450 minutes per week. */
    HIGH(4.5),
}
