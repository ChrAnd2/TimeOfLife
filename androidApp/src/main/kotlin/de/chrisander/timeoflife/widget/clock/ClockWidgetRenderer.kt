// Drawing proportions relative to the widget size.
@file:Suppress("MagicNumber")

package de.chrisander.timeoflife.widget.clock

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import de.chrisander.timeoflife.widget.MIN_LEGEND_ROW_DP
import de.chrisander.timeoflife.widget.WidgetColors
import de.chrisander.timeoflife.widget.domain.model.LifeProgress
import de.chrisander.timeoflife.widget.drawPanel
import de.chrisander.timeoflife.widget.legend.LegendEntry
import de.chrisander.timeoflife.widget.legend.LegendPosition
import de.chrisander.timeoflife.widget.legend.drawLegend
import de.chrisander.timeoflife.widget.renderWidgetBitmap
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

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

private fun Canvas.drawClock(
    progress: LifeProgress?,
    cx: Float,
    cy: Float,
    r: Float
) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    drawFace(cx, cy, r, paint)
    if (progress != null) drawPie(progress, cx, cy, r * 0.70f, paint)
    drawTicks(cx, cy, r, paint)
    if (progress != null) drawHand(progress.progress, cx, cy, r, paint)
}

private fun Canvas.drawFace(cx: Float, cy: Float, r: Float, paint: Paint) {
    paint.style = Paint.Style.FILL
    paint.color = WidgetColors.FACE
    drawCircle(cx, cy, r, paint)
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = r * 0.025f
    paint.color = WidgetColors.RIM
    drawCircle(cx, cy, r - paint.strokeWidth / 2, paint)
}

private fun Canvas.drawPie(
    progress: LifeProgress,
    cx: Float,
    cy: Float,
    pieR: Float,
    paint: Paint
) {
    val oval = RectF(cx - pieR, cy - pieR, cx + pieR, cy + pieR)
    paint.style = Paint.Style.FILL
    paint.color = WidgetColors.FUTURE
    drawCircle(cx, cy, pieR, paint)

    var start = -90f
    progress.segments.forEach { segment ->
        val sweep = 360f * segment.fraction.toFloat()
        paint.color = WidgetColors.of(segment)
        drawArc(oval, start, sweep, true, paint)
        start += sweep
    }
}

private fun Canvas.drawTicks(cx: Float, cy: Float, r: Float, paint: Paint) {
    paint.style = Paint.Style.STROKE
    paint.strokeCap = Paint.Cap.ROUND
    for (hour in 0 until 24) {
        val major = hour % 3 == 0
        val angle = hourAngle(hour)
        val r1 = r * if (major) 0.85f else 0.89f
        val r2 = r * 0.94f
        paint.color = if (major) WidgetColors.TEXT else WidgetColors.TEXT_DIM
        paint.strokeWidth = r * if (major) 0.022f else 0.012f
        drawLine(
            cx + r1 * cos(angle),
            cy + r1 * sin(angle),
            cx + r2 * cos(angle),
            cy + r2 * sin(angle),
            paint
        )
    }
}

private fun Canvas.drawHand(progress: Double, cx: Float, cy: Float, r: Float, paint: Paint) {
    val angle = fractionAngle(progress)

    paint.style = Paint.Style.STROKE
    paint.strokeCap = Paint.Cap.ROUND
    paint.setShadowLayer(r * 0.03f, 0f, 0f, Color.argb(0xAA, 0, 0, 0))
    paint.color = WidgetColors.HAND
    paint.strokeWidth = r * 0.06f
    drawLine(cx, cy, cx + r * 0.62f * cos(angle), cy + r * 0.62f * sin(angle), paint)
    paint.clearShadowLayer()

    paint.style = Paint.Style.FILL
    paint.color = WidgetColors.HAND
    drawCircle(cx, cy, r * 0.055f, paint)
    paint.color = WidgetColors.FACE
    drawCircle(cx, cy, r * 0.022f, paint)
}

private fun hourAngle(hour: Int): Float = fractionAngle(hour / 24.0)

/** Angle in radians for a fraction of a full turn, starting at the top and going clockwise. */
private fun fractionAngle(fraction: Double): Float =
    Math.toRadians(-90.0 + 360.0 * fraction).toFloat()

/**
 * With [LegendPosition.AUTOMATIC]: how much wider than tall (or taller than wide) the widget must be for the legend.
 */
private const val LEGEND_ASPECT_RATIO = 1.5f

/** Share of the widget's width (side legend) or height (bottom legend) the clock may take at most. */
private const val MAX_CLOCK_SHARE_WITH_LEGEND = 0.6f
