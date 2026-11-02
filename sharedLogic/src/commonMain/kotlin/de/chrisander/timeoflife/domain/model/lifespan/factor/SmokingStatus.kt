package de.chrisander.timeoflife.domain.model.lifespan.factor

sealed interface SmokingStatus : HealthFactor {
    data object Never : SmokingStatus
    data object Current : SmokingStatus

    /** Quit smoking at [quitAge]; the earlier, the more of the lost life expectancy is regained. */
    data class Former(val quitAge: Int) : SmokingStatus

    /** Doll et al. 2004: lifelong smokers lose ~10 years; quitting at 30/40/50/60 regains ~10/9/6/3 years. */
    override val yearsEffect: Double
        get() = when (this) {
            Never -> 0.0
            Current -> -10.0
            is Former -> when {
                quitAge < 35 -> 0.0
                quitAge < 45 -> -1.0
                quitAge < 55 -> -4.0
                else -> -7.0
            }
        }
}
