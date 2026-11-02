package de.chrisander.timeoflife.domain.usecase

import de.chrisander.timeoflife.data.datasource.BaselineLifeExpectancyProvider
import de.chrisander.timeoflife.data.datasource.StaticBaselineLifeExpectancyProvider
import de.chrisander.timeoflife.domain.model.lifespan.Lifespan
import de.chrisander.timeoflife.domain.model.profile.LifespanFactors
import de.chrisander.timeoflife.domain.model.profile.Sex

class StudyBasedLifespanEstimator(
    private val baseline: BaselineLifeExpectancyProvider = StaticBaselineLifeExpectancyProvider,
) : LifespanEstimator {

    override fun estimate(
        sex: Sex,
        currentAgeYears: Double,
        factors: LifespanFactors
    ): Lifespan.Estimated {
        val baselineYears = baseline.atBirth(factors.countryCode, sex)
        val adjustments = factors.healthFactors.filter { it.yearsEffect != 0.0 }
        return Lifespan.Estimated(
            // TODO: The baseline is life expectancy at birth. Use remaining life expectancy at currentAgeYears
            //  from a period life table instead.
            years = baselineYears + adjustments.sumOf { it.yearsEffect },
            baselineYears = baselineYears,
            adjustments = adjustments,
        )
    }
}