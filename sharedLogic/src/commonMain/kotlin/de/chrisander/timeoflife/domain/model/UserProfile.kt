package de.chrisander.timeoflife.domain.model

import kotlinx.datetime.LocalDate

data class UserProfile(
    val name: String,
    val sex: Sex,
    val birthDate: LocalDate,
)

