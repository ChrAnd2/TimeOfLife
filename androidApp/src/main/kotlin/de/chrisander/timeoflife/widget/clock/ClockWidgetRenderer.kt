// Drawing proportions relative to the widget size.
@file:Suppress("MagicNumber")

package de.chrisander.timeoflife.widget.clock

import android.graphics.Bitmap
import android.graphics.RectF
import android.graphics.Typeface
import de.chrisander.timeoflife.domain.model.LifeProgress
import de.chrisander.timeoflife.widget.MIN_LEGEND_ROW_DP
import de.chrisander.timeoflife.widget.drawPanel
import de.chrisander.timeoflife.widget.legend.LegendEntry
import de.chrisander.timeoflife.widget.legend.LegendPosition
import de.chrisander.timeoflife.widget.legend.drawLegend
import de.chrisander.timeoflife.widget.renderWidgetBitmap
import kotlin.math.min

/**
 * The whole widget: the clock, plus the [legend] where [legendPosition] puts it.
 *
 * @param progress `null` draws an empty face without hands, e.g. before onboarding.
 * @param density display density, so the legend stays legible at any widget size.
 */
internal fun renderClockWidget(
    progress: LifeProgress?,
    legend: List<LegendEntry>,
    legendPosition: LegendPosition,
    widthPx: Int,
    heightPx: Int,
    density: Float,
    typeface: Typeface,
): Bitmap = renderWidgetBitmap(widthPx, heightPx) { width, height, scale ->
    val minRowHeight = MIN_LEGEND_ROW_DP * density * scale
    val padding = min(width, height) * 0.06f

    val position =
        if (legend.isEmpty()) LegendPosition.HIDDEN else legendPosition.resolve(width, height)
    when (position) {
        LegendPosition.SIDE -> {
            val clockSize = min(height.toFloat(), width * MAX_CLOCK_SHARE_WITH_LEGEND)
            drawPanel(width, height)
            drawClock(
                progress,
                clockSize / 2,
                height / 2f,
                clockSize / 2 - padding
            )
            drawLegend(
                legend,
                RectF(clockSize, padding, width - padding * 1.5f, height - padding),
                minRowHeight,
                typeface,
            )
        }

        LegendPosition.BOTTOM -> {
            val clockSize = min(width.toFloat(), height * MAX_CLOCK_SHARE_WITH_LEGEND)
            drawPanel(width, height)
            drawClock(
                progress,
                width / 2f,
                clockSize / 2,
                clockSize / 2 - padding
            )
            drawLegend(
                legend,
                RectF(padding * 1.5f, clockSize, width - padding * 1.5f, height - padding),
                minRowHeight,
                typeface,
            )
        }

        else -> drawClock(
            progress,
            width / 2f,
            height / 2f,
            min(width, height) / 2f * 0.97f
        )
    }
}

/** Beside a wide widget, below a tall one, hidden on a roughly square one. */
private fun LegendPosition.resolve(width: Int, height: Int): LegendPosition = when {
    this != LegendPosition.AUTOMATIC -> this
    width >= height * LEGEND_ASPECT_RATIO -> LegendPosition.SIDE
    height >= width * LEGEND_ASPECT_RATIO -> LegendPosition.BOTTOM
    else -> LegendPosition.HIDDEN
}

/**
 * With [LegendPosition.AUTOMATIC]: how much wider than tall (or taller than wide) the widget must be for the legend.
 */
private const val LEGEND_ASPECT_RATIO = 1.5f

/** Share of the widget's width (side legend) or height (bottom legend) the clock may take at most. */
private const val MAX_CLOCK_SHARE_WITH_LEGEND = 0.6f
