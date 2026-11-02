package de.chrisander.timeoflife.data.datasource

import de.chrisander.timeoflife.domain.model.profile.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileDataSource {
    /** Emits the stored profile, or `null` while none has been saved. */
    val userProfile: Flow<UserProfile?>

    suspend fun saveUserProfile(profile: UserProfile)
}
