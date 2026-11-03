package de.chrisander.timeoflife

import android.app.Application
import de.chrisander.timeoflife.data.repository.UserProfileRepository
import de.chrisander.timeoflife.di.appModules
import de.chrisander.timeoflife.widget.Widgets
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class TimeOfLifeApplication : Application() {

    private val appScope = MainScope()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@TimeOfLifeApplication)
            modules(appModules)
        }
        refreshWidgetsOnProfileChange()
    }

    private fun refreshWidgetsOnProfileChange() {
        appScope.launch {
            get<UserProfileRepository>().userProfile
                .distinctUntilChanged()
                // The first value is what the widgets already show.
                .drop(1)
                .collect { Widgets.requestUpdate(this@TimeOfLifeApplication) }
        }
    }
}
