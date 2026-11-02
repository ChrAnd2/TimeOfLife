package de.chrisander.timeoflife.data.datasource

import de.chrisander.timeoflife.domain.model.profile.Sex

/**
 * A small built-in table of life expectancy at birth. Unknown countries fall back to the global average.
 * For [Sex.DIVERSE] there is no separate statistic, so the mean of the male and female values is used.
 *
 * Sources (see `docs/lifespan-estimation.md`):
 * - Germany: Destatis, Sterbetafel 2024 – https://www.destatis.de/DE/Themen/Gesellschaft-Umwelt/Bevoelkerung/Sterbefaelle-Lebenserwartung/_inhalt.html
 * - Global: WHO Global Health Estimates, 2019 – https://www.who.int/data/gho/data/themes/mortality-and-global-health-estimates/ghe-life-expectancy-and-healthy-life-expectancy
 *
 * TODO: Add more countries, ideally from a full dataset such as UN World Population Prospects.
 */
object StaticBaselineLifeExpectancyProvider : BaselineLifeExpectancyProvider {

    private data class Values(val male: Double, val female: Double)

    private val global = Values(male = 70.8, female = 75.9)

    private val byCountry = mapOf(
        "DE" to Values(male = 78.9, female = 83.5),
    )

    override fun atBirth(countryCode: String?, sex: Sex): Double {
        val values = countryCode?.uppercase()?.let(byCountry::get) ?: global
        return when (sex) {
            Sex.MALE -> values.male
            Sex.FEMALE -> values.female
            Sex.DIVERSE -> (values.male + values.female) / 2
        }
    }
}