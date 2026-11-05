package de.chrisander.timeoflife.ui.activity

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.chrisander.timeoflife.R
import de.chrisander.timeoflife.data.datasource.StaticBaselineLifeExpectancyProvider
import de.chrisander.timeoflife.domain.model.LifeSegment
import de.chrisander.timeoflife.domain.model.activity.ActivityId
import de.chrisander.timeoflife.domain.model.profile.Sex
import de.chrisander.timeoflife.domain.model.profile.UserProfile
import de.chrisander.timeoflife.domain.usecase.GetLifeTimeBreakdownUseCase
import de.chrisander.timeoflife.domain.usecase.LifeTimeCalculator
import de.chrisander.timeoflife.domain.usecase.StudyBasedLifespanEstimator
import de.chrisander.timeoflife.ui.activity.formatter.whenText
import de.chrisander.timeoflife.ui.activity.mapper.activityDetail
import de.chrisander.timeoflife.ui.formatter.color
import de.chrisander.timeoflife.ui.formatter.label
import de.chrisander.timeoflife.ui.formatter.perDayText
import de.chrisander.timeoflife.ui.formatter.text
import de.chrisander.timeoflife.ui.theme.TimeOfLifeTheme
import de.chrisander.timeoflife.ui.theme.card
import kotlinx.datetime.LocalDate
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
internal fun Schedule(
    detail: ActivityDetailUiState,
    modifier: Modifier = Modifier,
) {
    val calculation = detail.calculation
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(
                if (calculation is ActivityCalculation.Scheduled) {
                    R.string.detail_schedule_title
                } else {
                    R.string.detail_free_time_title
                },
            ),
            style = MaterialTheme.typography.titleLarge,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.card,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                when (calculation) {
                    is ActivityCalculation.Scheduled -> {
                        Row {
                            Fact(
                                title = stringResource(R.string.detail_how_often),
                                value = calculation.schedule.recurrence.text(),
                                modifier = Modifier.weight(1f),
                            )
                            Fact(
                                title = stringResource(R.string.detail_per_day),
                                value = perDayText(calculation.shareOfTime),
                                modifier = Modifier.weight(1f),
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Fact(title = stringResource(R.string.detail_when), value = calculation.whenText())
                            AgeTimeline(calculation, detail.lifespanYears, detail.segment.color)
                        }
                    }

                    is ActivityCalculation.FreeTime -> AverageDays(calculation)
                }
            }
        }
    }
}

@Composable
private fun AverageDays(calculation: ActivityCalculation.FreeTime) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            AverageDay(stringResource(R.string.detail_so_far), calculation.daySoFar)
            if (calculation.dayAhead.isNotEmpty()) {
                AverageDay(stringResource(R.string.activity_detail_ahead), calculation.dayAhead)
            }
            HourScale()
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        DayLegend(calculation.daySoFar.drop(1))
    }
}

@Composable
private fun AverageDay(
    title: String,
    day: List<LifeSegment>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(
                    R.string.detail_free_per_day,
                    perDayText(day.first().fraction)
                ),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(DAY_BAR_HEIGHT)
                .clip(CircleShape),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            day.filter { it.fraction > 0 }.forEach {
                val emphasized = it is LifeSegment.FreeTime
                Box(
                    Modifier
                        .weight(it.fraction.toFloat())
                        .fillMaxHeight()
                        .background(it.color.copy(alpha = if (emphasized) 1f else MUTED_ALPHA)),
                )
            }
        }
    }
}

