package de.chrisander.timeoflife.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import de.chrisander.timeoflife.R
import de.chrisander.timeoflife.domain.model.LifeProgress
import de.chrisander.timeoflife.domain.model.LifeSegment
import de.chrisander.timeoflife.ui.theme.card
import de.chrisander.timeoflife.widget.clock.ClockColors
import de.chrisander.timeoflife.widget.clock.drawClock
import kotlin.math.min

/**
 * The clock widget's face in the app's theme colors, so it fits the light background too. The pie sweeps open when it
 * first appears.
 */
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
        face = scheme.card.toArgb(),
        rim = scheme.outlineVariant.toArgb(),
        future = scheme.surfaceVariant.toArgb(),
        tick = scheme.onSurface.toArgb(),
        minorTick = scheme.onSurfaceVariant.copy(alpha = MINOR_TICK_ALPHA).toArgb(),
        hand = scheme.onSurface.toArgb(),
    )
    val description = stringResource(R.string.clock_widget_content_description, progress.clockText)

    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description },
    ) {
        val shown = progress.sweptTo(sweep.value.toDouble())
        drawIntoCanvas {
            it.nativeCanvas.drawClock(
                shown,
                center.x,
                center.y,
                min(size.width, size.height) / 2,
                colors,
            )
        }
    }
}

private fun LifeProgress.sweptTo(share: Double): LifeProgress = copy(
    progress = progress * share,
    segments = segments.map {
        when (it) {
            is LifeSegment.ActivitySegment -> it.copy(fraction = it.fraction * share)
            is LifeSegment.FreeTime -> it.copy(fraction = it.fraction * share)
        }
    },
)

private const val SWEEP_MILLIS = 1400
private const val MINOR_TICK_ALPHA = 0.6f
