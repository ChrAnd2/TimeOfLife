package de.chrisander.timeoflife.widget

import de.chrisander.timeoflife.domain.model.activity.Activity.DefaultActivity
import de.chrisander.timeoflife.domain.model.activity.DefaultActivityType
import de.chrisander.timeoflife.domain.model.lifespan.Lifespan
import de.chrisander.timeoflife.domain.model.lifetime.ActivityTime
import de.chrisander.timeoflife.domain.model.lifetime.LifeTimeBreakdown
import de.chrisander.timeoflife.widget.domain.model.LifeSegment
import de.chrisander.timeoflife.widget.mapper.toLifeProgress
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

class LifeProgressTest {

    private val sleep = DefaultActivity(DefaultActivityType.SLEEP)
    private val work = DefaultActivity(DefaultActivityType.WORK)

    private fun breakdown(
        lived: Duration,
        remaining: Duration,
        vararg spent: Pair<DefaultActivity, Duration>
    ) =
        LifeTimeBreakdown(
            lifespan = Lifespan.Manual(years = 80.0),
            expectedDeathDate = LocalDate(2070, 1, 1),
            lived = lived,
            remaining = remaining,
            beyondLifespan = Duration.ZERO,
            activities = spent.map { (activity, time) ->
                ActivityTime(
                    activity,
                    time,
                    Duration.ZERO
                )
            },
            freeTimeRemaining = remaining,
        )

    // Shares are powers of two (1/2, 1/4, 1/8), which doubles represent exactly, so results compare with plain equality.
    @Test
    fun livedTimeIsSplitIntoActivitiesAndFreeTime() {
        val progress =
            breakdown(lived = 512.days, remaining = 512.days, sleep to 128.days, work to 256.days)
                .toLifeProgress()

        assertEquals(0.5, progress.progress)
        assertEquals(
            listOf(
                LifeSegment.ActivitySegment(sleep, 0.125),
                LifeSegment.ActivitySegment(work, 0.25),
                LifeSegment.FreeTime(0.125),
            ),
            progress.segments,
        )
    }

    @Test
    fun activitiesNotStartedYetAreLeftOut() {
        val progress = breakdown(
            lived = 500.days,
            remaining = 500.days,
            sleep to 500.days,
            work to Duration.ZERO
        )
            .toLifeProgress()

        assertEquals(listOf(LifeSegment.ActivitySegment(sleep, 0.5)), progress.segments)
    }

    @Test
    fun overlappingActivitiesAreScaledToTheLivedShare() {
        val progress =
            breakdown(lived = 100.days, remaining = 100.days, sleep to 100.days, work to 100.days)
                .toLifeProgress()

        assertEquals(listOf(0.25, 0.25), progress.segments.map { it.fraction })
    }

    @Test
    fun clockTextIsTheAgeOnA24HourClock() {
        assertEquals("00:00", breakdown(Duration.ZERO, 1000.days).toLifeProgress().clockText)
        assertEquals("12:00", breakdown(500.days, 500.days).toLifeProgress().clockText)
        assertEquals("18:36", breakdown(775.days, 225.days).toLifeProgress().clockText)
    }

    @Test
    fun outlivedLifespanShowsAFullClock() {
        val progress = breakdown(lived = 1000.days, remaining = Duration.ZERO).toLifeProgress()

        assertEquals(1.0, progress.progress)
        assertEquals("24:00", progress.clockText)
    }
}
