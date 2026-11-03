package de.chrisander.timeoflife.widget.settings

import de.chrisander.timeoflife.widget.legend.LegendPosition

sealed interface WidgetSettingsEvent {
    data class LegendPositionSelected(val position: LegendPosition) : WidgetSettingsEvent
    data object Save : WidgetSettingsEvent
}
