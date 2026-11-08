package de.chrisander.timeoflife.di

import de.chrisander.timeoflife.data.datastore.createDataStore
import de.chrisander.timeoflife.widget.data.datasource.WidgetSettingsDataSource
import de.chrisander.timeoflife.widget.data.datasource.WidgetSettingsLocalDataSource
import de.chrisander.timeoflife.widget.data.datasource.WidgetSettingsLocalDataSource.Companion.WIDGET_SETTINGS_FILE_NAME
import de.chrisander.timeoflife.widget.data.repository.WidgetSettingsRepository
import de.chrisander.timeoflife.widget.data.repository.WidgetSettingsRepositoryImpl
import de.chrisander.timeoflife.widget.settings.WidgetSettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single<WidgetSettingsDataSource> {
        WidgetSettingsLocalDataSource(
            createDataStore { androidContext().filesDir.resolve(WIDGET_SETTINGS_FILE_NAME).absolutePath },
        )
    }
    singleOf(::WidgetSettingsRepositoryImpl) bind WidgetSettingsRepository::class
    viewModel { (appWidgetId: Int) -> WidgetSettingsViewModel(appWidgetId, get()) }
}

val appModules = sharedUiModules + appModule
