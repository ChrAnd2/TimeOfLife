package de.chrisander.timeoflife.ui.healthsettings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.chrisander.timeoflife.R
import de.chrisander.timeoflife.domain.model.lifespan.factor.AlcoholConsumption
import de.chrisander.timeoflife.domain.model.lifespan.factor.HealthCondition
import de.chrisander.timeoflife.domain.model.lifespan.factor.PhysicalActivityLevel
import de.chrisander.timeoflife.ui.components.ToLLevelSlider
import de.chrisander.timeoflife.ui.components.ToLMultiChoiceSelector
import de.chrisander.timeoflife.ui.components.ToLNumberField
import de.chrisander.timeoflife.ui.components.ToLSingleChoiceSelector
import de.chrisander.timeoflife.ui.healthsettings.model.FactorInfo
import de.chrisander.timeoflife.ui.healthsettings.model.SmokingChoice
import de.chrisander.timeoflife.ui.theme.TimeOfLifeTheme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HealthSettings(
    uiState: HealthSettingsUiState,
    onEvent: (HealthSettingsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(28.dp)) {
        Section(
            R.string.health_settings_country_label,
            FactorInfo.COUNTRY,
            R.string.health_settings_country_hint
        ) {
            CountryField(
                uiState.countryCode,
                onSelect = { onEvent(HealthSettingsEvent.CountrySelected(it)) },
            )
        }
        Section(
            R.string.health_settings_body_label,
            FactorInfo.BODY,
            R.string.health_settings_body_hint
        ) {
            BodyFields(uiState, onEvent)
        }
        Section(R.string.health_settings_smoking_label, FactorInfo.SMOKING) {
            SmokingSelector(uiState, onEvent)
        }
        Section(
            R.string.health_settings_alcohol_label,
            FactorInfo.ALCOHOL,
            R.string.health_settings_alcohol_hint
        ) {
            AlcoholSlider(uiState.alcohol, onEvent)
        }
        Section(
            R.string.health_settings_activity_label,
            FactorInfo.EXERCISE,
            R.string.health_settings_activity_hint
        ) {
            ActivitySlider(uiState.physicalActivity, onEvent)
        }
        Section(
            R.string.health_settings_conditions_label,
            FactorInfo.CONDITIONS,
            R.string.health_settings_conditions_hint
        ) {
            ConditionsSelector(uiState.conditions, onEvent)
        }
    }
}

@Composable
private fun AlcoholSlider(
    selected: AlcoholConsumption,
    onEvent: (HealthSettingsEvent) -> Unit,
) {
    ToLLevelSlider(
        levels = AlcoholConsumption.entries,
        selected = selected,
        label = {
            stringResource(
                when (it) {
                    AlcoholConsumption.NONE -> R.string.level_none
                    AlcoholConsumption.LOW -> R.string.level_low
                    AlcoholConsumption.MODERATE -> R.string.level_moderate
                    AlcoholConsumption.HIGH -> R.string.level_high
                    AlcoholConsumption.VERY_HIGH -> R.string.level_very_high
                },
            )
        },
        description = {
            stringResource(
                when (it) {
                    AlcoholConsumption.NONE -> R.string.alcohol_none
                    AlcoholConsumption.LOW -> R.string.alcohol_low
                    AlcoholConsumption.MODERATE -> R.string.alcohol_moderate
                    AlcoholConsumption.HIGH -> R.string.alcohol_high
                    AlcoholConsumption.VERY_HIGH -> R.string.alcohol_very_high
                },
            )
        },
        onSelect = { onEvent(HealthSettingsEvent.AlcoholSelected(it)) },
    )
}

@Composable
private fun ActivitySlider(
    selected: PhysicalActivityLevel,
    onEvent: (HealthSettingsEvent) -> Unit,
) {
    ToLLevelSlider(
        levels = PhysicalActivityLevel.entries,
        selected = selected,
        label = {
            stringResource(
                when (it) {
                    PhysicalActivityLevel.NONE -> R.string.level_none
                    PhysicalActivityLevel.LOW -> R.string.level_low
                    PhysicalActivityLevel.MODERATE -> R.string.level_moderate
                    PhysicalActivityLevel.HIGH -> R.string.level_high
                },
            )
        },
        description = {
            stringResource(
                when (it) {
                    PhysicalActivityLevel.NONE -> R.string.activity_level_none
                    PhysicalActivityLevel.LOW -> R.string.activity_level_low
                    PhysicalActivityLevel.MODERATE -> R.string.activity_level_moderate
                    PhysicalActivityLevel.HIGH -> R.string.activity_level_high
                },
            )
        },
        onSelect = { onEvent(HealthSettingsEvent.PhysicalActivitySelected(it)) },
    )
}

