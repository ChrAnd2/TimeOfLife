package de.chrisander.timeoflife.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import de.chrisander.timeoflife.domain.model.profile.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserProfileLocalDataSource(
    private val dataStore: DataStore<Preferences>,
) : UserProfileDataSource {

    /** A stored profile that can't be decoded or fails the domain model's validation is treated as missing. */
    override val userProfile: Flow<UserProfile?> = dataStore.data.map { preferences ->
        preferences[USER_PROFILE]?.let(UserProfileJson::decode)
    }

    override suspend fun saveUserProfile(profile: UserProfile) {
        dataStore.edit { preferences ->
            preferences[USER_PROFILE] = UserProfileJson.encode(profile)
        }
    }

    private companion object {
        val USER_PROFILE = stringPreferencesKey("user_profile")
    }
}
