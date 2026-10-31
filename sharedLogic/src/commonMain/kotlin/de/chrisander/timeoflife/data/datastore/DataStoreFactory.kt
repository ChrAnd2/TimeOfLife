package de.chrisander.timeoflife.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

internal const val USER_PREFERENCES_FILE_NAME = "user_preferences.preferences_pb"

/**
 * Creates a Preferences [DataStore] at the path returned by [producePath].
 *
 * Only one instance per file may be active at a time, so callers must keep the result as a singleton.
 */
fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() },
    )
