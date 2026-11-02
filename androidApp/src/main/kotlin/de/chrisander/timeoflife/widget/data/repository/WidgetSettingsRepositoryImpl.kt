package de.chrisander.timeoflife.widget.data.repository

import de.chrisander.timeoflife.widget.data.datasource.WidgetSettingsDataSource
import de.chrisander.timeoflife.widget.legend.LegendPosition

class WidgetSettingsRepositoryImpl(
    private val localDataSource: WidgetSettingsDataSource,
) : WidgetSettingsRepository {

    override suspend fun legendPosition(appWidgetId: Int): LegendPosition =
        localDataSource.legendPosition(appWidgetId) ?: LegendPosition.AUTOMATIC

    override suspend fun setLegendPosition(appWidgetId: Int, position: LegendPosition) {
        localDataSource.saveLegendPosition(appWidgetId, position)
    }

    override suspend fun remove(appWidgetIds: IntArray) {
        localDataSource.remove(appWidgetIds)
    }
}
