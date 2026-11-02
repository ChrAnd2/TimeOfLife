package de.chrisander.timeoflife.data.datasource

import de.chrisander.timeoflife.domain.model.profile.Sex
import kotlin.test.Test
import kotlin.test.assertEquals

class StaticBaselineLifeExpectancyProviderTest {

    @Test
    fun diverseUsesMeanOfMaleAndFemaleBaseline() {
        val male = StaticBaselineLifeExpectancyProvider.atBirth("de", Sex.MALE)
        val female = StaticBaselineLifeExpectancyProvider.atBirth("de", Sex.FEMALE)

        assertEquals(
            (male + female) / 2,
            StaticBaselineLifeExpectancyProvider.atBirth("de", Sex.DIVERSE)
        )
    }

    @Test
    fun unknownCountryFallsBackToGlobalAverage() {
        assertEquals(70.8, StaticBaselineLifeExpectancyProvider.atBirth("XX", Sex.MALE))
    }
}
