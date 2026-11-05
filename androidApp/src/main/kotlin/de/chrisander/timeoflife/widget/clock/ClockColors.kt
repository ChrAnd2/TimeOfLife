package de.chrisander.timeoflife.widget.clock

import de.chrisander.timeoflife.widget.WidgetColors

/** ARGB colors of the clock's parts. Activity colors are fixed and not part of it. */
internal data class ClockColors(
    val face: Int,
    val rim: Int,
    val future: Int,
    val tick: Int,
    val minorTick: Int,
    val hand: Int,
) {
    companion object {
        val Widget = ClockColors(
            face = WidgetColors.FACE,
            rim = WidgetColors.RIM,
            future = WidgetColors.FUTURE,
            tick = WidgetColors.TEXT,
            minorTick = WidgetColors.TEXT_DIM,
            hand = WidgetColors.HAND,
        )
    }
}
