package de.chrisander.timeoflife.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import de.chrisander.timeoflife.ui.theme.TimeOfLifeTheme

@Composable
fun <T> ToLMultiChoiceSelector(
    options: List<T>,
    selected: Set<T>,
    label: @Composable (T) -> String,
    onToggle: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    MultiChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                checked = option in selected,
                onCheckedChange = { onToggle(option) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                colors = ToLSegmentedButtonDefaults.colors(),
            ) {
                Text(text = label(option), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ToLMultiChoiceSelectorPreview() {
    TimeOfLifeTheme {
        ToLMultiChoiceSelector(
            options = listOf("Diabetes", "Heart attack", "Stroke"),
            selected = setOf("Diabetes"),
            label = { it },
            onToggle = {},
        )
    }
}
