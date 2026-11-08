package de.chrisander.timeoflife.ui.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey

/**
 * Owns the app's back stack so ViewModels can navigate without the UI passing callbacks around.
 * Registered as a Koin singleton; [AppNavigation] renders whatever is on [backStack].
 */
class Navigator {
    val backStack: SnapshotStateList<NavKey> = mutableStateListOf()

    fun navigateTo(route: NavKey) {
        backStack.add(route)
    }

    /** Makes [route] the only entry, so back does not return to anything shown before it. */
    fun replaceAll(route: NavKey) {
        backStack.add(route)
        backStack.removeRange(0, backStack.lastIndex)
    }

    fun goBack() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }
}
