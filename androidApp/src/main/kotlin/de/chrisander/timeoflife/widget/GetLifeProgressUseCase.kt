package de.chrisander.timeoflife.widget

import de.chrisander.timeoflife.data.repository.UserProfileRepository
import de.chrisander.timeoflife.domain.usecase.GetLifeTimeBreakdownUseCase
import de.chrisander.timeoflife.widget.domain.model.LifeProgress
import de.chrisander.timeoflife.widget.mapper.toLifeProgress
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

class GetLifeProgressUseCase(
    private val userProfileRepository: UserProfileRepository,
    private val getLifeTimeBreakdown: GetLifeTimeBreakdownUseCase,
    private val clock: Clock,
) {
    suspend fun load(): LifeProgress? {
        val profile = userProfileRepository.userProfile.first() ?: return null
        return getLifeTimeBreakdown(
            profile,
            clock.todayIn(TimeZone.currentSystemDefault())
        ).toLifeProgress()
    }
}
