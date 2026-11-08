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

@Serializable
data object ProfileRoute : NavKey

@Serializable
data object ProfileDataRoute : NavKey

@Serializable
data object CustomActivitiesRoute : NavKey

/** @property activityId the custom activity to edit, or `null` to add a new one. */
@Serializable
data class CustomActivityEditorRoute(val activityId: String?) : NavKey
