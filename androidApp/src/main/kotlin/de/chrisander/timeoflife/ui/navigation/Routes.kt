package de.chrisander.timeoflife.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object OnboardingRoute : NavKey

@Serializable
data object HomeRoute : NavKey

/** @property activityId the activity's id, or `null` for free time. */
@Serializable
data class ActivityDetailRoute(val activityId: String?) : NavKey
