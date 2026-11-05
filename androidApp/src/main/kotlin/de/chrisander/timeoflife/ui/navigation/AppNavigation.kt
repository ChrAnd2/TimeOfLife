package de.chrisander.timeoflife.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import de.chrisander.timeoflife.ui.activity.ActivityDetailScreen
import de.chrisander.timeoflife.ui.home.HomeScreen
import de.chrisander.timeoflife.ui.onboarding.OnboardingScreen
import org.koin.compose.koinInject

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navigator: Navigator = koinInject(),
) {
    NavDisplay(
        backStack = navigator.backStack,
        onBack = navigator::goBack,
        modifier = modifier,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<OnboardingRoute> { OnboardingScreen() }
            entry<HomeRoute> { HomeScreen() }
            entry<ActivityDetailRoute> { ActivityDetailScreen(it) }
        },
    )
}
