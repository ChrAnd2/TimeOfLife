// Drawing proportions relative to the widget size.
@file:Suppress("MagicNumber")

package de.chrisander.timeoflife.widget.bar

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import de.chrisander.timeoflife.domain.model.progress.LifeProgress
import de.chrisander.timeoflife.widget.MIN_LEGEND_ROW_DP
import de.chrisander.timeoflife.widget.WidgetColors
import de.chrisander.timeoflife.widget.drawPanel
import de.chrisander.timeoflife.widget.legend.LegendEntry
import de.chrisander.timeoflife.widget.legend.LegendPosition
import de.chrisander.timeoflife.widget.legend.drawLegend
import de.chrisander.timeoflife.widget.renderWidgetBitmap
import kotlin.math.min

internal fun renderBarWidget(
    progress: LifeProgress?,
    legend: List<LegendEntry>,
    legendPosition: LegendPosition,
    widthPx: Int,
    heightPx: Int,
    density: Float,
    typeface: Typeface,
): Bitmap = renderWidgetBitmap(widthPx, heightPx) { width, height, scale ->
    val dp = density * scale
    val padding = PADDING_DP * dp
    val maxBarHeight = MAX_BAR_AREA_DP * dp
    val minRowHeight = MIN_LEGEND_ROW_DP * dp

    drawPanel(width, height)
    val position =
        if (legend.isEmpty()) LegendPosition.HIDDEN else legendPosition.resolveForBar(heightPx / density)
    when (position) {
        LegendPosition.SIDE -> {
            val barRight = width * MAX_BAR_SHARE_WITH_LEGEND
            drawBar(
                progress,
                centeredBarArea(padding, padding, barRight, height - padding, maxBarHeight)
            )
            drawLegend(
                legend,
                RectF(barRight + padding, padding, width - padding, height - padding),
                minRowHeight,
                typeface,
            )
        }

        LegendPosition.BOTTOM -> {
            val barBottom =
                padding + min(maxBarHeight, (height - 2 * padding) * MAX_BAR_SHARE_WITH_LEGEND)
            drawBar(progress, RectF(padding, padding, width - padding, barBottom))
            drawLegend(
                legend,
                RectF(padding, barBottom + padding / 2, width - padding, height - padding),
                minRowHeight,
                typeface,
            )
        }

        else -> drawBar(
            progress,
            centeredBarArea(padding, padding, width - padding, height - padding, maxBarHeight)
        )
    }
}

/** Below the bar once there is room for at least [MIN_LEGEND_ROWS] rows, otherwise hidden. */
internal fun LegendPosition.resolveForBar(heightDp: Float): LegendPosition = when {
    this != LegendPosition.AUTOMATIC -> this
    heightDp >= MIN_HEIGHT_FOR_BOTTOM_LEGEND_DP -> LegendPosition.BOTTOM
    else -> LegendPosition.HIDDEN
}

private fun centeredBarArea(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    maxHeight: Float
): RectF {
    val centerY = (top + bottom) / 2
    val halfHeight = min(bottom - top, maxHeight) / 2
    return RectF(left, centerY - halfHeight, right, centerY + halfHeight)
}

/** Draws the bar into [area], leaving room above and below for the marker's overhang. */
private fun Canvas.drawBar(progress: LifeProgress?, area: RectF) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val overhang = area.height() * 0.1f
    val bar = RectF(area.left, area.top + overhang, area.right, area.bottom - overhang)

    drawBar(progress, bar, paint)
    if (progress != null) drawMarker(progress.progress, bar, overhang, paint)
}

private fun Canvas.drawBar(progress: LifeProgress?, bar: RectF, paint: Paint) {
    val corner = bar.height() * 0.3f
    val shape = Path().apply { addRoundRect(bar, corner, corner, Path.Direction.CW) }
    paint.style = Paint.Style.FILL
    paint.color = WidgetColors.FUTURE
    drawPath(shape, paint)

    var left = bar.left
    progress?.segments?.forEach { segment ->
        val right = left + bar.width() * segment.fraction.toFloat()
        // Intersected with the rounded shape instead of clipping, because the software canvas doesn't anti-alias clips.
        val segmentPath =
            Path().apply { addRect(left, bar.top, right, bar.bottom, Path.Direction.CW) }
        segmentPath.op(shape, Path.Op.INTERSECT)
        paint.color = WidgetColors.of(segment)
        drawPath(segmentPath, paint)
        left = right
    }
}

private fun Canvas.drawMarker(progress: Double, bar: RectF, overhang: Float, paint: Paint) {
    val x = bar.left + bar.width() * progress.toFloat()
    paint.style = Paint.Style.STROKE
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = bar.height() * 0.14f
    paint.color = WidgetColors.HAND
    paint.setShadowLayer(bar.height() * 0.1f, 0f, 0f, Color.argb(0xAA, 0, 0, 0))
    drawLine(x, bar.top - overhang, x, bar.bottom + overhang, paint)
    paint.clearShadowLayer()
}

private const val PADDING_DP = 12
private const val MAX_BAR_AREA_DP = 40
private const val MIN_LEGEND_ROWS = 2

/** Share of the widget's width (side legend) or of the available height (bottom legend) the bar may take at most. */
private const val MAX_BAR_SHARE_WITH_LEGEND = 0.6f

private const val MIN_HEIGHT_FOR_BOTTOM_LEGEND_DP =
    2 * PADDING_DP + MAX_BAR_AREA_DP + PADDING_DP / 2 + MIN_LEGEND_ROWS * MIN_LEGEND_ROW_DP
