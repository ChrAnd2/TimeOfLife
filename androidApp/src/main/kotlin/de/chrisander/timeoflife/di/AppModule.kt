package de.chrisander.timeoflife.di

import de.chrisander.timeoflife.data.datastore.createDataStore
import de.chrisander.timeoflife.ui.MainViewModel
import de.chrisander.timeoflife.ui.activity.ActivityDetailViewModel
import de.chrisander.timeoflife.ui.home.HomeViewModel
import de.chrisander.timeoflife.ui.navigation.ActivityDetailRoute
import de.chrisander.timeoflife.ui.navigation.Navigator
import de.chrisander.timeoflife.ui.onboarding.OnboardingViewModel
import de.chrisander.timeoflife.widget.GetLifeProgressUseCase
import de.chrisander.timeoflife.widget.data.datasource.WidgetSettingsDataSource
import de.chrisander.timeoflife.widget.data.datasource.WidgetSettingsLocalDataSource
import de.chrisander.timeoflife.widget.data.datasource.WidgetSettingsLocalDataSource.Companion.WIDGET_SETTINGS_FILE_NAME
import de.chrisander.timeoflife.widget.data.repository.WidgetSettingsRepository
import de.chrisander.timeoflife.widget.data.repository.WidgetSettingsRepositoryImpl
import de.chrisander.timeoflife.widget.settings.WidgetSettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.time.Clock

val appModule = module {
    single<Clock> { Clock.System }
    singleOf(::Navigator)

    viewModelOf(::MainViewModel)
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::HomeViewModel)
    viewModel { (route: ActivityDetailRoute) -> ActivityDetailViewModel(route, get(), get(), get(), get()) }

    factoryOf(::GetLifeProgressUseCase)
    single<WidgetSettingsDataSource> {
        WidgetSettingsLocalDataSource(
            createDataStore { androidContext().filesDir.resolve(WIDGET_SETTINGS_FILE_NAME).absolutePath },
        )
    }
    singleOf(::WidgetSettingsRepositoryImpl) bind WidgetSettingsRepository::class
    viewModel { (appWidgetId: Int) -> WidgetSettingsViewModel(appWidgetId, get()) }
}

val appModules = sharedModules + appModule
