package de.chrisander.timeoflife.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.chrisander.timeoflife.resources.Res
import de.chrisander.timeoflife.resources.cancel
import de.chrisander.timeoflife.resources.ok
import de.chrisander.timeoflife.ui.formatter.formatLongDate
import de.chrisander.timeoflife.ui.theme.TimeOfLifeTheme
import de.chrisander.timeoflife.util.toLocalDateFromUtcMillis
import de.chrisander.timeoflife.util.toUtcEpochMillis
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

/** A read-only text field that opens a date picker dialog when tapped. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToLDateField(
    date: LocalDate?,
    onDateSelect: (LocalDate) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    selectableDates: SelectableDates = DatePickerDefaults.AllDates,
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) showDatePicker = true
        }
    }

    OutlinedTextField(
        value = date?.let(::formatLongDate).orEmpty(),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        interactionSource = interactionSource,
        modifier = modifier.fillMaxWidth(),
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date?.toUtcEpochMillis(),
            selectableDates = selectableDates,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { onDateSelect(it.toLocalDateFromUtcMillis()) }
                        showDatePicker = false
                    },
                    enabled = datePickerState.selectedDateMillis != null,
                ) {
                    Text(stringResource(Res.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ToLDateFieldPreview() {
    TimeOfLifeTheme {
        ToLDateField(date = LocalDate(1990, 5, 17), onDateSelect = {}, label = "Birth date")
    }
}
