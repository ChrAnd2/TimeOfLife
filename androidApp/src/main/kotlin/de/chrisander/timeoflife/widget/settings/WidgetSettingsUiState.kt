package de.chrisander.timeoflife.widget.settings

import de.chrisander.timeoflife.widget.legend.LegendPosition

data class WidgetSettingsUiState(
    val legendPosition: LegendPosition = LegendPosition.AUTOMATIC,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
)
