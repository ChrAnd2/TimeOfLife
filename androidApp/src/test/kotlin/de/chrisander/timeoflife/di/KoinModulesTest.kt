package de.chrisander.timeoflife.di

import android.content.Context
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
            extraTypes = listOf(Context::class),
        )
    }
}
