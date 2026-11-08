package de.chrisander.timeoflife.ui.healthsettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.chrisander.timeoflife.resources.Res
import de.chrisander.timeoflife.resources.ic_info
import de.chrisander.timeoflife.resources.info_close
import de.chrisander.timeoflife.resources.info_content_description
import de.chrisander.timeoflife.resources.info_source
import de.chrisander.timeoflife.resources.info_sources
import de.chrisander.timeoflife.ui.healthsettings.model.FactorInfo
import de.chrisander.timeoflife.ui.theme.TimeOfLifeTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** An info icon that opens a short explanation of [info] with links to its studies. */
@Composable
internal fun FactorInfoButton(
    title: String,
    info: FactorInfo,
    modifier: Modifier = Modifier,
) {
    var open by rememberSaveable { mutableStateOf(false) }
    IconButton(onClick = { open = true }, modifier = modifier) {
        Icon(
            painter = painterResource(Res.drawable.ic_info),
            contentDescription = stringResource(Res.string.info_content_description, title),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    if (open) {
        AlertDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    open = false
                }) { Text(stringResource(Res.string.info_close)) }
            },
            title = { Text(title) },
            text = { InfoText(info) },
        )
    }
}

@Composable
private fun InfoText(info: FactorInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = stringResource(info.text), style = MaterialTheme.typography.bodyMedium)
        val linkStyles = TextLinkStyles(
            SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline
            ),
        )
        val sourceLabel = stringResource(
            if (info.factorSources.size == 1) Res.string.info_source else Res.string.info_sources,
        )
        Text(
            text = buildAnnotatedString {
                append("$sourceLabel ")
                info.factorSources.forEachIndexed { index, source ->
                    if (index > 0) append(", ")
                    withLink(LinkAnnotation.Url(source.url, linkStyles)) { append(source.citation) }
                }
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FactorInfoButtonPreview() {
    TimeOfLifeTheme {
        FactorInfoButton(title = "Smoking", info = FactorInfo.SMOKING)
    }
}
