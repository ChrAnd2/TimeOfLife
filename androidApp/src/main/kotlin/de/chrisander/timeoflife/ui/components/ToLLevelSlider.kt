package de.chrisander.timeoflife.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.chrisander.timeoflife.ui.theme.TimeOfLifeTheme
import kotlin.math.roundToInt

/** A stepped slider for ordered [levels], with the selected level and what it means above it. */
@Composable
fun <T> ToLLevelSlider(
    levels: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    description: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label(selected),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 16.dp),
            )
            Text(
                text = description(selected),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f),
            )
        }
        Slider(
            value = levels.indexOf(selected).toFloat(),
            onValueChange = { onSelect(levels[it.roundToInt()]) },
            valueRange = 0f..levels.lastIndex.toFloat(),
            steps = levels.size - 2,
            colors = SliderDefaults.colors(
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                inactiveTickColor = MaterialTheme.colorScheme.onSurfaceVariant,
                activeTickColor = MaterialTheme.colorScheme.onPrimary,
            ),
        )
        Row {
            Text(
                text = label(levels.first()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = label(levels.last()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ToLLevelSliderPreview() {
    TimeOfLifeTheme {
        ToLLevelSlider(
            levels = listOf("None", "Low", "Moderate", "High"),
            selected = "Moderate",
            label = { it },
            description = { "2 to 4 hours a week" },
            onSelect = {},
        )
    }
}
