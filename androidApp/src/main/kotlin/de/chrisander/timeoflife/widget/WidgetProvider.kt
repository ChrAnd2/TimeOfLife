package de.chrisander.timeoflife.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Typeface
import android.os.Bundle
import android.widget.RemoteViews
import de.chrisander.timeoflife.MainActivity
import de.chrisander.timeoflife.R
import de.chrisander.timeoflife.domain.model.progress.LifeProgress
import de.chrisander.timeoflife.domain.usecase.GetLifeProgressUseCase
import de.chrisander.timeoflife.widget.data.repository.WidgetSettingsRepository
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

abstract class WidgetProvider : AppWidgetProvider(), KoinComponent {

    private val getLifeProgressUseCase: GetLifeProgressUseCase by inject()
    private val settings: WidgetSettingsRepository by inject()

    protected abstract fun render(
        progress: LifeProgress?,
        legend: List<LegendEntry>,
        legendPosition: LegendPosition,
        widthPx: Int,
        heightPx: Int,
        density: Float,
        typeface: Typeface,
    ): Bitmap

    protected abstract fun contentDescription(context: Context, progress: LifeProgress): String

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
                        appWidgetManager = appWidgetManager,
                        appWidgetId = id,
                        progress = progress,
                        legend = legend,
                        legendPosition = settings.legendPosition(id),
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
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        progress: LifeProgress?,
        legend: List<LegendEntry>,
        legendPosition: LegendPosition,
    ): RemoteViews {
        val density = context.resources.displayMetrics.density
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val info = appWidgetManager.getAppWidgetInfo(appWidgetId)
        // Portrait size: the launcher reports min width and max height for it.
        // Without it, fall back to the minimum size from the widget info, which is already in px.
        val widthPx = options.sizePx(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, density)
            ?: info?.minWidth ?: FALLBACK_SIZE_PX
        val heightPx = options.sizePx(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, density)
            ?: info?.minHeight ?: FALLBACK_SIZE_PX

        val typeface = context.resources.getFont(R.font.outfit_semibold)
        val bitmap = render(progress, legend, legendPosition, widthPx, heightPx, density, typeface)
        return RemoteViews(context.packageName, R.layout.widget).apply {
            setImageViewBitmap(R.id.life_widget_image, bitmap)
            setContentDescription(
                R.id.life_widget_image,
                progress?.let { contentDescription(context, it) }
                    ?: context.getString(R.string.widget_set_up),
            )
            setOnClickPendingIntent(R.id.life_widget_image, openAppIntent(context))
        }
    }

    private fun Bundle.sizePx(key: String, density: Float): Int? =
        getInt(key).takeIf { it > 0 }?.let { (it * density).roundToInt() }

    private fun openAppIntent(context: Context): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private companion object {
        const val FALLBACK_SIZE_PX = 300
    }
}
