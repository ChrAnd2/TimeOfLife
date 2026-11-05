package de.chrisander.timeoflife.widget.clock

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import de.chrisander.timeoflife.R
import de.chrisander.timeoflife.domain.model.LifeProgress
import de.chrisander.timeoflife.widget.WidgetProvider
import de.chrisander.timeoflife.widget.legend.LegendEntry
import de.chrisander.timeoflife.widget.legend.LegendPosition

class ClockWidgetProvider : WidgetProvider() {

    override fun render(
        progress: LifeProgress?,
        legend: List<LegendEntry>,
        legendPosition: LegendPosition,
        widthPx: Int,
        heightPx: Int,
        density: Float,
        typeface: Typeface,
    ): Bitmap = renderClockWidget(
        progress, legend, legendPosition, widthPx, heightPx, density, typeface
    )

    override fun contentDescription(context: Context, progress: LifeProgress): String =
        context.getString(R.string.clock_widget_content_description, progress.clockText)
}
