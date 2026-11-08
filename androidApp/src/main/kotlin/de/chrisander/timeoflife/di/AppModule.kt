package de.chrisander.timeoflife.di

import de.chrisander.timeoflife.data.datastore.createDataStore
import de.chrisander.timeoflife.ui.MainViewModel
import de.chrisander.timeoflife.ui.activity.ActivityDetailViewModel
import de.chrisander.timeoflife.ui.customactivities.CustomActivitiesViewModel
import de.chrisander.timeoflife.ui.customactivityeditor.CustomActivityEditorViewModel
import de.chrisander.timeoflife.ui.healthsettings.HealthSettingsHandler
import de.chrisander.timeoflife.ui.home.HomeViewModel
import de.chrisander.timeoflife.ui.navigation.ActivityDetailRoute
import de.chrisander.timeoflife.ui.navigation.CustomActivityEditorRoute
import de.chrisander.timeoflife.ui.navigation.Navigator
import de.chrisander.timeoflife.ui.onboarding.OnboardingViewModel
import de.chrisander.timeoflife.ui.profile.ProfileViewModel
import de.chrisander.timeoflife.ui.profiledata.ProfileDataViewModel
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
    viewModelOf(::ProfileViewModel)
    viewModelOf(::ProfileDataViewModel)
    viewModelOf(::CustomActivitiesViewModel)
    viewModel { (route: CustomActivityEditorRoute) ->
        CustomActivityEditorViewModel(
            route,
            get(),
            get()
        )
    }
    factoryOf(::HealthSettingsHandler)

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
