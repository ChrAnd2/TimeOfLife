package de.chrisander.timeoflife.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.chrisander.timeoflife.resources.Res
import de.chrisander.timeoflife.resources.ic_person
import de.chrisander.timeoflife.resources.ic_schedule
import de.chrisander.timeoflife.resources.profile_age_and_birth_date
import de.chrisander.timeoflife.resources.profile_custom_activities_subtitle
import de.chrisander.timeoflife.resources.profile_custom_activities_subtitle_empty
import de.chrisander.timeoflife.resources.profile_custom_activities_title
import de.chrisander.timeoflife.resources.profile_personal_data_subtitle
import de.chrisander.timeoflife.resources.profile_personal_data_title
import de.chrisander.timeoflife.resources.profile_settings_title
import de.chrisander.timeoflife.resources.profile_title
import de.chrisander.timeoflife.ui.components.ToLSettingsItem
import de.chrisander.timeoflife.ui.components.ToLTopAppBar
import de.chrisander.timeoflife.ui.formatter.formatDate
import de.chrisander.timeoflife.ui.profile.model.ProfileSetting
import de.chrisander.timeoflife.ui.theme.TimeOfLifeTheme
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileContent(
        uiState = uiState,
        onSettingClick = viewModel::onSettingClick,
        onBack = viewModel::onBack,
    )
}

@Composable
private fun ProfileContent(
    uiState: ProfileUiState?,
    onSettingClick: (ProfileSetting) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ToLTopAppBar(
                title = stringResource(Res.string.profile_title),
                onBack = onBack
            )
        },
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ProfileHeader(uiState, Modifier.padding(bottom = 24.dp))
            Text(
                text = stringResource(Res.string.profile_settings_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            ProfileSetting.entries.forEach { setting ->
                when (setting) {
                    ProfileSetting.PERSONAL_DATA -> ToLSettingsItem(
                        title = stringResource(Res.string.profile_personal_data_title),
                        subtitle = stringResource(Res.string.profile_personal_data_subtitle),
                        icon = Res.drawable.ic_person,
                        onClick = { onSettingClick(setting) },
                    )

                    ProfileSetting.CUSTOM_ACTIVITIES -> ToLSettingsItem(
                        title = stringResource(Res.string.profile_custom_activities_title),
                        subtitle = if (uiState.customActivityCount == 0) {
                            stringResource(Res.string.profile_custom_activities_subtitle_empty)
                        } else {
                            pluralStringResource(
                                Res.plurals.profile_custom_activities_subtitle,
                                uiState.customActivityCount,
                                uiState.customActivityCount,
                            )
                        },
                        icon = Res.drawable.ic_schedule,
                        onClick = { onSettingClick(setting) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    uiState: ProfileUiState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(64.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = uiState.name.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
        }
        Column {
            Text(text = uiState.name, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = stringResource(
                    Res.string.profile_age_and_birth_date,
                    uiState.age,
                    formatDate(uiState.birthDate),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(
    showBackground = true,
    uiMode = AndroidUiModes.UI_MODE_NIGHT_YES,
)
@Composable
private fun ProfileContentPreview() {
    TimeOfLifeTheme {
        ProfileContent(
            uiState = ProfileUiState(
                name = "Chris",
                birthDate = LocalDate(1990, 5, 17),
                age = 36,
                customActivityCount = 2,
            ),
            onSettingClick = {},
            onBack = {},
        )
    }
}
