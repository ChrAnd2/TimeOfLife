package de.chrisander.timeoflife.di

import android.content.Context
import de.chrisander.timeoflife.ui.navigation.ActivityDetailRoute
import de.chrisander.timeoflife.ui.navigation.CustomActivityEditorRoute
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify
import kotlin.test.Test

class KoinModulesTest {

    /** Fails if any definition has a constructor dependency that isn't provided by the modules. */
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun allDependenciesCanBeResolved() {
        module { includes(appModules) }.verify(
            // Injected at runtime with parametersOf.
            extraTypes = listOf(
                Context::class,
                ActivityDetailRoute::class,
                CustomActivityEditorRoute::class
            ),
        )
    }
}
