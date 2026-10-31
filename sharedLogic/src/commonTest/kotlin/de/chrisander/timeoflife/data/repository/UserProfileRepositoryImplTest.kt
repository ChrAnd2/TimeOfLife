package de.chrisander.timeoflife.data.repository

import de.chrisander.timeoflife.data.datasource.UserProfileDataSource
import de.chrisander.timeoflife.domain.model.Sex
import de.chrisander.timeoflife.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserProfileRepositoryImplTest {

    private val dataSource = FakeUserProfileDataSource()
    private val repository = UserProfileRepositoryImpl(dataSource)

    @Test
    fun onboardingIsNotCompletedWithoutProfile() = runTest {
        assertFalse(repository.isOnboardingCompleted.first())
    }

    @Test
    fun savingProfileCompletesOnboarding() = runTest {
        repository.saveUserProfile(UserProfile("Chris", Sex.MALE, LocalDate(1990, 5, 17)))

        assertTrue(repository.isOnboardingCompleted.first())
    }

    @Test
    fun savingProfileTrimsName() = runTest {
        repository.saveUserProfile(UserProfile("  Chris ", Sex.MALE, LocalDate(1990, 5, 17)))

        assertEquals("Chris", repository.userProfile.first()?.name)
    }

    private class FakeUserProfileDataSource : UserProfileDataSource {
        private val state = MutableStateFlow<UserProfile?>(null)

        override val userProfile: Flow<UserProfile?> = state

        override suspend fun saveUserProfile(profile: UserProfile) {
            state.value = profile
        }
    }
}
