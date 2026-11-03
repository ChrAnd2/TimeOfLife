package de.chrisander.timeoflife.widget.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.chrisander.timeoflife.R
import de.chrisander.timeoflife.ui.theme.TimeOfLifeTheme
import de.chrisander.timeoflife.widget.legend.LegendPosition

@Composable
fun WidgetSettingsScreen(
    viewModel: WidgetSettingsViewModel,
    onSaved: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    WidgetSettingsContent(uiState = uiState, onEvent = viewModel::onEvent)
}

@Composable
private fun WidgetSettingsContent(
    uiState: WidgetSettingsUiState,
    onEvent: (WidgetSettingsEvent) -> Unit,
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.widget_settings_title),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = stringResource(R.string.widget_settings_legend_label),
                style = MaterialTheme.typography.titleMedium,
            )
            Column(Modifier.selectableGroup()) {
                LegendPosition.entries.forEach { position ->
                    LegendPositionOption(
                        position = position,
                        selected = position == uiState.legendPosition,
                        enabled = !uiState.isLoading,
                        onSelect = { onEvent(WidgetSettingsEvent.LegendPositionSelected(position)) },
                    )
                }
            }
            Button(
                onClick = { onEvent(WidgetSettingsEvent.Save) },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.widget_settings_save))
            }
        }
    }
}

@Composable
private fun LegendPositionOption(
    position: LegendPosition,
    selected: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onSelect
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null, enabled = enabled)
        val legendPositionText = when (position) {
            LegendPosition.AUTOMATIC -> R.string.widget_settings_legend_automatic
            LegendPosition.SIDE -> R.string.widget_settings_legend_side
            LegendPosition.BOTTOM -> R.string.widget_settings_legend_bottom
            LegendPosition.HIDDEN -> R.string.widget_settings_legend_hidden
        }
        Text(
            text = stringResource(legendPositionText),
            modifier = Modifier.padding(start = 16.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview
@Composable
private fun WidgetSettingsContentPreview() {
    TimeOfLifeTheme {
        WidgetSettingsContent(uiState = WidgetSettingsUiState(isLoading = false), onEvent = {})
    }
}
