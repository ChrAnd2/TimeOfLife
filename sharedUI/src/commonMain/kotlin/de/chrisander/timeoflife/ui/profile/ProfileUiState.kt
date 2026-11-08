package de.chrisander.timeoflife.ui.profile

import kotlinx.datetime.LocalDate

data class ProfileUiState(
    val name: String,
    val birthDate: LocalDate,
    val age: Int,
    val customActivityCount: Int,
)
