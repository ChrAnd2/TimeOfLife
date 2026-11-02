package de.chrisander.timeoflife.domain.model.profile

sealed interface LifespanSetting {
    data object Estimated : LifespanSetting

    data class Manual(val years: Double) : LifespanSetting {
        init {
            require(years > 0) { "Life span must be positive, was $years" }
        }
    }
}
