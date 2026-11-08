package de.chrisander.timeoflife.ui.healthsettings.model

import androidx.annotation.StringRes
import de.chrisander.timeoflife.R

enum class FactorInfo(
    @param:StringRes val text: Int,
    val factorSources: List<FactorSource>,
) {
    COUNTRY(
        R.string.info_country,
        listOf(
            FactorSource(
                "Destatis, Sterbetafel 2024",
                "https://www.destatis.de/DE/Themen/Gesellschaft-Umwelt/Bevoelkerung/" +
                        "Sterbefaelle-Lebenserwartung/_inhalt.html",
            ),
            FactorSource(
                "WHO Global Health Estimates, 2019",
                "https://www.who.int/data/gho/data/themes/mortality-and-global-health-estimates/" +
                        "ghe-life-expectancy-and-healthy-life-expectancy",
            ),
        ),
    ),
    BODY(
        R.string.info_body,
        listOf(
            FactorSource(
                "Prospective Studies Collaboration, Lancet 2009",
                "https://doi.org/10.1016/S0140-6736(09)60318-4",
            ),
        ),
    ),
    SMOKING(
        R.string.info_smoking,
        listOf(
            FactorSource(
                "Doll et al., BMJ 2004",
                "https://doi.org/10.1136/bmj.38142.554479.AE"
            )
        ),
    ),
    ALCOHOL(
        R.string.info_alcohol,
        listOf(
            FactorSource(
                "Wood et al., Lancet 2018",
                "https://doi.org/10.1016/S0140-6736(18)30134-X"
            )
        ),
    ),
    EXERCISE(
        R.string.info_exercise,
        listOf(
            FactorSource(
                "Moore et al., PLoS Medicine 2012",
                "https://doi.org/10.1371/journal.pmed.1001335"
            )
        ),
    ),
    CONDITIONS(
        R.string.info_conditions,
        listOf(
            FactorSource(
                "Emerging Risk Factors Collaboration, JAMA 2015",
                "https://doi.org/10.1001/jama.2015.7008",
            ),
        ),
    ),
}
