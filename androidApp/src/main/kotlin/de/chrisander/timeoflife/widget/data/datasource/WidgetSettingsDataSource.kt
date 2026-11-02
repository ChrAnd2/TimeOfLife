package de.chrisander.timeoflife.widget.data.datasource

import de.chrisander.timeoflife.widget.legend.LegendPosition

interface WidgetSettingsDataSource {
    suspend fun legendPosition(appWidgetId: Int): LegendPosition?

    suspend fun saveLegendPosition(appWidgetId: Int, position: LegendPosition)

    suspend fun remove(appWidgetIds: IntArray)
}
