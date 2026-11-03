package de.chrisander.timeoflife.widget.legend

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import de.chrisander.timeoflife.widget.WidgetColors
import kotlin.math.min

/**
 * Draws one row per entry (color dot, label, percent), vertically centered in [bounds].
 * Rows never get smaller than [minRowHeight]; entries that don't fit are left out from the end.
 */
internal fun Canvas.drawLegend(
    entries: List<LegendEntry>,
    bounds: RectF,
    minRowHeight: Float,
    typeface: Typeface,
) {
    val visibleCount = min(entries.size, (bounds.height() / minRowHeight).toInt())
    if (visibleCount == 0) return
    val visible = entries.take(visibleCount)
    val rowHeight =
        minOf(bounds.height() / visibleCount, bounds.height() / MIN_ROWS, bounds.width() * 0.16f)

    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val labelPaint = textPaint(rowHeight * 0.5f, WidgetColors.TEXT, Paint.Align.LEFT, typeface)
    val percentPaint =
        textPaint(rowHeight * 0.5f, WidgetColors.TEXT_DIM, Paint.Align.RIGHT, typeface)
    val dotRadius = rowHeight * 0.2f
    val labelStart = bounds.left + dotRadius * 2 + rowHeight * 0.3f
    val percentWidth = percentPaint.measureText("100 %")

    var top = bounds.centerY() - visibleCount * rowHeight / 2
    visible.forEach { entry ->
        val centerY = top + rowHeight / 2
        val baseline = centerY - (labelPaint.ascent() + labelPaint.descent()) / 2

        dotPaint.color = entry.color
        drawCircle(bounds.left + dotRadius, centerY, dotRadius, dotPaint)

        val labelWidth = bounds.right - percentWidth - rowHeight * 0.3f - labelStart
        val label =
            TextUtils.ellipsize(entry.label, labelPaint, labelWidth, TextUtils.TruncateAt.END)
        drawText(label, 0, label.length, labelStart, baseline, labelPaint)
        drawText("${entry.percent} %", bounds.right, baseline, percentPaint)

        top += rowHeight
    }
}

private fun textPaint(textSize: Float, color: Int, align: Paint.Align, typeface: Typeface) =
    TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.textSize = textSize
        this.color = color
        this.typeface = typeface
        textAlign = align
    }

/** Keeps rows from getting oversized when there are only a few entries. */
private const val MIN_ROWS = 4f
