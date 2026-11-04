package de.chrisander.timeoflife.ui.onboarding

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.chrisander.timeoflife.R
import de.chrisander.timeoflife.domain.model.profile.Sex
import de.chrisander.timeoflife.ui.theme.TimeOfLifeTheme
import de.chrisander.timeoflife.util.toLocalDateFromUtcMillis
import de.chrisander.timeoflife.util.toUtcEpochMillis
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OnboardingContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun OnboardingContent(
    uiState: OnboardingUiState,
    onEvent: (OnboardingEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    SaveErrorSnackbar(
        show = uiState.showSaveError,
        snackbarHostState = snackbarHostState,
        onDismiss = { onEvent(OnboardingEvent.DismissError) },
    )

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.onboarding_title),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = stringResource(R.string.onboarding_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.name,
                onValueChange = { onEvent(OnboardingEvent.NameChanged(it)) },
                label = { Text(stringResource(R.string.onboarding_name_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            SexSelector(
                selected = uiState.sex,
                onSelect = { onEvent(OnboardingEvent.SexSelected(it)) },
            )

            BirthDateField(
                birthDate = uiState.birthDate,
                onBirthDateSelect = { onEvent(OnboardingEvent.BirthDateSelected(it)) },
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { onEvent(OnboardingEvent.Submit) },
                enabled = uiState.canSubmit,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.onboarding_submit))
            }
        }
    }
}

@Composable
private fun SaveErrorSnackbar(
    show: Boolean,
    snackbarHostState: SnackbarHostState,
    onDismiss: () -> Unit,
) {
    val message = stringResource(R.string.onboarding_save_error)
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    LaunchedEffect(show) {
        if (show) {
            snackbarHostState.showSnackbar(message)
            currentOnDismiss()
        }
    }
}

@Composable
private fun SexSelector(
    selected: Sex?,
    onSelect: (Sex) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.onboarding_sex_label),
            style = MaterialTheme.typography.labelLarge,
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            Sex.entries.forEachIndexed { index, sex ->
                SegmentedButton(
                    selected = sex == selected,
                    onClick = { onSelect(sex) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = Sex.entries.size),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                ) {
                    val sexLabel = when (sex) {
                        Sex.MALE -> R.string.sex_male
                        Sex.FEMALE -> R.string.sex_female
                        Sex.DIVERSE -> R.string.sex_diverse
                    }
                    Text(stringResource(sexLabel))
                }
            }
        }
    }
}

@Composable
private fun BirthDateField(
    birthDate: LocalDate?,
    onBirthDateSelect: (LocalDate) -> Unit,
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
        value = birthDate?.let { formatter.formatDate(it.toUtcEpochMillis(), locale) }.orEmpty(),
        onValueChange = {},
        readOnly = true,
        label = { Text(stringResource(R.string.onboarding_birth_date_label)) },
        placeholder = { Text(stringResource(R.string.onboarding_birth_date_placeholder)) },
        interactionSource = interactionSource,
        modifier = Modifier.fillMaxWidth(),
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = birthDate?.toUtcEpochMillis(),
            selectableDates = PastOrPresentSelectableDates,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onBirthDateSelect(it.toLocalDateFromUtcMillis())
                        }
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

private object PastOrPresentSelectableDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
        utcTimeMillis <= Clock.System.now().toEpochMilliseconds()

    override fun isSelectableYear(year: Int): Boolean =
        year <= Clock.System.todayIn(TimeZone.currentSystemDefault()).year
}

@Preview(showBackground = true)
@Composable
private fun OnboardingContentPreview() {
    TimeOfLifeTheme {
        OnboardingContent(
            uiState = OnboardingUiState(
                name = "Chris",
                sex = Sex.MALE,
                birthDate = LocalDate(1990, 5, 17)
            ),
            onEvent = {}
        )
    }
}
