package de.chrisander.timeoflife.data.datasource.mapper

import de.chrisander.timeoflife.data.datasource.dto.LifespanSettingDto
import de.chrisander.timeoflife.domain.model.profile.LifespanSetting

internal fun LifespanSetting.toDto(): LifespanSettingDto = when (this) {
    LifespanSetting.Estimated -> LifespanSettingDto.Estimated
    is LifespanSetting.Manual -> LifespanSettingDto.Manual(years)
}

internal fun LifespanSettingDto.toDomain(): LifespanSetting = when (this) {
    LifespanSettingDto.Estimated -> LifespanSetting.Estimated
    is LifespanSettingDto.Manual -> LifespanSetting.Manual(years)
}
