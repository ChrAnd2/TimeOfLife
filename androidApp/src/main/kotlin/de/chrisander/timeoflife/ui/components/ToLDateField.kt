package de.chrisander.timeoflife.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import de.chrisander.timeoflife.ui.theme.TimeOfLifeTheme
import de.chrisander.timeoflife.util.toLocalDateFromUtcMillis
import de.chrisander.timeoflife.util.toUtcEpochMillis
import kotlinx.datetime.LocalDate

/** A read-only text field that opens a date picker dialog when tapped. */
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

    val formatter = remember { DatePickerDefaults.dateFormatter() }
    val locale = Locale.current.platformLocale
    OutlinedTextField(
        value = date?.let { formatter.formatDate(it.toUtcEpochMillis(), locale) }.orEmpty(),
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
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(android.R.string.cancel))
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
