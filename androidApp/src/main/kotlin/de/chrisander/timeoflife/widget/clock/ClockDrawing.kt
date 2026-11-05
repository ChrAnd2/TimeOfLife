// Drawing proportions relative to the clock radius.
@file:Suppress("MagicNumber")

package de.chrisander.timeoflife.widget.clock

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import de.chrisander.timeoflife.domain.model.LifeProgress
import de.chrisander.timeoflife.widget.WidgetColors
import kotlin.math.cos
import kotlin.math.sin

/**
 * The life clock face centered at ([cx], [cy]) with radius [r]. Shared by the clock widget and the home screen, so
 * both look the same.
 *
 * @param progress `null` draws an empty face without hands, e.g. before onboarding.
 */
internal fun Canvas.drawClock(
    progress: LifeProgress?,
    cx: Float,
    cy: Float,
    r: Float,
    colors: ClockColors = ClockColors.Widget,
) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    drawFace(cx, cy, r, colors, paint)
    if (progress != null) drawPie(progress, cx, cy, r * 0.70f, colors, paint)
    drawTicks(cx, cy, r, colors, paint)
    if (progress != null) drawHand(progress.progress, cx, cy, r, colors, paint)
}

private fun Canvas.drawFace(cx: Float, cy: Float, r: Float, colors: ClockColors, paint: Paint) {
    paint.style = Paint.Style.FILL
    paint.color = colors.face
    drawCircle(cx, cy, r, paint)
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = r * 0.025f
    paint.color = colors.rim
    drawCircle(cx, cy, r - paint.strokeWidth / 2, paint)
}

private fun Canvas.drawPie(
    progress: LifeProgress,
    cx: Float,
    cy: Float,
    pieR: Float,
    colors: ClockColors,
    paint: Paint,
) {
    val oval = RectF(cx - pieR, cy - pieR, cx + pieR, cy + pieR)
    paint.style = Paint.Style.FILL
    paint.color = colors.future
    drawCircle(cx, cy, pieR, paint)

    var start = -90f
    progress.segments.forEach { segment ->
        val sweep = 360f * segment.fraction.toFloat()
        paint.color = WidgetColors.of(segment)
        drawArc(oval, start, sweep, true, paint)
        start += sweep
    }
}

private fun Canvas.drawTicks(cx: Float, cy: Float, r: Float, colors: ClockColors, paint: Paint) {
    paint.style = Paint.Style.STROKE
    paint.strokeCap = Paint.Cap.ROUND
    for (hour in 0 until 24) {
        val major = hour % 3 == 0
        val angle = hourAngle(hour)
        val r1 = r * if (major) 0.85f else 0.89f
        val r2 = r * 0.94f
        paint.color = if (major) colors.tick else colors.minorTick
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

private fun Canvas.drawHand(
    progress: Double,
    cx: Float,
    cy: Float,
    r: Float,
    colors: ClockColors,
    paint: Paint,
) {
    val angle = fractionAngle(progress)

    paint.style = Paint.Style.STROKE
    paint.strokeCap = Paint.Cap.ROUND
    paint.setShadowLayer(r * 0.03f, 0f, 0f, Color.argb(0x66, 0, 0, 0))
    paint.color = colors.hand
    paint.strokeWidth = r * 0.06f
    drawLine(cx, cy, cx + r * 0.62f * cos(angle), cy + r * 0.62f * sin(angle), paint)
    paint.clearShadowLayer()

    paint.style = Paint.Style.FILL
    paint.color = colors.hand
    drawCircle(cx, cy, r * 0.055f, paint)
    paint.color = colors.face
    drawCircle(cx, cy, r * 0.022f, paint)
}

private fun hourAngle(hour: Int): Float = fractionAngle(hour / 24.0)

/** Angle in radians for a fraction of a full turn, starting at the top and going clockwise. */
private fun fractionAngle(fraction: Double): Float =
    Math.toRadians(-90.0 + 360.0 * fraction).toFloat()
