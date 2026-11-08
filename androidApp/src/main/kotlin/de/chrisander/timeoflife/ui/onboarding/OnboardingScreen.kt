package de.chrisander.timeoflife.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.chrisander.timeoflife.R
import de.chrisander.timeoflife.domain.model.profile.Sex
import de.chrisander.timeoflife.ui.healthsettings.HealthSettings
import de.chrisander.timeoflife.ui.healthsettings.HealthSettingsUiState
import de.chrisander.timeoflife.ui.healthsettings.model.SmokingChoice
import de.chrisander.timeoflife.ui.personalsettings.PersonalSettings
import de.chrisander.timeoflife.ui.theme.TimeOfLifeTheme
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel

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
                .verticalScroll(
                    rememberSaveable(
                        uiState.step,
                        saver = ScrollState.Saver
                    ) { ScrollState(0) })
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (uiState.step) {
                OnboardingStep.PERSONAL -> PersonalStep(uiState, onEvent)
                OnboardingStep.HEALTH -> {
                    BackHandler { onEvent(OnboardingEvent.Back) }
                    StepHeader(
                        title = stringResource(R.string.onboarding_health_title),
                        subtitle = stringResource(R.string.onboarding_health_subtitle),
                        onBack = { onEvent(OnboardingEvent.Back) },
                    )
                    HealthSettings(
                        uiState = uiState.health,
                        onEvent = { onEvent(OnboardingEvent.Health(it)) },
                    )
                    Button(
                        onClick = { onEvent(OnboardingEvent.Submit) },
                        enabled = uiState.canSubmit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    ) {
                        Text(stringResource(R.string.onboarding_submit))
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonalStep(
    uiState: OnboardingUiState,
    onEvent: (OnboardingEvent) -> Unit,
) = Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
    StepHeader(
        title = stringResource(R.string.onboarding_title),
        subtitle = stringResource(R.string.onboarding_subtitle),
    )
    PersonalSettings(
        name = uiState.name,
        onNameChange = { onEvent(OnboardingEvent.NameChanged(it)) },
        sex = uiState.sex,
        onSexSelect = { onEvent(OnboardingEvent.SexSelected(it)) },
        birthDate = uiState.birthDate,
        onBirthDateSelect = { onEvent(OnboardingEvent.BirthDateSelected(it)) },
    )
    Button(
        onClick = { onEvent(OnboardingEvent.Continue) },
        enabled = uiState.canContinue,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    ) {
        Text(stringResource(R.string.onboarding_continue))
    }
}

@Composable
private fun StepHeader(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier.padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.offset(x = (-12).dp)) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.onboarding_back),
                )
            }
        }
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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

@Preview(showBackground = true)
@Composable
private fun OnboardingContentPreview() {
    TimeOfLifeTheme {
        OnboardingContent(
            uiState = OnboardingUiState(
                name = "Chris",
                sex = Sex.MALE,
                birthDate = LocalDate(1990, 5, 17),
                canContinue = true,
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingHealthPreview() {
    TimeOfLifeTheme {
        OnboardingContent(
            uiState = OnboardingUiState(
                step = OnboardingStep.HEALTH,
                name = "Chris",
                sex = Sex.MALE,
                birthDate = LocalDate(1990, 5, 17),
                health = HealthSettingsUiState(
                    countryCode = "DE",
                    height = "182",
                    weight = "78",
                    bmi = 23.5,
                    smoking = SmokingChoice.FORMER,
                ),
                canContinue = true,
            ),
            onEvent = {},
        )
    }
}
