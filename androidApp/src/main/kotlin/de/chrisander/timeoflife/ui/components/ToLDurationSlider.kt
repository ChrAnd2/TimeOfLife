package de.chrisander.timeoflife.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.chrisander.timeoflife.ui.formatter.durationText
import de.chrisander.timeoflife.ui.theme.TimeOfLifeTheme
import kotlin.math.roundToInt

/**
 * Picks a duration from [optionsMinutes], which may be spaced unevenly so that short and long durations both get
 * usable precision. The slider moves over the options' positions, not their values. It has no tick marks, since
 * there can be hundreds of options.
 */
@Composable
fun ToLDurationSlider(
    optionsMinutes: List<Int>,
    selectedMinutes: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = durationText(selectedMinutes),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        val selectedIndex = optionsMinutes.indexOf(selectedMinutes).coerceAtLeast(0)
        Slider(
            value = selectedIndex.toFloat(),
            onValueChange = {
                val option = optionsMinutes[it.roundToInt()]
                if (option != selectedMinutes) onSelect(option)
            },
            valueRange = 0f..optionsMinutes.lastIndex.coerceAtLeast(1).toFloat(),
            enabled = optionsMinutes.size > 1,
            colors = SliderDefaults.colors(inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant),
        )
        if (optionsMinutes.isNotEmpty()) {
            Row {
                Text(
                    text = durationText(optionsMinutes.first()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = durationText(optionsMinutes.last()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ToLDurationSliderPreview() {
    TimeOfLifeTheme {
        ToLDurationSlider(
            optionsMinutes = (1..12).map { it * 5 } + (5..16).map { it * 15 } + (9..24).map { it * 60 },
            selectedMinutes = 90,
            onSelect = {},
        )
    }
}
