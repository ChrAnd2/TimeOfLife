package de.chrisander.timeoflife.di

import de.chrisander.timeoflife.data.datasource.UserProfileDataSource
import de.chrisander.timeoflife.data.datasource.UserProfileLocalDataSource
import de.chrisander.timeoflife.data.repository.UserProfileRepository
import de.chrisander.timeoflife.data.repository.UserProfileRepositoryImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal expect val platformModule: Module

val dataModule = module {
    singleOf(::UserProfileLocalDataSource) bind UserProfileDataSource::class
    singleOf(::UserProfileRepositoryImpl) bind UserProfileRepository::class
}

val sharedModules: List<Module> = listOf(platformModule, dataModule)
