package de.chrisander.timeoflife.data.repository

import de.chrisander.timeoflife.data.datasource.UserProfileDataSource
import de.chrisander.timeoflife.domain.model.profile.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class UserProfileRepositoryImpl(
    private val localDataSource: UserProfileDataSource,
) : UserProfileRepository {

    override val userProfile: Flow<UserProfile?> = localDataSource.userProfile

    override val isOnboardingCompleted: Flow<Boolean> =
        localDataSource.userProfile.map { it != null }.distinctUntilChanged()

    override suspend fun saveUserProfile(profile: UserProfile) {
        localDataSource.saveUserProfile(profile.copy(name = profile.name.trim()))
    }
}
