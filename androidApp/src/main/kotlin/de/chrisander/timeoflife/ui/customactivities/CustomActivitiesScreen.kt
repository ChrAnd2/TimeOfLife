package de.chrisander.timeoflife.ui.customactivities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.chrisander.timeoflife.R
import de.chrisander.timeoflife.domain.model.activity.Activity
import de.chrisander.timeoflife.domain.model.activity.ActivityId
import de.chrisander.timeoflife.domain.model.activity.ActivitySchedule
import de.chrisander.timeoflife.domain.model.activity.Recurrence
import de.chrisander.timeoflife.ui.activity.formatter.ageRangeText
import de.chrisander.timeoflife.ui.components.ToLSettingsItem
import de.chrisander.timeoflife.ui.components.ToLTopAppBar
import de.chrisander.timeoflife.ui.formatter.text
import de.chrisander.timeoflife.ui.theme.TimeOfLifeTheme
import kotlinx.datetime.DateTimeUnit
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.hours

@Composable
fun CustomActivitiesScreen(
    viewModel: CustomActivitiesViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CustomActivitiesContent(
        uiState = uiState,
        onActivityClick = viewModel::onActivityClick,
        onAddClick = viewModel::onAddClick,
        onBack = viewModel::onBack,
    )
}

@Composable
private fun CustomActivitiesContent(
    uiState: CustomActivitiesUiState?,
    onActivityClick: (ActivityId) -> Unit,
    onAddClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ToLTopAppBar(
                title = stringResource(R.string.profile_custom_activities_title),
                onBack = onBack
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                icon = { Icon(painterResource(R.drawable.ic_add), contentDescription = null) },
                text = { Text(stringResource(R.string.custom_activities_add)) },
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
                // Leaves room so the floating button never covers the last entry.
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.custom_activities_caption),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            if (uiState.activities.isEmpty()) {
                Text(
                    text = stringResource(R.string.custom_activities_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 24.dp),
                )
            }
            uiState.activities.forEach { activity ->
                ToLSettingsItem(
                    title = activity.name,
                    subtitle = "${activity.schedule.recurrence.text()} · ${activity.schedule.ageRangeText()}",
                    onClick = { onActivityClick(activity.id) },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomActivitiesContentPreview() {
    TimeOfLifeTheme {
        CustomActivitiesContent(
            uiState = CustomActivitiesUiState(
                listOf(
                    Activity.CustomActivity(
                        id = ActivityId("custom:1"),
                        name = "Football",
                        schedule = ActivitySchedule(
                            Recurrence(3.hours, DateTimeUnit.WEEK),
                            fromAge = 8
                        ),
                    ),
                    Activity.CustomActivity(
                        id = ActivityId("custom:2"),
                        name = "Piano",
                        schedule = ActivitySchedule(Recurrence(1.hours, DateTimeUnit.DAY), 10, 18),
                    ),
                ),
            ),
            onActivityClick = {},
            onAddClick = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomActivitiesEmptyPreview() {
    TimeOfLifeTheme {
        CustomActivitiesContent(
            uiState = CustomActivitiesUiState(emptyList()),
            onActivityClick = {},
            onAddClick = {},
            onBack = {},
        )
    }
}
