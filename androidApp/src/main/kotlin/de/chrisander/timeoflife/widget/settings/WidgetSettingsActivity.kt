package de.chrisander.timeoflife.widget.settings

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import de.chrisander.timeoflife.ui.theme.TimeOfLifeTheme
import de.chrisander.timeoflife.widget.Widgets
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Configuration activity for the life widgets.
 */
class WidgetSettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        // Leaving without saving cancels placing the widget, as Android expects from configuration activities.
        setResult(RESULT_CANCELED, result)

        setContent {
            TimeOfLifeTheme {
                WidgetSettingsScreen(
                    viewModel = koinViewModel { parametersOf(appWidgetId) },
                    onSave = {
                        Widgets.requestUpdate(this, appWidgetId)
                        setResult(RESULT_OK, result)
                        finish()
                    },
                )
            }
        }
    }
}