@Composable
private fun ConditionsSelector(
    selected: Set<HealthCondition>,
    onEvent: (HealthSettingsEvent) -> Unit,
) {
    ToLMultiChoiceSelector(
        options = HealthCondition.entries,
        selected = selected,
        label = {
            stringResource(
                when (it) {
                    HealthCondition.DIABETES -> R.string.condition_diabetes
                    HealthCondition.MYOCARDIAL_INFARCTION -> R.string.condition_heart_attack
                    HealthCondition.STROKE -> R.string.condition_stroke
                },
            )
        },
        onToggle = { onEvent(HealthSettingsEvent.ConditionToggled(it)) },
    )
}

@Composable
private fun Section(
    @StringRes title: Int,
    info: FactorInfo,
    @StringRes hint: Int? = null,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                FactorInfoButton(
                    title = stringResource(title),
                    info = info,
                    modifier = Modifier.offset(x = 12.dp)
                )
            }
            if (hint != null) {
                Text(
                    text = stringResource(hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.offset(y = (-8).dp),
                )
            }
        }
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountryField(
    countryCode: String?,
    onSelect: (String?) -> Unit,
) {
    val countries = remember {
        Locale.getISOCountries()
            .map { it to Locale.Builder().setRegion(it).build().displayCountry }
            .sortedBy { it.second }
    }
    val selectedName = countries.firstOrNull { it.first == countryCode }?.second.orEmpty()
    var query by rememberSaveable(countryCode) { mutableStateOf(selectedName) }
    var expanded by remember { mutableStateOf(false) }
    val matches = query.trim().let { search ->
        countries
            .filter { it.second.contains(search, ignoreCase = true) }
            .sortedBy { !it.second.startsWith(search, ignoreCase = true) }
    }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                expanded = true
                if (it.isBlank()) onSelect(null)
            },
            placeholder = { Text(stringResource(R.string.health_settings_country_global)) },
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded && matches.isNotEmpty(),
            onDismissRequest = {
                expanded = false
                query = selectedName
            },
        ) {
            matches.forEach { (code, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        expanded = false
                        query = name
                        onSelect(code)
                    },
                )
            }
        }
    }
}

@Composable
private fun BodyFields(
    uiState: HealthSettingsUiState,
    onEvent: (HealthSettingsEvent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ToLNumberField(
                value = uiState.height,
                onValueChange = { onEvent(HealthSettingsEvent.HeightChanged(it)) },
                label = stringResource(R.string.health_settings_height_label),
                isError = uiState.heightError,
                modifier = Modifier.weight(1f),
            )
            ToLNumberField(
                value = uiState.weight,
                onValueChange = { onEvent(HealthSettingsEvent.WeightChanged(it)) },
                label = stringResource(R.string.health_settings_weight_label),
                isError = uiState.weightError,
                modifier = Modifier.weight(1f),
            )
        }
        val bmi = uiState.bmi
        val message = when {
            uiState.heightError || uiState.weightError -> stringResource(R.string.health_settings_body_error)
            bmi != null -> stringResource(
                R.string.health_settings_bmi,
                NumberFormat.getNumberInstance().apply { maximumFractionDigits = 1 }.format(bmi),
            )

            else -> null
        }
        if (message != null) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.run { if (bmi == null) error else onSurfaceVariant },
                modifier = Modifier.padding(start = 16.dp),
            )
        }
    }
}

@Composable
private fun SmokingSelector(
    uiState: HealthSettingsUiState,
    onEvent: (HealthSettingsEvent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ToLSingleChoiceSelector(
            options = SmokingChoice.entries,
            selected = uiState.smoking,
            label = {
                stringResource(
                    when (it) {
                        SmokingChoice.NEVER -> R.string.smoking_never
                        SmokingChoice.FORMER -> R.string.smoking_former
                        SmokingChoice.CURRENT -> R.string.smoking_current
                    },
                )
            },
            onSelect = { onEvent(HealthSettingsEvent.SmokingSelected(it)) },
        )
        if (uiState.smoking == SmokingChoice.FORMER) {
            ToLNumberField(
                value = uiState.quitAge,
                onValueChange = { onEvent(HealthSettingsEvent.QuitAgeChanged(it)) },
                label = stringResource(R.string.health_settings_quit_age_label),
                isError = uiState.quitAgeError,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HealthSettingsPreview() {
    TimeOfLifeTheme {
        HealthSettings(
            uiState = HealthSettingsUiState(
                countryCode = "DE",
                height = "182",
                weight = "78",
                bmi = 23.5,
                smoking = SmokingChoice.FORMER,
            ),
            onEvent = {},
        )
    }
}
