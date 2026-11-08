package de.chrisander.timeoflife.widget

import de.chrisander.timeoflife.brand.SegmentColors
import de.chrisander.timeoflife.data.datasource.StaticBaselineLifeExpectancyProvider
import de.chrisander.timeoflife.data.datasource.UserProfileJson
import de.chrisander.timeoflife.domain.mapper.toLifeProgress
import de.chrisander.timeoflife.domain.model.activity.Activity
import de.chrisander.timeoflife.domain.model.profile.UserProfile
import de.chrisander.timeoflife.domain.model.progress.LifeSegment
import de.chrisander.timeoflife.domain.usecase.GetLifeTimeBreakdownUseCase
import de.chrisander.timeoflife.domain.usecase.LifeTimeCalculator
import de.chrisander.timeoflife.domain.usecase.StudyBasedLifespanEstimator
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import platform.Foundation.NSUserDefaults
import kotlin.time.Clock

object WidgetProfileStore {

    const val APP_GROUP = "group.de.chrisander.timeoflife"
    private const val PROFILE_KEY = "widget_profile"

    private val defaults: NSUserDefaults?
        get() = NSUserDefaults(suiteName = APP_GROUP)

    fun save(profile: UserProfile?) {
        val defaults = defaults ?: return
        if (profile == null) {
            defaults.removeObjectForKey(PROFILE_KEY)
        } else {
            defaults.setObject(UserProfileJson.encode(profile), forKey = PROFILE_KEY)
        }
    }

    fun loadLifeProgress(): WidgetLifeProgress? {
        val profile =
            defaults?.stringForKey(PROFILE_KEY)?.let(UserProfileJson::decode) ?: return null
        val getLifeTimeBreakdown = GetLifeTimeBreakdownUseCase(
            StudyBasedLifespanEstimator(StaticBaselineLifeExpectancyProvider),
            LifeTimeCalculator(),
        )
        val progress =
            getLifeTimeBreakdown(profile, Clock.System.todayIn(TimeZone.currentSystemDefault()))
                .toLifeProgress()
        return WidgetLifeProgress(
            progress = progress.progress,
            clockText = progress.clockText,
            segments = progress.segments.map { segment ->
                val activity = (segment as? LifeSegment.ActivitySegment)?.activity
                WidgetSegment(
                    fraction = segment.fraction,
                    color = SegmentColors.of(segment),
                    activityKey = when (activity) {
                        null -> "FREE_TIME"
                        is Activity.DefaultActivity -> activity.type.name
                        is Activity.CustomActivity -> "CUSTOM"
                    },
                    customName = (activity as? Activity.CustomActivity)?.name,
                )
            },
        )
    }
}
