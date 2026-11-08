package de.chrisander.timeoflife

import androidx.compose.ui.window.ComposeUIViewController
import de.chrisander.timeoflife.data.repository.UserProfileRepository
import de.chrisander.timeoflife.di.sharedUiModules
import de.chrisander.timeoflife.widget.WidgetProfileStore
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform
import platform.UIKit.UIViewController

@Suppress("FunctionNaming")
fun MainViewController(): UIViewController = ComposeUIViewController { App() }

fun initKoin() {
    startKoin { modules(sharedUiModules) }
}

fun observeProfileForWidgets(onChange: () -> Unit) {
    MainScope().launch {
        KoinPlatform.getKoin().get<UserProfileRepository>().userProfile
            .distinctUntilChanged()
            .collect { profile ->
                WidgetProfileStore.save(profile)
                onChange()
            }
    }
}
