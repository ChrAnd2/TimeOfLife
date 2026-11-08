package de.chrisander.timeoflife.ui.customactivities

import de.chrisander.timeoflife.domain.model.activity.Activity

data class CustomActivitiesUiState(
    val activities: List<Activity.CustomActivity>,
)
