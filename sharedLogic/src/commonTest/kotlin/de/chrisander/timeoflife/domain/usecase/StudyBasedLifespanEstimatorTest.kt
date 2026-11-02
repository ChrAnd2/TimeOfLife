package de.chrisander.timeoflife.domain.usecase

import de.chrisander.timeoflife.domain.model.lifespan.Lifespan
import de.chrisander.timeoflife.domain.model.lifespan.factor.AlcoholConsumption
import de.chrisander.timeoflife.domain.model.lifespan.factor.BodyMeasurements
import de.chrisander.timeoflife.domain.model.lifespan.factor.HealthCondition
import de.chrisander.timeoflife.domain.model.lifespan.factor.HealthConditions
import de.chrisander.timeoflife.domain.model.lifespan.factor.PhysicalActivityLevel
import de.chrisander.timeoflife.domain.model.lifespan.factor.SmokingStatus
import de.chrisander.timeoflife.domain.model.profile.LifespanFactors
import de.chrisander.timeoflife.domain.model.profile.LifespanSetting
import de.chrisander.timeoflife.domain.model.profile.Sex
import de.chrisander.timeoflife.domain.model.profile.UserProfile
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StudyBasedLifespanEstimatorTest {

    private val estimator = StudyBasedLifespanEstimator(
        baseline = { _, sex -> if (sex == Sex.MALE) 80.0 else 84.0 },
    )

    @Test
    fun manualLifespanOverridesEstimate() {
        val profile = UserProfile(
            "Chris",
            Sex.MALE,
            LocalDate(1990, 1, 1),
            lifespanSetting = LifespanSetting.Manual(95.0)
        )

        assertEquals(
            Lifespan.Manual(years = 95.0),
            estimator.lifespanOf(profile, LocalDate(2030, 1, 1))
        )
    }

    @Test
    fun estimatedLifespanCarriesEstimate() {
        val profile = UserProfile("Chris", Sex.MALE, LocalDate(1990, 1, 1))

        val lifespan = estimator.lifespanOf(profile, LocalDate(2030, 1, 1))

        assertEquals(80.0, lifespan.years)
        assertEquals(80.0, (lifespan as Lifespan.Estimated).baselineYears)
    }

    @Test
    fun withoutFactorsEstimateIsBaseline() {
        val estimate = estimator.estimate(Sex.FEMALE, currentAgeYears = 30.0, LifespanFactors())

        assertEquals(84.0, estimate.years)
        assertTrue(estimate.adjustments.isEmpty())
    }

    @Test
    fun adjustmentsAreAddedToBaseline() {
        val body = BodyMeasurements(heightCm = 180.0, weightKg = 105.0) // BMI ≈ 32.4
        val conditions = HealthConditions(setOf(HealthCondition.DIABETES))
        val factors = LifespanFactors(
            smoking = SmokingStatus.Current,
            alcohol = AlcoholConsumption.HIGH,
            physicalActivity = PhysicalActivityLevel.MODERATE,
            body = body,
            conditions = conditions,
        )

        val estimate = estimator.estimate(Sex.MALE, currentAgeYears = 30.0, factors)

        assertEquals(
            listOf(
                SmokingStatus.Current,
                AlcoholConsumption.HIGH,
                PhysicalActivityLevel.MODERATE,
                body,
                conditions
            ),
            estimate.adjustments,
        )
        val effects = listOf(-10.0, -1.5, 3.4, -3.0, -6.0)
        assertEquals(effects, estimate.adjustments.map { it.yearsEffect })
        assertEquals(80.0 + effects.sum(), estimate.years)
    }

    @Test
    fun neutralFactorsAreNotListedAsAdjustments() {
        val factors = LifespanFactors(
            alcohol = AlcoholConsumption.LOW,
            smoking = SmokingStatus.Former(quitAge = 30)
        )

        assertTrue(
            estimator.estimate(
                Sex.MALE,
                currentAgeYears = 50.0,
                factors
            ).adjustments.isEmpty()
        )
    }

    @Test
    fun quittingEarlyRegainsMoreYears() {
        val early = estimator.estimate(
            Sex.MALE, 50.0,
            LifespanFactors(smoking = SmokingStatus.Former(quitAge = 30))
        )
        val late = estimator.estimate(
            Sex.MALE, 70.0,
            LifespanFactors(smoking = SmokingStatus.Former(quitAge = 60))
        )

        assertEquals(80.0, early.years)
        assertEquals(73.0, late.years)
    }

    @Test
    fun estimateIsNotClampedToCurrentAge() {
        val factors = LifespanFactors(smoking = SmokingStatus.Current)

        val estimate = estimator.estimate(Sex.MALE, currentAgeYears = 85.0, factors)

        assertEquals(70.0, estimate.years)
    }
}
