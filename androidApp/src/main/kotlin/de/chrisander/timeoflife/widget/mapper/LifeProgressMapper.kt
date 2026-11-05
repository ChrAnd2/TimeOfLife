package de.chrisander.timeoflife.widget.mapper

import de.chrisander.timeoflife.domain.model.LifeProgress
import de.chrisander.timeoflife.domain.model.LifeSegment
import de.chrisander.timeoflife.domain.model.lifetime.LifeTimeBreakdown
import kotlin.time.Duration

fun LifeTimeBreakdown.toLifeProgress(): LifeProgress {
    val progress = if (total > Duration.ZERO) lived / total else 0.0
    val spent = activities.filter { it.spent > Duration.ZERO }
    val spentTotal = spent.fold(Duration.ZERO) { sum, activity -> sum + activity.spent }

    // Activities with more than 24 h per day combined would overflow the lived share, so they are scaled to fit.
    val scale = if (spentTotal > lived) lived / spentTotal else 1.0
    val activitySegments = spent.map {
        LifeSegment.ActivitySegment(it.activity, it.spent / total * scale)
    }
    val freeTime = progress - activitySegments.sumOf { it.fraction }

    return LifeProgress(
        progress = progress,
        segments = activitySegments + listOfNotNull(
            LifeSegment.FreeTime(freeTime).takeIf { freeTime > EPSILON }),
        clockText = lifeClockText(progress),
    )
}

private fun lifeClockText(progress: Double): String {
    val minutes = (progress * MINUTES_PER_DAY).toInt()
    return "${(minutes / MINUTES_PER_HOUR).toString().padStart(2, '0')}:${
        (minutes % MINUTES_PER_HOUR).toString().padStart(2, '0')
    }"
}

private const val MINUTES_PER_HOUR = 60
private const val MINUTES_PER_DAY = 24 * MINUTES_PER_HOUR
private const val EPSILON = 1e-9
