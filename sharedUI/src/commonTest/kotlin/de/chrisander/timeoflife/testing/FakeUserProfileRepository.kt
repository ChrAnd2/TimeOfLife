package de.chrisander.timeoflife.testing

import androidx.datastore.core.IOException
import de.chrisander.timeoflife.data.repository.UserProfileRepository
import de.chrisander.timeoflife.domain.model.profile.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeUserProfileRepository(initial: UserProfile? = null) : UserProfileRepository {
    val profile = MutableStateFlow(initial)
    var failOnSave = false

    override val userProfile: Flow<UserProfile?> = profile
    override val isOnboardingCompleted: Flow<Boolean> = profile.map { it != null }

    override suspend fun saveUserProfile(profile: UserProfile) {
        if (failOnSave) throw IOException("disk full")
        this.profile.value = profile
    }
}
