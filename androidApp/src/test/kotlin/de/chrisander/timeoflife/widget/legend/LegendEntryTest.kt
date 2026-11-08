package de.chrisander.timeoflife.widget.legend

import de.chrisander.timeoflife.domain.model.activity.Activity.DefaultActivity
import de.chrisander.timeoflife.domain.model.activity.DefaultActivityType
import de.chrisander.timeoflife.domain.model.progress.LifeProgress
import de.chrisander.timeoflife.domain.model.progress.LifeSegment
import de.chrisander.timeoflife.widget.WidgetColors
import kotlin.test.Test
import kotlin.test.assertEquals

class LegendEntryTest {

    private val sleep =
        LifeSegment.ActivitySegment(DefaultActivity(DefaultActivityType.SLEEP), 0.125)
    private val work = LifeSegment.ActivitySegment(DefaultActivity(DefaultActivityType.WORK), 0.25)
    private val freeTime = LifeSegment.FreeTime(0.125)

    private fun label(segment: LifeSegment) = when (segment) {
        is LifeSegment.FreeTime -> "Free"
        is LifeSegment.ActivitySegment -> (segment.activity as DefaultActivity).type.name
    }

    @Test
    fun percentIsShareOfLivedTimeAndLargestComesFirst() {
        val progress = LifeProgress(
            progress = 0.5,
            segments = listOf(sleep, work, freeTime),
            clockText = "12:00"
        )

        assertEquals(
            listOf(
                LegendEntry(WidgetColors.of(work), "WORK", 50),
                LegendEntry(WidgetColors.of(sleep), "SLEEP", 25),
                LegendEntry(WidgetColors.FREE_TIME, "Free", 25),
            ),
            progress.legendEntries(::label),
        )
    }

    @Test
    fun nothingLivedYetHasNoEntries() {
        val progress = LifeProgress(progress = 0.0, segments = emptyList(), clockText = "00:00")

        assertEquals(emptyList(), progress.legendEntries(::label))
    }
}
