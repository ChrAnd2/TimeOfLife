package de.chrisander.timeoflife.domain.usecase

import de.chrisander.timeoflife.domain.model.activity.Activity.CustomActivity
import de.chrisander.timeoflife.domain.model.activity.Activity.DefaultActivity
import de.chrisander.timeoflife.domain.model.activity.ActivityId
import de.chrisander.timeoflife.domain.model.activity.ActivitySchedule
import de.chrisander.timeoflife.domain.model.activity.DefaultActivityType
import de.chrisander.timeoflife.domain.model.activity.Recurrence
import de.chrisander.timeoflife.domain.model.lifespan.Lifespan
import de.chrisander.timeoflife.domain.model.lifetime.DAYS_PER_YEAR
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlin.math.roundToLong
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class LifeTimeCalculatorTest {

    private val calculator = LifeTimeCalculator()
    private val birthDate = LocalDate(1990, 1, 1)
    private val today = LocalDate(2030, 1, 1)
    private val eighty = Lifespan.Manual(years = 80.0)
    private val sleep = DefaultActivity(
        DefaultActivityType.SLEEP,
        ActivitySchedule(Recurrence(8.hours, DateTimeUnit.DAY))
    )

    @Test
    fun splitsLifeIntoLivedAndRemaining() {
        val breakdown = calculator.calculate(birthDate, eighty, activities = emptyList(), today)

        val lifespanDays = (80 * DAYS_PER_YEAR).toLong()
        assertEquals(birthDate.daysUntil(today).days, breakdown.lived)
        assertEquals(lifespanDays.days, breakdown.total)
        assertEquals(breakdown.remaining, breakdown.freeTimeRemaining)
        assertEquals(eighty, breakdown.lifespan)
    }

    @Test
    fun dailyActivityTakesItsShareOfLivedAndRemainingTime() {
        val breakdown = calculator.calculate(birthDate, eighty, activities = listOf(sleep), today)

        val sleepTime = breakdown.activities.single()
        assertEquals(breakdown.lived / 3, sleepTime.spent)
        assertEquals(breakdown.remaining / 3, sleepTime.remaining)
        assertEquals(breakdown.remaining - sleepTime.remaining, breakdown.freeTimeRemaining)
    }

    @Test
    fun activityOnlyCountsWithinItsAgeRange() {
        // 2 h every 2 days = 1 h per day, from age 30 to 50. Today the user is 40.
        val hobby = CustomActivity(
            id = ActivityId("guitar"),
            name = "Guitar",
            schedule = ActivitySchedule(
                Recurrence(2.hours, DateTimeUnit.DAY * 2),
                fromAge = 30,
                untilAge = 50
            ),
        )

        val time = calculator.calculate(birthDate, eighty, listOf(hobby), today).activities.single()

        assertEquals(LocalDate(2020, 1, 1).daysUntil(today).days / 24, time.spent)
        assertEquals(today.daysUntil(LocalDate(2040, 1, 1)).days / 24, time.remaining)
    }

    @Test
    fun weeklyActivityTakesItsShareOfTheWeek() {
        // 14 h every week = 2 h per day on average.
        val sport = CustomActivity(
            id = ActivityId("sport"),
            name = "Sport",
            schedule = ActivitySchedule(Recurrence(14.hours, DateTimeUnit.WEEK)),
        )

        val breakdown = calculator.calculate(birthDate, eighty, listOf(sport), today)

        assertEquals(breakdown.remaining / 12, breakdown.activities.single().remaining)
    }

    @Test
    fun outlivingTheLifespanCountsTimeBeyondIt() {
        val breakdown =
            calculator.calculate(birthDate, Lifespan.Manual(years = 30.0), listOf(sleep), today)

        val lifespanDays = (30 * DAYS_PER_YEAR).roundToLong()
        assertEquals(birthDate.daysUntil(today).days, breakdown.lived)
        assertEquals(Duration.ZERO, breakdown.remaining)
        assertEquals((birthDate.daysUntil(today) - lifespanDays).days, breakdown.beyondLifespan)
        assertTrue(breakdown.hasOutlivedLifespan)
        assertEquals(breakdown.lived / 3, breakdown.activities.single().spent)
        assertEquals(Duration.ZERO, breakdown.activities.single().remaining)
    }

    @Test
    fun withinLifespanNothingIsBeyondIt() {
        val breakdown = calculator.calculate(birthDate, eighty, activities = emptyList(), today)

        assertEquals(Duration.ZERO, breakdown.beyondLifespan)
        assertFalse(breakdown.hasOutlivedLifespan)
    }
}
