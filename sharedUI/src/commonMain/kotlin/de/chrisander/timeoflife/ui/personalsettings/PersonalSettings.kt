package de.chrisander.timeoflife.ui.personalsettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.chrisander.timeoflife.domain.model.profile.Sex
import de.chrisander.timeoflife.resources.Res
import de.chrisander.timeoflife.resources.personal_settings_birth_date_label
import de.chrisander.timeoflife.resources.personal_settings_birth_date_placeholder
import de.chrisander.timeoflife.resources.personal_settings_name_label
import de.chrisander.timeoflife.resources.personal_settings_sex_label
import de.chrisander.timeoflife.resources.sex_diverse
import de.chrisander.timeoflife.resources.sex_female
import de.chrisander.timeoflife.resources.sex_male
import de.chrisander.timeoflife.ui.components.ToLDateField
import de.chrisander.timeoflife.ui.components.ToLSingleChoiceSelector
import de.chrisander.timeoflife.ui.theme.TimeOfLifeTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

@Composable
fun PersonalSettings(
    name: String,
    onNameChange: (String) -> Unit,
    sex: Sex?,
    onSexSelect: (Sex) -> Unit,
    birthDate: LocalDate?,
    onBirthDateSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(Res.string.personal_settings_name_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(Res.string.personal_settings_sex_label),
                style = MaterialTheme.typography.labelLarge,
            )
            ToLSingleChoiceSelector(
                options = Sex.entries,
                selected = sex,
                label = {
                    stringResource(
                        when (it) {
                            Sex.MALE -> Res.string.sex_male
                            Sex.FEMALE -> Res.string.sex_female
                            Sex.DIVERSE -> Res.string.sex_diverse
                        },
                    )
                },
                onSelect = onSexSelect,
            )
        }
        ToLDateField(
            date = birthDate,
            onDateSelect = onBirthDateSelect,
            label = stringResource(Res.string.personal_settings_birth_date_label),
            placeholder = stringResource(Res.string.personal_settings_birth_date_placeholder),
            selectableDates = PastOrPresentSelectableDates,
        )
    }
}

private object PastOrPresentSelectableDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
        utcTimeMillis <= Clock.System.now().toEpochMilliseconds()

    override fun isSelectableYear(year: Int): Boolean =
        year <= Clock.System.todayIn(TimeZone.currentSystemDefault()).year
}

@Preview(showBackground = true)
@Composable
private fun PersonalSettingsPreview() {
    TimeOfLifeTheme {
        PersonalSettings(
            name = "Chris",
            onNameChange = {},
            sex = Sex.MALE,
            onSexSelect = {},
            birthDate = LocalDate(1990, 5, 17),
            onBirthDateSelect = {},
        )
    }
}
