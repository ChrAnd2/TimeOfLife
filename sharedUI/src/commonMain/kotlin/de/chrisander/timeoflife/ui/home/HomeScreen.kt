package de.chrisander.timeoflife.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.chrisander.timeoflife.data.datasource.StaticBaselineLifeExpectancyProvider
import de.chrisander.timeoflife.domain.model.activity.ActivityId
import de.chrisander.timeoflife.domain.model.profile.Sex
import de.chrisander.timeoflife.domain.model.profile.UserProfile
import de.chrisander.timeoflife.domain.usecase.GetLifeTimeBreakdownUseCase
import de.chrisander.timeoflife.domain.usecase.LifeTimeCalculator
import de.chrisander.timeoflife.domain.usecase.StudyBasedLifespanEstimator
import de.chrisander.timeoflife.resources.Res
import de.chrisander.timeoflife.resources.home_activities_caption
import de.chrisander.timeoflife.resources.home_activities_title
import de.chrisander.timeoflife.resources.home_activity_share
import de.chrisander.timeoflife.resources.home_bonus_body
import de.chrisander.timeoflife.resources.home_expected_label
import de.chrisander.timeoflife.resources.home_expected_until
import de.chrisander.timeoflife.resources.home_expected_years
import de.chrisander.timeoflife.resources.home_greeting_afternoon
import de.chrisander.timeoflife.resources.home_greeting_evening
import de.chrisander.timeoflife.resources.home_greeting_morning
import de.chrisander.timeoflife.resources.home_greeting_night
import de.chrisander.timeoflife.resources.home_profile
import de.chrisander.timeoflife.resources.ic_person
import de.chrisander.timeoflife.ui.components.Clock
import de.chrisander.timeoflife.ui.components.TimeOfLifeLogo
import de.chrisander.timeoflife.ui.formatter.color
import de.chrisander.timeoflife.ui.formatter.description
import de.chrisander.timeoflife.ui.formatter.formatDate
import de.chrisander.timeoflife.ui.formatter.formatPercent
import de.chrisander.timeoflife.ui.formatter.formatYears
import de.chrisander.timeoflife.ui.formatter.label
import de.chrisander.timeoflife.ui.formatter.yearsText
import de.chrisander.timeoflife.ui.home.mapper.toLifeOverview
import de.chrisander.timeoflife.ui.theme.TimeOfLifeTheme
import de.chrisander.timeoflife.ui.theme.card
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(
        uiState = uiState,
        onActivityClick = viewModel::onActivityClick,
        onProfileClick = viewModel::onProfileClick,
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState?,
    onActivityClick: (ActivityId?) -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { innerPadding ->
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TimeOfLifeLogo(height = 28.dp, modifier = Modifier.weight(1f))
                IconButton(onClick = onProfileClick) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_person),
                        contentDescription = stringResource(Res.string.home_profile),
                    )
                }
            }
            val greeting = when (uiState.timeOfDay) {
                TimeOfDay.MORNING -> Res.string.home_greeting_morning
                TimeOfDay.AFTERNOON -> Res.string.home_greeting_afternoon
                TimeOfDay.EVENING -> Res.string.home_greeting_evening
                TimeOfDay.NIGHT -> Res.string.home_greeting_night
            }
            Text(
                text = stringResource(greeting, uiState.name),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            Clock(
                progress = uiState.life.progress,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp)
                    .widthIn(max = 200.dp)
                    .fillMaxWidth(),
            )
            Lifespan(uiState.life, Modifier.fillMaxWidth())
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    text = stringResource(Res.string.home_activities_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = stringResource(Res.string.home_activities_caption),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.life.activities.forEach {
                    ActivityRow(
                        item = it,
                        onClick = { onActivityClick(it.activityId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun Lifespan(
    life: LifeOverview,
    modifier: Modifier = Modifier,
) {
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(Res.string.home_expected_label),
            style = MaterialTheme.typography.titleMedium,
            color = color,
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = formatYears(life.lifespanYears),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(Res.string.home_expected_years),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 10.dp),
            )
        }
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Text(
                text = stringResource(
                    Res.string.home_expected_until,
                    formatDate(life.expectedUntil)
                ),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        if (life.yearsBeyondLifespan > 0) {
            Text(
                text = stringResource(
                    Res.string.home_bonus_body,
                    formatYears(life.yearsBeyondLifespan)
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = color,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun ActivityRow(
    item: ActivityOverview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.card,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(item.segment.color)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp),
            ) {
                Text(text = item.segment.label(), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = item.segment.description(),
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                )
            }
            val tabularFigures = "tnum"
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = yearsText(item.yearsSpent),
                    style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = tabularFigures),
                )
                Text(
                    text = stringResource(
                        Res.string.home_activity_share,
                        formatPercent(item.segment.fraction)
                    ),
                    style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = tabularFigures),
                    color = color,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(
    showBackground = true,
    uiMode = AndroidUiModes.UI_MODE_NIGHT_YES,
)
@Composable
private fun HomeContentPreview() {
    TimeOfLifeTheme {
        val getLifeTimeBreakdown = GetLifeTimeBreakdownUseCase(
            StudyBasedLifespanEstimator(StaticBaselineLifeExpectancyProvider),
            LifeTimeCalculator(),
        )
        val profile = UserProfile("Chris", Sex.MALE, LocalDate(1990, 5, 17))
        val life = getLifeTimeBreakdown(profile, LocalDate(2026, 11, 4)).toLifeOverview()
        HomeContent(
            uiState = HomeUiState(name = "Chris", timeOfDay = TimeOfDay.EVENING, life = life),
            onActivityClick = {},
            onProfileClick = {},
        )
    }
}
