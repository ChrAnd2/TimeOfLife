package de.chrisander.timeoflife.widget

import de.chrisander.timeoflife.brand.BrandPalette
import de.chrisander.timeoflife.brand.SegmentColors
import de.chrisander.timeoflife.domain.model.progress.LifeSegment

internal object WidgetColors {
    const val FACE = BrandPalette.PANEL.toInt()
    const val RIM = BrandPalette.RIM.toInt()
    const val FUTURE = BrandPalette.FUTURE.toInt()
    const val FREE_TIME = BrandPalette.LILAC.toInt()
    const val TEXT = BrandPalette.WHITE.toInt()
    const val TEXT_DIM = 0x99FFFFFF.toInt()
    const val HAND = BrandPalette.WHITE.toInt()

    fun of(segment: LifeSegment): Int = SegmentColors.of(segment).toInt()
}
