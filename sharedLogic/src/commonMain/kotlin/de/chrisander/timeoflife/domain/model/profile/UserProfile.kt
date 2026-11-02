package de.chrisander.timeoflife.domain.model.profile

import de.chrisander.timeoflife.domain.model.activity.Activity
import de.chrisander.timeoflife.domain.model.activity.Activity.DefaultActivity
import de.chrisander.timeoflife.domain.model.activity.DefaultActivityType
import kotlinx.datetime.LocalDate

data class UserProfile(
    val name: String,
    val sex: Sex,
    val birthDate: LocalDate,
    val lifespanSetting: LifespanSetting = LifespanSetting.Estimated,
    val lifespanFactors: LifespanFactors = LifespanFactors(),
    val activities: List<Activity> = DefaultActivityType.entries.map(::DefaultActivity),
)
