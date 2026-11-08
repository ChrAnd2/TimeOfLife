package de.chrisander.timeoflife.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.chrisander.timeoflife.ui.navigation.AppNavigation
import de.chrisander.timeoflife.ui.theme.TimeOfLifeTheme
import de.chrisander.timeoflife.ui.util.BooleanPreviewProvider
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TimeOfLifeApp(
    viewModel: MainViewModel = koinViewModel(),
) {
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    TimeOfLifeAppContent(isLoading)
}

@Composable
private fun TimeOfLifeAppContent(isLoading: Boolean) {
    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        else -> AppNavigation()
    }
}

@Preview
@Composable
private fun TimeOfLifeAppPreview(
    @PreviewParameter(BooleanPreviewProvider::class) isLoading: Boolean
) {
    TimeOfLifeTheme {
        TimeOfLifeAppContent(isLoading = isLoading)
    }
}
