package de.chrisander.timeoflife.widget.bar

import de.chrisander.timeoflife.widget.legend.LegendPosition
import kotlin.test.Test
import kotlin.test.assertEquals

class BarLegendPositionTest {

    @Test
    fun automaticHidesTheLegendOnASingleRowWidget() {
        assertEquals(LegendPosition.HIDDEN, LegendPosition.AUTOMATIC.resolveForBar(heightDp = 80f))
    }

    @Test
    fun automaticPutsTheLegendBelowATallerWidget() {
        assertEquals(LegendPosition.BOTTOM, LegendPosition.AUTOMATIC.resolveForBar(heightDp = 180f))
    }

    @Test
    fun explicitPositionIsKept() {
        LegendPosition.entries.filter { it != LegendPosition.AUTOMATIC }.forEach {
            assertEquals(it, it.resolveForBar(heightDp = 80f))
        }
    }
}
