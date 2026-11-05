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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.chrisander.timeoflife.R
import de.chrisander.timeoflife.data.datasource.StaticBaselineLifeExpectancyProvider
import de.chrisander.timeoflife.domain.model.activity.ActivityId
import de.chrisander.timeoflife.domain.model.profile.Sex
import de.chrisander.timeoflife.domain.model.profile.UserProfile
import de.chrisander.timeoflife.domain.usecase.GetLifeTimeBreakdownUseCase
import de.chrisander.timeoflife.domain.usecase.LifeTimeCalculator
import de.chrisander.timeoflife.domain.usecase.StudyBasedLifespanEstimator
import de.chrisander.timeoflife.ui.activity.description
import de.chrisander.timeoflife.ui.activity.formatPercent
import de.chrisander.timeoflife.ui.components.TimeOfLifeLogo
import de.chrisander.timeoflife.ui.theme.TimeOfLifeTheme
import de.chrisander.timeoflife.ui.theme.card
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(uiState = uiState, onActivityClick = viewModel::onActivityClick)
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onActivityClick: (ActivityId?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { innerPadding ->
        val life = uiState.life
        if (life == null) {
            Box(
                Modifier
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
            TimeOfLifeLogo(height = 28.dp)
            Text(
                text = stringResource(uiState.timeOfDay.greeting, uiState.name),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            LifeClock(
                progress = life.progress,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp)
                    .widthIn(max = 200.dp)
                    .fillMaxWidth(),
            )
            Lifespan(life, Modifier.fillMaxWidth())
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    text = stringResource(R.string.home_activities_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = stringResource(R.string.home_activities_caption),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                life.activities.forEach { ActivityRow(it, onClick = { onActivityClick(it.activityId) }) }
            }
        }
    }
}

private val TimeOfDay.greeting: Int
    get() = when (this) {
        TimeOfDay.MORNING -> R.string.home_greeting_morning
        TimeOfDay.AFTERNOON -> R.string.home_greeting_afternoon
        TimeOfDay.EVENING -> R.string.home_greeting_evening
        TimeOfDay.NIGHT -> R.string.home_greeting_night
    }

/** The expected life span as the screen's headline number. */
@Composable
private fun Lifespan(
    life: LifeOverview,
    modifier: Modifier = Modifier,
) {
    val dim = MaterialTheme.colorScheme.onSurfaceVariant
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.home_expected_label),
            style = MaterialTheme.typography.titleMedium,
            color = dim,
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
                text = stringResource(R.string.home_expected_years),
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
                text = stringResource(R.string.home_expected_until, formatDate(life.expectedUntil)),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        if (life.yearsBeyondLifespan > 0) {
            Text(
                text = stringResource(R.string.home_bonus_body, formatYears(life.yearsBeyondLifespan)),
                style = MaterialTheme.typography.bodyMedium,
                color = dim,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

/** One tile per activity: name and schedule, and how much of the life so far it took. */
@Composable
private fun ActivityRow(
    item: ActivityOverview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dim = MaterialTheme.colorScheme.onSurfaceVariant
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
                Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(item.segment.color),
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
                    color = dim,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = yearsText(item.yearsSpent),
                    style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = TABULAR_FIGURES),
                )
                Text(
                    text = stringResource(R.string.home_activity_share, formatPercent(item.segment.fraction)),
                    style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = TABULAR_FIGURES),
                    color = dim,
                )
            }
        }
    }
}

private const val TABULAR_FIGURES = "tnum"

@Preview(showBackground = true, heightDp = 1400)
@Preview(
    showBackground = true,
    heightDp = 1400,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
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
        )
    }
}
