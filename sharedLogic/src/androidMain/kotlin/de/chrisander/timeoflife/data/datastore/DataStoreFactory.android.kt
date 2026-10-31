package de.chrisander.timeoflife.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

fun createUserPreferencesDataStore(context: Context): DataStore<Preferences> =
    createDataStore(
        producePath = { context.filesDir.resolve(USER_PREFERENCES_FILE_NAME).absolutePath },
    )
