package de.chrisander.timeoflife.ui.activity

import de.chrisander.timeoflife.domain.model.LifeSegment
import de.chrisander.timeoflife.domain.model.activity.Activity
import de.chrisander.timeoflife.domain.model.activity.ActivityId
import de.chrisander.timeoflife.domain.model.activity.DefaultActivityType
import de.chrisander.timeoflife.domain.model.lifespan.Lifespan
import de.chrisander.timeoflife.domain.usecase.LifeTimeCalculator
import de.chrisander.timeoflife.ui.activity.mapper.activityDetail
import kotlinx.datetime.LocalDate
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ActivityDetailMapperTest {

    private val sleep = Activity.DefaultActivity(DefaultActivityType.SLEEP)
    private val work = Activity.DefaultActivity(DefaultActivityType.WORK)

    // Born 2000, life span 80 years, today at age 40: half lived, half ahead.
    private val breakdown = LifeTimeCalculator().calculate(
        birthDate = LocalDate(2000, 1, 1),
        lifespan = Lifespan.Manual(80.0),
        activities = listOf(sleep, work),
        today = LocalDate(2040, 1, 1),
    )

    @Test
    fun sleepTakesAThirdOfEveryPartOfLife() {
        val detail = breakdown.activityDetail(sleep.id)!!

        assertClose(1 / 3.0, detail.shareOfLived)
        assertClose(1 / 3.0, detail.shareOfRemaining)
        assertClose(1 / 3.0, detail.shareOfLifespan)
        assertClose(detail.yearsSpent + detail.yearsAhead, detail.yearsTotal)
        val calculation = assertIs<ActivityCalculation.Scheduled>(detail.calculation)
        assertClose(1 / 3.0, calculation.shareOfTime)
        assertClose(40.0, calculation.age, tolerance = 0.01)
    }

    @Test
    fun workOnlyCountsWithinItsAgeRange() {
        val detail = breakdown.activityDetail(work.id)!!
        val share = (detail.calculation as ActivityCalculation.Scheduled).shareOfTime

        // Work runs from 18 to 67: 22 years so far, 27 ahead.
        assertClose(22.0 * share, detail.yearsSpent, tolerance = 0.01)
        assertClose(27.0 * share, detail.yearsAhead, tolerance = 0.01)
    }

    @Test
    fun freeTimeIsWhatTheActivitiesLeave() {
        val detail = breakdown.activityDetail(null)!!

        assertIs<LifeSegment.FreeTime>(detail.segment)
        val calculation = assertIs<ActivityCalculation.FreeTime>(detail.calculation)
        assertIs<LifeSegment.FreeTime>(calculation.daySoFar.first())
        assertClose(1.0, calculation.daySoFar.sumOf { it.fraction })
        assertClose(1.0, calculation.dayAhead.sumOf { it.fraction })
        val activitiesSoFar = breakdown.activities.sumOf { it.spent.inWholeHours } / 24.0 / 365.2425
        assertClose(40.0 - activitiesSoFar, detail.yearsSpent, tolerance = 0.01)
        assertTrue(detail.yearsSpent > 0)
    }

    @Test
    fun unknownActivityHasNoDetail() {
        assertNull(breakdown.activityDetail(ActivityId("gone")))
    }

    private fun assertClose(expected: Double, actual: Double, tolerance: Double = 1e-6) =
        assertTrue(abs(expected - actual) < tolerance, "expected $expected but was $actual")
}
