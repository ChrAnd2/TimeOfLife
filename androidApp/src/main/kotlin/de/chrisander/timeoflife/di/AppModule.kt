package de.chrisander.timeoflife.di

import de.chrisander.timeoflife.ui.MainViewModel
import de.chrisander.timeoflife.ui.home.HomeViewModel
import de.chrisander.timeoflife.ui.navigation.Navigator
import de.chrisander.timeoflife.ui.onboarding.OnboardingViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import kotlin.time.Clock

val appModule = module {
    single<Clock> { Clock.System }
    singleOf(::Navigator)

    viewModelOf(::MainViewModel)
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::HomeViewModel)
}

val appModules = sharedModules + appModule
