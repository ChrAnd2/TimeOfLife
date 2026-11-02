package de.chrisander.timeoflife.widget.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import de.chrisander.timeoflife.widget.legend.LegendPosition
import kotlinx.coroutines.flow.first

class WidgetSettingsLocalDataSource(
    private val dataStore: DataStore<Preferences>,
) : WidgetSettingsDataSource {

    companion object {
        const val WIDGET_SETTINGS_FILE_NAME = "widget_settings.preferences_pb"
    }

    override suspend fun legendPosition(appWidgetId: Int): LegendPosition? =
        dataStore.data.first()[legendPositionKey(appWidgetId)]
            ?.let { stored -> LegendPosition.entries.find { it.name == stored } }

    override suspend fun saveLegendPosition(appWidgetId: Int, position: LegendPosition) {
        dataStore.edit { it[legendPositionKey(appWidgetId)] = position.name }
    }

    override suspend fun remove(appWidgetIds: IntArray) {
        dataStore.edit { preferences ->
            appWidgetIds.forEach { preferences.remove(legendPositionKey(it)) }
        }
    }

    private fun legendPositionKey(appWidgetId: Int) =
        stringPreferencesKey("legend_position_$appWidgetId")
}
