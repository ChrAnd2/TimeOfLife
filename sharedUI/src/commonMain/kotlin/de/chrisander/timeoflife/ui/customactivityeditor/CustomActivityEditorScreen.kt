package de.chrisander.timeoflife.ui.customactivityeditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.chrisander.timeoflife.domain.model.activity.Recurrence
import de.chrisander.timeoflife.resources.Res
import de.chrisander.timeoflife.resources.cancel
import de.chrisander.timeoflife.resources.custom_activity_add_title
import de.chrisander.timeoflife.resources.custom_activity_ages_error
import de.chrisander.timeoflife.resources.custom_activity_ages_hint
import de.chrisander.timeoflife.resources.custom_activity_ages_label
import de.chrisander.timeoflife.resources.custom_activity_delete
import de.chrisander.timeoflife.resources.custom_activity_delete_body
import de.chrisander.timeoflife.resources.custom_activity_delete_title
import de.chrisander.timeoflife.resources.custom_activity_duration_label
import de.chrisander.timeoflife.resources.custom_activity_edit_title
import de.chrisander.timeoflife.resources.custom_activity_every_label
import de.chrisander.timeoflife.resources.custom_activity_from_age_label
import de.chrisander.timeoflife.resources.custom_activity_interval_label
import de.chrisander.timeoflife.resources.custom_activity_name_label
import de.chrisander.timeoflife.resources.custom_activity_summary
import de.chrisander.timeoflife.resources.custom_activity_until_age_label
import de.chrisander.timeoflife.resources.ic_delete
import de.chrisander.timeoflife.resources.interval_unit_days
import de.chrisander.timeoflife.resources.interval_unit_months
import de.chrisander.timeoflife.resources.interval_unit_weeks
import de.chrisander.timeoflife.resources.interval_unit_years
import de.chrisander.timeoflife.resources.save
import de.chrisander.timeoflife.resources.save_error
import de.chrisander.timeoflife.ui.components.ToLDurationSlider
import de.chrisander.timeoflife.ui.components.ToLNumberField
import de.chrisander.timeoflife.ui.components.ToLSingleChoiceSelector
import de.chrisander.timeoflife.ui.components.ToLTopAppBar
import de.chrisander.timeoflife.ui.customactivityeditor.model.IntervalUnit
import de.chrisander.timeoflife.ui.formatter.perDayText
import de.chrisander.timeoflife.ui.formatter.text
import de.chrisander.timeoflife.ui.navigation.CustomActivityEditorRoute
import de.chrisander.timeoflife.ui.theme.TimeOfLifeTheme
import kotlinx.datetime.DateTimeUnit
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Duration.Companion.hours

