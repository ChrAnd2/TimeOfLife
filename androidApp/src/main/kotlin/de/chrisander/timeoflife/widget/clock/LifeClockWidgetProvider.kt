package de.chrisander.timeoflife.widget.clock

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import de.chrisander.timeoflife.MainActivity
import de.chrisander.timeoflife.R
import de.chrisander.timeoflife.widget.GetLifeProgressUseCase
import de.chrisander.timeoflife.widget.data.repository.WidgetSettingsRepository
import de.chrisander.timeoflife.widget.domain.model.LifeProgress
import de.chrisander.timeoflife.widget.legend.LegendEntry
import de.chrisander.timeoflife.widget.legend.LegendPosition
import de.chrisander.timeoflife.widget.legend.legendEntries
import de.chrisander.timeoflife.widget.legend.legendLabel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.roundToInt

class LifeClockWidgetProvider : AppWidgetProvider(), KoinComponent {

    private val getLifeProgressUseCase: GetLifeProgressUseCase by inject()
    private val settings: WidgetSettingsRepository by inject()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        update(context, appWidgetManager, appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) {
        // Redraw at the new size, the bitmap would be blurry otherwise.
        update(context, appWidgetManager, intArrayOf(appWidgetId))
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                settings.remove(appWidgetIds)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun update(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val progress = getLifeProgressUseCase.load()
                val legend = progress?.legendEntries(context::legendLabel).orEmpty()
                appWidgetIds.forEach { id ->
                    val views = remoteViews(
                        context = context,
                        progress = progress,
                        legend = legend,
                        legendPosition = settings.legendPosition(id),
                        options = appWidgetManager.getAppWidgetOptions(id),
                    )
                    appWidgetManager.updateAppWidget(id, views)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun remoteViews(
        context: Context,
        progress: LifeProgress?,
        legend: List<LegendEntry>,
        legendPosition: LegendPosition,
        options: Bundle,
    ): RemoteViews {
        val density = context.resources.displayMetrics.density
        // Portrait size: the launcher reports min width and max height for it.
        val widthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH).takeIf { it > 0 }
            ?: DEFAULT_WIDTH_DP
        val heightDp =
            options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT).takeIf { it > 0 }
                ?: DEFAULT_HEIGHT_DP
        val bitmap = renderLifeClockWidget(
            progress = progress,
            legend = legend,
            legendPosition = legendPosition,
            widthPx = (widthDp * density).roundToInt(),
            heightPx = (heightDp * density).roundToInt(),
            density = density,
        )
        return RemoteViews(context.packageName, R.layout.clock_widget).apply {
            setImageViewBitmap(R.id.life_clock_image, bitmap)
            setContentDescription(
                R.id.life_clock_image,
                progress?.let {
                    context.getString(
                        R.string.clock_widget_content_description,
                        it.clockText
                    )
                }
                    ?: context.getString(R.string.widget_set_up),
            )
            setOnClickPendingIntent(R.id.life_clock_image, openAppIntent(context))
        }
    }

    private fun openAppIntent(context: Context): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private companion object {
        const val DEFAULT_WIDTH_DP = 250
        const val DEFAULT_HEIGHT_DP = 110
    }
}
