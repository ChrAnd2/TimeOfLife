package de.chrisander.timeoflife.data.datasource

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import de.chrisander.timeoflife.data.datastore.createDataStore
import de.chrisander.timeoflife.domain.model.activity.Activity.CustomActivity
import de.chrisander.timeoflife.domain.model.activity.Activity.DefaultActivity
import de.chrisander.timeoflife.domain.model.activity.ActivityId
import de.chrisander.timeoflife.domain.model.activity.ActivitySchedule
import de.chrisander.timeoflife.domain.model.activity.DefaultActivityType
import de.chrisander.timeoflife.domain.model.activity.Recurrence
import de.chrisander.timeoflife.domain.model.lifespan.factor.AlcoholConsumption
import de.chrisander.timeoflife.domain.model.lifespan.factor.BodyMeasurements
import de.chrisander.timeoflife.domain.model.lifespan.factor.HealthCondition
import de.chrisander.timeoflife.domain.model.lifespan.factor.HealthConditions
import de.chrisander.timeoflife.domain.model.lifespan.factor.PhysicalActivityLevel
import de.chrisander.timeoflife.domain.model.lifespan.factor.SmokingStatus
import de.chrisander.timeoflife.domain.model.profile.LifespanFactors
import de.chrisander.timeoflife.domain.model.profile.LifespanSetting
import de.chrisander.timeoflife.domain.model.profile.Sex
import de.chrisander.timeoflife.domain.model.profile.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class UserProfileLocalDataSourceTest {

    private val tempDir: File = Files.createTempDirectory("datastore-test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    private val dataStore = createDataStore { tempDir.resolve("test.preferences_pb").absolutePath }

    private fun createDataSource() = UserProfileLocalDataSource(dataStore)

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

    @Test
    fun fullProfileIsReadBack() = runTest {
        val dataSource = createDataSource()
        val profile = UserProfile(
            name = "Chris",
            sex = Sex.MALE,
            birthDate = LocalDate(1990, 5, 17),
            lifespanSetting = LifespanSetting.Manual(91.5),
            lifespanFactors = LifespanFactors(
                countryCode = "DE",
                body = BodyMeasurements(heightCm = 180.0, weightKg = 75.5),
                smoking = SmokingStatus.Former(quitAge = 32),
                alcohol = AlcoholConsumption.MODERATE,
                physicalActivity = PhysicalActivityLevel.HIGH,
                conditions = HealthConditions(
                    setOf(
                        HealthCondition.DIABETES,
                        HealthCondition.STROKE
                    )
                ),
            ),
            activities = listOf(
                DefaultActivity(DefaultActivityType.SLEEP),
                DefaultActivity(
                    DefaultActivityType.WORK,
                    ActivitySchedule(Recurrence(30.hours, DateTimeUnit.WEEK), 20, 60)
                ),
                CustomActivity(
                    id = ActivityId("guitar"),
                    name = "Guitar",
                    schedule = ActivitySchedule(
                        Recurrence(90.minutes, DateTimeUnit.MONTH * 3),
                        fromAge = 12
                    ),
                ),
            ),
        )

        dataSource.saveUserProfile(profile)

        assertEquals(profile, dataSource.userProfile.first())
    }

    @Test
    fun profileFailingValidationIsTreatedAsMissing() = runTest {
        dataStore.edit {
            it[stringPreferencesKey("user_profile")] =
                """{"name":"Chris","sex":"MALE","birthDate":"1990-05-17","lifespanSetting":{"kind":"estimated"},""" +
                        """"lifespanFactors":{"heightCm":-1,"weightKg":70},"activities":[]}"""
        }

        assertNull(createDataSource().userProfile.first())
    }

    @Test
    fun undecodableProfileIsTreatedAsMissing() = runTest {
        dataStore.edit { it[stringPreferencesKey("user_profile")] = "not json" }

        assertNull(createDataSource().userProfile.first())
    }
}
