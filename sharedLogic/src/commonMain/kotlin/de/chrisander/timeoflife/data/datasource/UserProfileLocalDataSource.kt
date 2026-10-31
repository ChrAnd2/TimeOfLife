package de.chrisander.timeoflife.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import de.chrisander.timeoflife.domain.model.Sex
import de.chrisander.timeoflife.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class UserProfileLocalDataSource(
    private val dataStore: DataStore<Preferences>,
) : UserProfileDataSource {

    override val userProfile: Flow<UserProfile?> = dataStore.data.map { preferences ->
        val name = preferences[Keys.NAME] ?: return@map null
        val sex = preferences[Keys.SEX]?.let { stored -> Sex.entries.find { it.name == stored } }
            ?: return@map null
        val birthDate = preferences[Keys.BIRTH_DATE]?.let(LocalDate::parse) ?: return@map null
        UserProfile(name = name, sex = sex, birthDate = birthDate)
    }

    override suspend fun saveUserProfile(profile: UserProfile) {
        dataStore.edit { preferences ->
            preferences[Keys.NAME] = profile.name
            preferences[Keys.SEX] = profile.sex.name
            // ISO-8601 (yyyy-MM-dd)
            preferences[Keys.BIRTH_DATE] = profile.birthDate.toString()
        }
    }

    private object Keys {
        val NAME = stringPreferencesKey("user_name")
        val SEX = stringPreferencesKey("user_sex")
        val BIRTH_DATE = stringPreferencesKey("user_birth_date")
    }
}