@Composable
private fun HourScale() {
    Row(Modifier.fillMaxWidth()) {
        listOf(0, HOURS_PER_DAY / 2, HOURS_PER_DAY).forEachIndexed { index, hours ->
            if (index > 0) Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(R.string.duration_hours, hours),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DayLegend(activities: List<LifeSegment>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        activities.forEach {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(it.color),
                )
                Text(
                    text = stringResource(
                        R.string.detail_segment_per_day, it.label(),
                        perDayText(it.fraction)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun Fact(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun AgeTimeline(
    calculation: ActivityCalculation.Scheduled,
    lifespanYears: Double,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val end = max(lifespanYears, calculation.age)
    val from = (calculation.schedule.fromAge ?: 0).toDouble()
    val untilAge = calculation.schedule.untilAge?.takeIf { it < end }
    val until = untilAge?.toDouble() ?: end
    val track = MaterialTheme.colorScheme.surfaceVariant
    val marker = MaterialTheme.colorScheme.onSurface
    val markerRing = MaterialTheme.colorScheme.card
    val labelStyle = MaterialTheme.typography.labelMedium
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        AtFraction(calculation.age / end) {
            Text(text = stringResource(R.string.detail_today), style = labelStyle)
        }
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(16.dp),
        ) {
            val stroke = TRACK_WIDTH.toPx()
            val y = size.height / 2
            fun x(age: Double) = stroke / 2 + (size.width - stroke) * (age / end).toFloat()
            drawLine(track, Offset(x(0.0), y), Offset(x(end), y), stroke, StrokeCap.Round)
            if (until > from) drawLine(color, Offset(x(from), y), Offset(x(until), y), stroke, StrokeCap.Round)
            drawCircle(markerRing, MARKER_RING_RADIUS.toPx(), Offset(x(calculation.age), y))
            drawCircle(marker, MARKER_RADIUS.toPx(), Offset(x(calculation.age), y))
        }
        Box(Modifier.fillMaxWidth()) {
            AtFraction(from / end) {
                Text(
                    text = if (from == 0.0) stringResource(R.string.detail_birth) else from.toInt().toString(),
                    style = labelStyle,
                    color = labelColor,
                )
            }
            if (untilAge != null) {
                AtFraction(until / end) {
                    Text(text = untilAge.toString(), style = labelStyle, color = labelColor)
                }
            }
        }
    }
}

@Composable
private fun AtFraction(
    fraction: Double,
    content: @Composable () -> Unit,
) {
    Layout(content = content, modifier = Modifier.fillMaxWidth()) { measurables, constraints ->
        val placeable = measurables.first().measure(constraints.copy(minWidth = 0))
        val maxX = (constraints.maxWidth - placeable.width).coerceAtLeast(0)
        val x = (constraints.maxWidth * fraction - placeable.width / 2.0).roundToInt().coerceIn(0, maxX)
        layout(constraints.maxWidth, placeable.height) { placeable.place(x, 0) }
    }
}

private val TRACK_WIDTH = 8.dp
private val DAY_BAR_HEIGHT = 14.dp
private const val MUTED_ALPHA = 0.4f
private const val HOURS_PER_DAY = 24
private val MARKER_RADIUS = 5.dp
private val MARKER_RING_RADIUS = 8.dp


@Preview(showBackground = true)
@Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ActivitySchedulePreview() {
    TimeOfLifeTheme {
        val breakdown = GetLifeTimeBreakdownUseCase(
            StudyBasedLifespanEstimator(StaticBaselineLifeExpectancyProvider),
            LifeTimeCalculator(),
        )(UserProfile("Chris", Sex.MALE, LocalDate(1990, 5, 17)), LocalDate(2026, 11, 4))
        breakdown.activityDetail(ActivityId("default:WORK"))?.let { Schedule(it) }
    }
}

@Preview(showBackground = true)
@Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun FreeTimeSchedulePreview() {
    TimeOfLifeTheme {
        val breakdown = GetLifeTimeBreakdownUseCase(
            StudyBasedLifespanEstimator(StaticBaselineLifeExpectancyProvider),
            LifeTimeCalculator(),
        )(UserProfile("Chris", Sex.MALE, LocalDate(1990, 5, 17)), LocalDate(2026, 11, 4))
        breakdown.activityDetail(null)?.let { Schedule(it) }
    }
}
