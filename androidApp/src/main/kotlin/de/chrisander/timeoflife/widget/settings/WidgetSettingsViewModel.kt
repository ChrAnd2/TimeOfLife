package de.chrisander.timeoflife.widget.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.chrisander.timeoflife.widget.data.repository.WidgetSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WidgetSettingsViewModel(
    private val appWidgetId: Int,
    private val widgetSettingsRepository: WidgetSettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WidgetSettingsUiState())
    val uiState: StateFlow<WidgetSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val position = widgetSettingsRepository.legendPosition(appWidgetId)
            _uiState.update { it.copy(legendPosition = position, isLoading = false) }
        }
    }

    fun onEvent(event: WidgetSettingsEvent) {
        when (event) {
            is WidgetSettingsEvent.LegendPositionSelected -> _uiState.update {
                it.copy(
                    legendPosition = event.position
                )
            }

            WidgetSettingsEvent.Save -> viewModelScope.launch {
                widgetSettingsRepository.setLegendPosition(
                    appWidgetId,
                    _uiState.value.legendPosition
                )
                _uiState.update { it.copy(isSaved = true) }
            }
        }
    }
}
