package de.chrisander.timeoflife.di

import de.chrisander.timeoflife.data.datasource.BaselineLifeExpectancyProvider
import de.chrisander.timeoflife.data.datasource.StaticBaselineLifeExpectancyProvider
import de.chrisander.timeoflife.data.datasource.UserProfileDataSource
import de.chrisander.timeoflife.data.datasource.UserProfileLocalDataSource
import de.chrisander.timeoflife.data.repository.UserProfileRepository
import de.chrisander.timeoflife.data.repository.UserProfileRepositoryImpl
import de.chrisander.timeoflife.domain.usecase.GetLifeProgressUseCase
import de.chrisander.timeoflife.domain.usecase.GetLifeTimeBreakdownUseCase
import de.chrisander.timeoflife.domain.usecase.LifeTimeCalculator
import de.chrisander.timeoflife.domain.usecase.LifespanEstimator
import de.chrisander.timeoflife.domain.usecase.StudyBasedLifespanEstimator
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.time.Clock

internal expect val platformModule: Module

val dataModule = module {
    singleOf(::UserProfileLocalDataSource) bind UserProfileDataSource::class
    singleOf(::UserProfileRepositoryImpl) bind UserProfileRepository::class
    single<BaselineLifeExpectancyProvider> { StaticBaselineLifeExpectancyProvider }
}

val domainModule = module {
    single<Clock> { Clock.System }
    single<LifespanEstimator> { StudyBasedLifespanEstimator(get()) }
    singleOf(::LifeTimeCalculator)
    factoryOf(::GetLifeTimeBreakdownUseCase)
    factoryOf(::GetLifeProgressUseCase)
}

val sharedModules: List<Module> = listOf(platformModule, dataModule, domainModule)
