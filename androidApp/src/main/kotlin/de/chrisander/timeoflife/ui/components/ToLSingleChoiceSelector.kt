package de.chrisander.timeoflife.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import de.chrisander.timeoflife.ui.theme.TimeOfLifeTheme

@Composable
fun <T> ToLSingleChoiceSelector(
    options: List<T>,
    selected: T?,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option == selected,
                onClick = { onSelect(option) },
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
private fun ToLSingleChoiceSelectorPreview() {
    TimeOfLifeTheme {
        ToLSingleChoiceSelector(
            options = listOf("Never", "Former", "Current"),
            selected = "Former",
            label = { it },
            onSelect = {},
        )
    }
}
