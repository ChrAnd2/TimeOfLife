package de.chrisander.timeoflife.widget.data.repository

import de.chrisander.timeoflife.widget.legend.LegendPosition

interface WidgetSettingsRepository {
    suspend fun legendPosition(appWidgetId: Int): LegendPosition

    suspend fun setLegendPosition(appWidgetId: Int, position: LegendPosition)

    suspend fun remove(appWidgetIds: IntArray)
}
