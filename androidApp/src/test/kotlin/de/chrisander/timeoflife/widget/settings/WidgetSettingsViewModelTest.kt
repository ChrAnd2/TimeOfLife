package de.chrisander.timeoflife.widget.settings

import de.chrisander.timeoflife.data.datastore.createDataStore
import de.chrisander.timeoflife.widget.data.datasource.WidgetSettingsLocalDataSource
import de.chrisander.timeoflife.widget.data.repository.WidgetSettingsRepositoryImpl
import de.chrisander.timeoflife.widget.legend.LegendPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class WidgetSettingsViewModelTest {

    private val tempDir: File = Files.createTempDirectory("widget-settings-test").toFile()
    private val repository = WidgetSettingsRepositoryImpl(
        WidgetSettingsLocalDataSource(
            createDataStore { tempDir.resolve("widget_settings.preferences_pb").absolutePath },
        ),
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        tempDir.deleteRecursively()
    }

    @Test
    fun defaultsToAutomatic() = runTest {
        val viewModel = WidgetSettingsViewModel(appWidgetId = 1, repository)

        viewModel.uiState.first { !it.isLoading }

        assertEquals(LegendPosition.AUTOMATIC, viewModel.uiState.value.legendPosition)
    }

    @Test
    fun savedPositionIsStoredPerWidget() = runTest {
        val viewModel = WidgetSettingsViewModel(appWidgetId = 1, repository)
        viewModel.uiState.first { !it.isLoading }

        viewModel.onEvent(WidgetSettingsEvent.LegendPositionSelected(LegendPosition.BOTTOM))
        viewModel.onEvent(WidgetSettingsEvent.Save)

        assertTrue(viewModel.uiState.first { it.isSaved }.isSaved)
        assertEquals(LegendPosition.BOTTOM, repository.legendPosition(1))
        assertEquals(LegendPosition.AUTOMATIC, repository.legendPosition(2))
    }

    @Test
    fun opensWithTheStoredPosition() = runTest {
        repository.setLegendPosition(appWidgetId = 1, LegendPosition.HIDDEN)

        val viewModel = WidgetSettingsViewModel(appWidgetId = 1, repository)

        assertEquals(
            LegendPosition.HIDDEN,
            viewModel.uiState.first { !it.isLoading }.legendPosition
        )
    }
}
