package de.chrisander.timeoflife.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import de.chrisander.timeoflife.widget.clock.LifeClockWidgetProvider

object LifeWidgets {

    /** List of widget providers (e.g. clock and bar widget) so they refresh on profile changes. */
    private val providers: List<Class<out AppWidgetProvider>> =
        listOf(LifeClockWidgetProvider::class.java)

    fun requestUpdate(context: Context, appWidgetId: Int) {
        val provider =
            AppWidgetManager.getInstance(context).getAppWidgetInfo(appWidgetId)?.provider ?: return
        context.sendBroadcast(
            Intent()
                .setComponent(provider)
                .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId)),
        )
    }

    fun requestUpdate(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        providers.forEach { provider ->
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, provider))
            if (ids.isNotEmpty()) {
                context.sendBroadcast(
                    Intent(context, provider)
                        .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids),
                )
            }
        }
    }
}
