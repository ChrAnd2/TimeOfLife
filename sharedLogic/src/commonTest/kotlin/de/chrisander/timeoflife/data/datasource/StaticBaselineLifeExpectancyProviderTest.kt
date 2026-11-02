package de.chrisander.timeoflife.data.datasource

import de.chrisander.timeoflife.domain.model.profile.Sex
import kotlin.test.Test
import kotlin.test.assertEquals

class StaticBaselineLifeExpectancyProviderTest {

    @Test
    fun diverseUsesMeanOfMaleAndFemaleBaseline() {
        assertEquals(
            81.2,
            StaticBaselineLifeExpectancyProvider.atBirth("de", Sex.DIVERSE),
            absoluteTolerance = 1e-9
        )
    }

    @Test
    fun unknownCountryFallsBackToGlobalAverage() {
        assertEquals(70.8, StaticBaselineLifeExpectancyProvider.atBirth("XX", Sex.MALE))
    }
}
