package de.chrisander.timeoflife.domain.model

/**
 * What the life widgets show, independent of how they draw it. All fractions are of the whole life, so 1.0 is the
 * full clock face or the full bar.
 *
 * @property progress share of life already lived.
 * @property segments the lived share split by activity; their fractions add up to [progress].
 * @property clockText the current age as a time on a 24 h clock whose full day is the whole life, e.g. `"14:23"`.
 */
data class LifeProgress(
    val progress: Double,
    val segments: List<LifeSegment>,
    val clockText: String,
)