@Composable
fun CustomActivityEditorScreen(
    route: CustomActivityEditorRoute,
    viewModel: CustomActivityEditorViewModel = koinViewModel { parametersOf(route) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CustomActivityEditorContent(uiState = uiState, onEvent = viewModel::onEvent)
}

@Composable
private fun CustomActivityEditorContent(
    uiState: CustomActivityEditorUiState?,
    onEvent: (CustomActivityEditorEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val saveError = stringResource(Res.string.save_error)
    val currentOnEvent by rememberUpdatedState(onEvent)
    LaunchedEffect(uiState?.showSaveError) {
        if (uiState?.showSaveError == true) {
            snackbarHostState.showSnackbar(saveError)
            currentOnEvent(CustomActivityEditorEvent.DismissError)
        }
    }
    if (uiState?.showDeleteConfirmation == true) {
        DeleteDialog(uiState.name, onEvent)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            ToLTopAppBar(
                title = stringResource(
                    if (uiState?.isNew == false) {
                        Res.string.custom_activity_edit_title
                    } else {
                        Res.string.custom_activity_add_title
                    },
                ),
                onBack = { onEvent(CustomActivityEditorEvent.Back) },
                actions = {
                    if (uiState?.isNew == false) {
                        IconButton(onClick = { onEvent(CustomActivityEditorEvent.Delete) }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_delete),
                                contentDescription = stringResource(Res.string.custom_activity_delete),
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        if (uiState == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { onEvent(CustomActivityEditorEvent.NameChanged(it)) },
                label = { Text(stringResource(Res.string.custom_activity_name_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            IntervalSection(uiState, onEvent)
            DurationSection(uiState, onEvent)
            AgeSection(uiState, onEvent)
            Button(
                onClick = { onEvent(CustomActivityEditorEvent.Save) },
                enabled = uiState.canSave,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.save))
            }
        }
    }
}

@Composable
private fun DurationSection(
    uiState: CustomActivityEditorUiState,
    onEvent: (CustomActivityEditorEvent) -> Unit,
) {
    Section(title = stringResource(Res.string.custom_activity_duration_label)) {
        ToLDurationSlider(
            optionsMinutes = uiState.durationOptions,
            selectedMinutes = uiState.durationMinutes,
            onSelect = { onEvent(CustomActivityEditorEvent.DurationSelected(it)) },
        )
        uiState.recurrence?.let { RecurrenceSummary(it) }
    }
}

/** Spells out what duration and interval mean together, e.g. "3 h every 4 days · about 45 min a day". */
@Composable
private fun RecurrenceSummary(recurrence: Recurrence) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        // A daily average says nothing new for something that happens every day.
        val text = if (recurrence.interval == DateTimeUnit.DAY) {
            recurrence.text()
        } else {
            stringResource(
                Res.string.custom_activity_summary,
                recurrence.text(),
                perDayText(recurrence.fractionOfTime),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun IntervalSection(
    uiState: CustomActivityEditorUiState,
    onEvent: (CustomActivityEditorEvent) -> Unit,
) {
    Section(title = stringResource(Res.string.custom_activity_interval_label)) {
        ToLNumberField(
            value = uiState.every,
            onValueChange = { onEvent(CustomActivityEditorEvent.EveryChanged(it)) },
            label = stringResource(Res.string.custom_activity_every_label),
            isError = uiState.everyError,
            keyboardType = KeyboardType.Number,
            modifier = Modifier.fillMaxWidth(),
        )
        // The unit reads as the end of "every 4 …", so it follows the number's plural.
        val count = uiState.every.toIntOrNull() ?: 1
        ToLSingleChoiceSelector(
            options = IntervalUnit.entries,
            selected = uiState.unit,
            label = {
                pluralStringResource(
                    when (it) {
                        IntervalUnit.DAY -> Res.plurals.interval_unit_days
                        IntervalUnit.WEEK -> Res.plurals.interval_unit_weeks
                        IntervalUnit.MONTH -> Res.plurals.interval_unit_months
                        IntervalUnit.YEAR -> Res.plurals.interval_unit_years
                    },
                    count,
                )
            },
            onSelect = { onEvent(CustomActivityEditorEvent.UnitSelected(it)) },
        )
    }
}

@Composable
private fun AgeSection(
    uiState: CustomActivityEditorUiState,
    onEvent: (CustomActivityEditorEvent) -> Unit,
) {
    Section(
        title = stringResource(Res.string.custom_activity_ages_label),
        hint = stringResource(Res.string.custom_activity_ages_hint),
        error = stringResource(Res.string.custom_activity_ages_error).takeIf { uiState.ageError },
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ToLNumberField(
                value = uiState.fromAge,
                onValueChange = { onEvent(CustomActivityEditorEvent.FromAgeChanged(it)) },
                label = stringResource(Res.string.custom_activity_from_age_label),
                isError = uiState.ageError,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
            )
            ToLNumberField(
                value = uiState.untilAge,
                onValueChange = { onEvent(CustomActivityEditorEvent.UntilAgeChanged(it)) },
                label = stringResource(Res.string.custom_activity_until_age_label),
                isError = uiState.ageError,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun Section(
    title: String,
    hint: String? = null,
    error: String? = null,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.labelLarge)
        if (hint != null) {
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        content()
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun DeleteDialog(
    name: String,
    onEvent: (CustomActivityEditorEvent) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onEvent(CustomActivityEditorEvent.DismissDelete) },
        title = { Text(stringResource(Res.string.custom_activity_delete_title, name)) },
        text = { Text(stringResource(Res.string.custom_activity_delete_body)) },
        confirmButton = {
            TextButton(onClick = { onEvent(CustomActivityEditorEvent.ConfirmDelete) }) {
                Text(stringResource(Res.string.custom_activity_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(CustomActivityEditorEvent.DismissDelete) }) {
                Text(stringResource(Res.string.cancel))
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun CustomActivityEditorContentPreview() {
    TimeOfLifeTheme {
        CustomActivityEditorContent(
            uiState = CustomActivityEditorUiState(
                isNew = false,
                name = "Football",
                durationMinutes = 180,
                durationOptions = PreviewDurationOptions,
                every = "1",
                unit = IntervalUnit.WEEK,
                recurrence = Recurrence(3.hours, DateTimeUnit.WEEK),
                fromAge = "8",
                canSave = true,
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomActivityEditorErrorPreview() {
    TimeOfLifeTheme {
        CustomActivityEditorContent(
            uiState = CustomActivityEditorUiState(
                name = "Reading",
                durationMinutes = 30,
                durationOptions = PreviewDurationOptions,
                every = "",
                unit = IntervalUnit.DAY,
                fromAge = "20",
                untilAge = "10",
                everyError = true,
                ageError = true,
            ),
            onEvent = {},
        )
    }
}

private val PreviewDurationOptions =
    (1..12).map { it * 5 } + (5..16).map { it * 15 } + (9..24).map { it * 60 }
