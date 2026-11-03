package de.chrisander.timeoflife.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.createBitmap
import kotlin.math.max
import kotlin.math.min

/**
 * Creates the bitmap for a widget of [widthPx] × [heightPx] and lets [draw] paint it.
 *
 * RemoteViews bitmaps count against a binder memory limit, so large widgets are drawn smaller and scaled up by the
 * ImageView. [draw] receives the actual bitmap size and the [scale] applied to it, so dp-based sizes can be
 * converted with `dp * density * scale`.
 */
internal fun renderWidgetBitmap(
    widthPx: Int,
    heightPx: Int,
    draw: Canvas.(width: Int, height: Int, scale: Float) -> Unit,
): Bitmap {
    val scale = min(1f, MAX_SIZE_PX.toFloat() / max(widthPx, heightPx))
    val width = (widthPx * scale).toInt().coerceAtLeast(MIN_SIZE_PX)
    val height = (heightPx * scale).toInt().coerceAtLeast(MIN_SIZE_PX)
    return createBitmap(width, height).also { Canvas(it).draw(width, height, scale) }
}

/** The rounded background behind a widget's content. */
internal fun Canvas.drawPanel(width: Int, height: Int) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = WidgetColors.FACE }
    val corner = min(width, height) * 0.12f
    drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), corner, corner, paint)
}

/** Legend rows never get smaller than this, so the text stays legible. */
internal const val MIN_LEGEND_ROW_DP = 16

private const val MIN_SIZE_PX = 64
private const val MAX_SIZE_PX = 1024
