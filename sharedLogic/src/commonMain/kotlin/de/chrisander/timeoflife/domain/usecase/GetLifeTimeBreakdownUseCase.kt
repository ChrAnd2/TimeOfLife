package de.chrisander.timeoflife.domain.usecase

import de.chrisander.timeoflife.domain.model.lifetime.LifeTimeBreakdown
import de.chrisander.timeoflife.domain.model.profile.UserProfile
import kotlinx.datetime.LocalDate

class GetLifeTimeBreakdownUseCase(
    private val estimator: LifespanEstimator,
    private val calculator: LifeTimeCalculator,
) {
    operator fun invoke(profile: UserProfile, today: LocalDate): LifeTimeBreakdown =
        calculator.calculate(
            birthDate = profile.birthDate,
            lifespan = estimator.lifespanOf(profile, today),
            activities = profile.activities,
            today = today,
        )
}
