package de.chrisander.timeoflife.domain.usecase

import de.chrisander.timeoflife.domain.model.activity.Activity.DefaultActivity
import de.chrisander.timeoflife.domain.model.activity.DefaultActivityType
import de.chrisander.timeoflife.domain.model.lifespan.Lifespan
import de.chrisander.timeoflife.domain.model.profile.Sex
import de.chrisander.timeoflife.domain.model.profile.UserProfile
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class GetLifeTimeBreakdownUseCaseTest {

    private val estimate =
        Lifespan.Estimated(years = 80.0, baselineYears = 80.0, adjustments = emptyList())
    private val getBreakdown = GetLifeTimeBreakdownUseCase(
        estimator = { _, _, _ -> estimate },
        calculator = LifeTimeCalculator(),
    )

    @Test
    fun usesProfileActivitiesAndCarriesEstimate() {
        val profile = UserProfile("Chris", Sex.MALE, LocalDate(1990, 1, 1))

        val breakdown = getBreakdown(profile, today = LocalDate(2030, 1, 1))

        assertEquals(
            DefaultActivityType.entries.toList(),
            breakdown.activities.map { (it.activity as DefaultActivity).type })
        assertEquals(estimate, breakdown.lifespan)
    }
}
