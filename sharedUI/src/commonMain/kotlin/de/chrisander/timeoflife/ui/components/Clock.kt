@file:Suppress("MagicNumber")

package de.chrisander.timeoflife.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.chrisander.timeoflife.brand.SegmentColors
import de.chrisander.timeoflife.domain.model.activity.Activity
import de.chrisander.timeoflife.domain.model.activity.DefaultActivityType
import de.chrisander.timeoflife.domain.model.progress.LifeProgress
import de.chrisander.timeoflife.domain.model.progress.LifeSegment
import de.chrisander.timeoflife.resources.Res
import de.chrisander.timeoflife.resources.clock_widget_content_description
import de.chrisander.timeoflife.ui.theme.TimeOfLifeTheme
import de.chrisander.timeoflife.ui.theme.card
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun Clock(
    progress: LifeProgress,
    modifier: Modifier = Modifier,
) {
    val sweep = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        sweep.animateTo(1f, tween(durationMillis = SWEEP_MILLIS, easing = FastOutSlowInEasing))
    }
    val scheme = MaterialTheme.colorScheme
    val colors = ClockColors(
        face = scheme.card,
        rim = scheme.outlineVariant,
        future = scheme.surfaceVariant,
        tick = scheme.onSurface,
        minorTick = scheme.onSurfaceVariant.copy(alpha = MINOR_TICK_ALPHA),
        hand = scheme.onSurface,
    )
    val description =
        stringResource(Res.string.clock_widget_content_description, progress.clockText)

    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description },
    ) {
        drawClock(progress.sweptTo(sweep.value.toDouble()), colors)
    }
}

private data class ClockColors(
    val face: Color,
    val rim: Color,
    val future: Color,
    val tick: Color,
    val minorTick: Color,
    val hand: Color,
)

private fun DrawScope.drawClock(progress: LifeProgress, colors: ClockColors) {
    val r = min(size.width, size.height) / 2
    drawCircle(colors.face, r, center)
    val rimWidth = r * 0.025f
    drawCircle(colors.rim, r - rimWidth / 2, center, style = Stroke(rimWidth))
    drawPie(progress, r * 0.70f, colors)
    drawTicks(r, colors)
    drawHand(progress.progress, r, colors)
}

private fun DrawScope.drawPie(progress: LifeProgress, pieR: Float, colors: ClockColors) {
    drawCircle(colors.future, pieR, center)
    val topLeft = Offset(center.x - pieR, center.y - pieR)
    val pieSize = Size(pieR * 2, pieR * 2)
    var start = -90f
    progress.segments.forEach { segment ->
        val sweep = 360f * segment.fraction.toFloat()
        drawArc(
            Color(SegmentColors.of(segment)),
            start,
            sweep,
            useCenter = true,
            topLeft = topLeft,
            size = pieSize
        )
        start += sweep
    }
}

private fun DrawScope.drawTicks(r: Float, colors: ClockColors) {
    for (hour in 0 until 24) {
        val major = hour % 3 == 0
        val angle = fractionAngle(hour / 24.0)
        val r1 = r * if (major) 0.85f else 0.89f
        val r2 = r * 0.94f
        drawLine(
            color = if (major) colors.tick else colors.minorTick,
            start = pointAt(angle, r1),
            end = pointAt(angle, r2),
            strokeWidth = r * if (major) 0.022f else 0.012f,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawHand(progress: Double, r: Float, colors: ClockColors) {
    val angle = fractionAngle(progress)
    val end = pointAt(angle, r * 0.62f)
    drawLine(SHADOW, center, end, strokeWidth = r * 0.09f, cap = StrokeCap.Round)
    drawLine(colors.hand, center, end, strokeWidth = r * 0.06f, cap = StrokeCap.Round)
    drawCircle(colors.hand, r * 0.055f, center)
    drawCircle(colors.face, r * 0.022f, center)
}

private fun DrawScope.pointAt(angle: Double, radius: Float) =
    Offset(center.x + radius * cos(angle).toFloat(), center.y + radius * sin(angle).toFloat())

private fun fractionAngle(fraction: Double): Double = (-90.0 + 360.0 * fraction) * PI / 180.0

private fun LifeProgress.sweptTo(share: Double): LifeProgress = copy(
    progress = progress * share,
    segments = segments.map {
        when (it) {
            is LifeSegment.ActivitySegment -> it.copy(fraction = it.fraction * share)
            is LifeSegment.FreeTime -> it.copy(fraction = it.fraction * share)
        }
    },
)

private val SHADOW = Color(0x33000000)
private const val SWEEP_MILLIS = 1400
private const val MINOR_TICK_ALPHA = 0.6f

@Preview(showBackground = true)
@Composable
private fun ClockPreview() {
    TimeOfLifeTheme {
        Clock(
            progress = LifeProgress(
                progress = 0.45,
                segments = listOf(
                    LifeSegment.ActivitySegment(
                        Activity.DefaultActivity(DefaultActivityType.SLEEP),
                        0.15
                    ),
                    LifeSegment.ActivitySegment(
                        Activity.DefaultActivity(DefaultActivityType.WORK),
                        0.08
                    ),
                    LifeSegment.FreeTime(0.22),
                ),
                clockText = "10:48",
            ),
            modifier = Modifier.size(200.dp),
        )
    }
}
