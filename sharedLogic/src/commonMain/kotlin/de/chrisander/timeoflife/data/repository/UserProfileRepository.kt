package de.chrisander.timeoflife.data.repository

import de.chrisander.timeoflife.domain.model.profile.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    val userProfile: Flow<UserProfile?>
    val isOnboardingCompleted: Flow<Boolean>

    suspend fun saveUserProfile(profile: UserProfile)
}
