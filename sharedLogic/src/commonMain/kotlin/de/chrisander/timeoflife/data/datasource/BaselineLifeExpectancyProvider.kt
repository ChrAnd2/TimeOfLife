package de.chrisander.timeoflife.data.datasource

import de.chrisander.timeoflife.domain.model.profile.Sex

/** Average life expectancy at birth for a population, before any personal factors are applied. */
fun interface BaselineLifeExpectancyProvider {
    fun atBirth(countryCode: String?, sex: Sex): Double
}

