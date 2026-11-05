package de.chrisander.timeoflife.ui.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.res.painterResource
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
import de.chrisander.timeoflife.ui.home.color
import de.chrisander.timeoflife.ui.home.label
import de.chrisander.timeoflife.ui.home.yearsText
import de.chrisander.timeoflife.ui.navigation.ActivityDetailRoute
import de.chrisander.timeoflife.ui.theme.TimeOfLifeTheme
import de.chrisander.timeoflife.ui.theme.card
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ActivityDetailScreen(
    route: ActivityDetailRoute,
    viewModel: ActivityDetailViewModel = koinViewModel { parametersOf(route) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ActivityDetailContent(uiState = uiState, onBack = viewModel::onBack)
}

@Composable
private fun ActivityDetailContent(
    uiState: ActivityDetailUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(start = 8.dp, end = 20.dp, bottom = 24.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.activity_detail_back),
                )
            }
            val detail = uiState.detail
            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (detail != null) {
                Column(
                    modifier = Modifier.padding(start = 12.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Header(detail)
                    SpentSoFar(detail, Modifier.fillMaxWidth().padding(vertical = 16.dp))
                    AheadAndWholeLife(detail)
                    Schedule(detail, Modifier.padding(top = 16.dp))
                }
            }
        }
    }
}

@Composable
private fun Header(detail: ActivityDetail) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(detail.segment.color),
            )
            Text(
                text = detail.segment.label(),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
        Text(
            text = detail.segment.description(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** The headline number, built like the life span on the home screen, with a pill in the activity's color. */
@Composable
private fun SpentSoFar(
    detail: ActivityDetail,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.activity_detail_spent_so_far),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = yearsText(detail.yearsSpent),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Surface(
            shape = CircleShape,
            color = detail.segment.color.copy(alpha = PILL_ALPHA),
            modifier = Modifier.padding(top = 4.dp),
        ) {
            Text(
                text = stringResource(R.string.activity_detail_share_of_lived, formatPercent(detail.shareOfLived)),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun AheadAndWholeLife(detail: ActivityDetail) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NumberTile(
            title = stringResource(R.string.activity_detail_ahead),
            years = detail.yearsAhead,
            share = stringResource(
                R.string.activity_detail_share_of_remaining,
                formatPercent(detail.shareOfRemaining),
            ),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
        NumberTile(
            title = stringResource(R.string.activity_detail_whole_life),
            years = detail.yearsTotal,
            share = stringResource(
                R.string.activity_detail_share_of_lifespan,
                formatPercent(detail.shareOfLifespan),
            ),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
    }
}

@Composable
private fun NumberTile(
    title: String,
    years: Double,
    share: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.card,
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = yearsText(years),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = share,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private const val PILL_ALPHA = 0.25f

@Preview(showBackground = true, heightDp = 1000)
@Preview(
    showBackground = true,
    heightDp = 1000,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ActivityDetailContentPreview() {
    TimeOfLifeTheme {
        val getLifeTimeBreakdown = GetLifeTimeBreakdownUseCase(
            StudyBasedLifespanEstimator(StaticBaselineLifeExpectancyProvider),
            LifeTimeCalculator(),
        )
        val profile = UserProfile("Chris", Sex.MALE, LocalDate(1990, 5, 17))
        val detail = getLifeTimeBreakdown(profile, LocalDate(2026, 11, 4))
            .activityDetail(ActivityId("default:WORK"))
        ActivityDetailContent(
            uiState = ActivityDetailUiState(isLoading = false, detail = detail),
            onBack = {},
        )
    }
}
