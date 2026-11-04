package de.chrisander.timeoflife.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.chrisander.timeoflife.R
import de.chrisander.timeoflife.ui.theme.Outfit
import de.chrisander.timeoflife.ui.theme.TimeOfLifeTheme

/**
 * The TimeOfLife logo: the clock mark with the wordmark next to it. The proportions match `docs/brand/logo.svg`.
 *
 * @param height of the mark; the wordmark scales with it.
 */
@Composable
fun TimeOfLifeLogo(
    modifier: Modifier = Modifier,
    height: Dp = 40.dp,
) {
    val appName = stringResource(R.string.app_name)
    Row(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = appName },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(height * HORIZONTAL_SPACING_FACTOR),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_logo_mark),
            contentDescription = null,
            modifier = Modifier.size(height),
        )
        Text(
            text = buildAnnotatedString {
                append("Time")
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.primary
                    )
                ) {
                    append("Of")
                }
                append("Life")
            },
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = Outfit,
            fontWeight = FontWeight.SemiBold,
            // Same ratio of text to mark as in the SVG logo (84 / 120).
            fontSize = (height.value * 0.7f).sp,
            maxLines = 1,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TimeOfLifeLogoPreview() {
    TimeOfLifeTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TimeOfLifeLogo()
            TimeOfLifeLogo(height = 64.dp)
        }
    }
}

private const val HORIZONTAL_SPACING_FACTOR = 0.22f
