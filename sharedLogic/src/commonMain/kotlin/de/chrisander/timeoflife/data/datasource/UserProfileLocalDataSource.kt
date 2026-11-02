package de.chrisander.timeoflife.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import de.chrisander.timeoflife.data.datasource.dto.UserProfileDto
import de.chrisander.timeoflife.data.datasource.mapper.toDomain
import de.chrisander.timeoflife.data.datasource.mapper.toDto
import de.chrisander.timeoflife.domain.model.profile.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class UserProfileLocalDataSource(
    private val dataStore: DataStore<Preferences>,
) : UserProfileDataSource {

    private val json = Json {
        ignoreUnknownKeys = true
        // ActivityDto.Default has a property named "type", the default discriminator.
        classDiscriminator = "kind"
    }

    /** A stored profile that can't be decoded or fails the domain model's validation is treated as missing. */
    override val userProfile: Flow<UserProfile?> = dataStore.data.map { preferences ->
        preferences[USER_PROFILE]?.let { stored ->
            runCatching { json.decodeFromString<UserProfileDto>(stored).toDomain() }.getOrNull()
        }
    }

    override suspend fun saveUserProfile(profile: UserProfile) {
        dataStore.edit { preferences ->
            preferences[USER_PROFILE] = json.encodeToString(profile.toDto())
        }
    }

    private companion object {
        val USER_PROFILE = stringPreferencesKey("user_profile")
    }
}
