package de.chrisander.timeoflife

import android.app.Application
import de.chrisander.timeoflife.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class TimeOfLifeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@TimeOfLifeApplication)
            modules(appModules)
        }
    }
}
