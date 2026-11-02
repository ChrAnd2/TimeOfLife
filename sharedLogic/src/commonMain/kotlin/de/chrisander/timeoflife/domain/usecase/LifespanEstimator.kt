package de.chrisander.timeoflife.domain.usecase

import de.chrisander.timeoflife.domain.model.lifespan.Lifespan
import de.chrisander.timeoflife.domain.model.lifetime.DAYS_PER_YEAR
import de.chrisander.timeoflife.domain.model.profile.LifespanFactors
import de.chrisander.timeoflife.domain.model.profile.LifespanSetting
import de.chrisander.timeoflife.domain.model.profile.Sex
import de.chrisander.timeoflife.domain.model.profile.UserProfile
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

fun interface LifespanEstimator {

    fun estimate(sex: Sex, currentAgeYears: Double, factors: LifespanFactors): Lifespan.Estimated

    fun lifespanOf(profile: UserProfile, today: LocalDate): Lifespan =
        when (val setting = profile.lifespanSetting) {
            is LifespanSetting.Manual -> Lifespan.Manual(years = setting.years)
            LifespanSetting.Estimated -> estimate(
                sex = profile.sex,
                currentAgeYears = profile.birthDate.daysUntil(today) / DAYS_PER_YEAR,
                factors = profile.lifespanFactors,
            )
        }
}
