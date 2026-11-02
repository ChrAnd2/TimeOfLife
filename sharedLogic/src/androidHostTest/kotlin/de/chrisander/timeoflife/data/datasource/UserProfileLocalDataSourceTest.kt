package de.chrisander.timeoflife.data.datasource

import de.chrisander.timeoflife.data.datastore.createDataStore
import de.chrisander.timeoflife.domain.model.profile.Sex
import de.chrisander.timeoflife.domain.model.profile.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserProfileLocalDataSourceTest {

    private val tempDir: File = Files.createTempDirectory("datastore-test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    private fun createDataSource() = UserProfileLocalDataSource(
        createDataStore { tempDir.resolve("test.preferences_pb").absolutePath },
    )

    @Test
    fun emitsNullWhenNothingStored() = runTest {
        assertNull(createDataSource().userProfile.first())
    }

    @Test
    fun savedProfileIsReadBack() = runTest {
        val dataSource = createDataSource()
        val profile = UserProfile("Chris", Sex.DIVERSE, LocalDate(1990, 5, 17))

        dataSource.saveUserProfile(profile)

        assertEquals(profile, dataSource.userProfile.first())
    }
}
