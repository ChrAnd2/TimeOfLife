package de.chrisander.timeoflife.di

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
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val uiModule = module {
    singleOf(::Navigator)
    factoryOf(::HealthSettingsHandler)

    viewModelOf(::MainViewModel)
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::HomeViewModel)
    viewModel { (route: ActivityDetailRoute) ->
        ActivityDetailViewModel(
            route,
            get(),
            get(),
            get(),
            get()
        )
    }
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
}

val sharedUiModules: List<Module> = sharedModules + uiModule
