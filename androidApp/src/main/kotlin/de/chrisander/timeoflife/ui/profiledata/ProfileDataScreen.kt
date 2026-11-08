package de.chrisander.timeoflife.ui.profiledata

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.chrisander.timeoflife.R
import de.chrisander.timeoflife.domain.model.profile.Sex
import de.chrisander.timeoflife.ui.components.ToLTopAppBar
import de.chrisander.timeoflife.ui.healthsettings.HealthSettings
import de.chrisander.timeoflife.ui.healthsettings.HealthSettingsUiState
import de.chrisander.timeoflife.ui.healthsettings.model.SmokingChoice
import de.chrisander.timeoflife.ui.personalsettings.PersonalSettings
import de.chrisander.timeoflife.ui.theme.TimeOfLifeTheme
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileDataScreen(
    viewModel: ProfileDataViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileDataContent(uiState = uiState, onEvent = viewModel::onEvent)
}

@Composable
private fun ProfileDataContent(
    uiState: ProfileDataUiState?,
    onEvent: (ProfileDataEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val saveError = stringResource(R.string.save_error)
    val currentOnEvent by rememberUpdatedState(onEvent)
    LaunchedEffect(uiState?.showSaveError) {
        if (uiState?.showSaveError == true) {
            snackbarHostState.showSnackbar(saveError)
            currentOnEvent(ProfileDataEvent.DismissError)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            ToLTopAppBar(
                title = stringResource(R.string.profile_personal_data_title),
                onBack = { onEvent(ProfileDataEvent.Back) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PersonalSettings(
                name = uiState.name,
                onNameChange = { onEvent(ProfileDataEvent.NameChanged(it)) },
                sex = uiState.sex,
                onSexSelect = { onEvent(ProfileDataEvent.SexSelected(it)) },
                birthDate = uiState.birthDate,
                onBirthDateSelect = { onEvent(ProfileDataEvent.BirthDateSelected(it)) },
            )
            Text(
                text = stringResource(R.string.onboarding_health_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp),
            )
            HealthSettings(
                uiState = uiState.health,
                onEvent = { onEvent(ProfileDataEvent.Health(it)) },
            )
            Button(
                onClick = { onEvent(ProfileDataEvent.Save) },
                enabled = uiState.canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileDataContentPreview() {
    TimeOfLifeTheme {
        ProfileDataContent(
            uiState = ProfileDataUiState(
                name = "Chris",
                sex = Sex.MALE,
                birthDate = LocalDate(1990, 5, 17),
                health = HealthSettingsUiState(
                    countryCode = "DE",
                    height = "182",
                    weight = "78",
                    bmi = 23.5,
                    smoking = SmokingChoice.NEVER,
                ),
                canSave = true,
            ),
            onEvent = {},
        )
    }
}
