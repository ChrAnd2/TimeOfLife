package de.chrisander.timeoflife.di

import de.chrisander.timeoflife.data.datastore.createUserPreferencesDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformModule: Module = module {
    single { createUserPreferencesDataStore(androidContext()) }
}
