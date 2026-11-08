package de.chrisander.timeoflife.widget

data class WidgetLifeProgress(
    val progress: Double,
    val clockText: String,
    val segments: List<WidgetSegment>,
)
