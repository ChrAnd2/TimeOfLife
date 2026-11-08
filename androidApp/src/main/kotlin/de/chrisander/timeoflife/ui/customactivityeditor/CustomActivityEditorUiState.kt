package de.chrisander.timeoflife.ui.customactivityeditor

import de.chrisander.timeoflife.domain.model.activity.Recurrence
import de.chrisander.timeoflife.ui.customactivityeditor.model.IntervalUnit

data class CustomActivityEditorUiState(
    val isNew: Boolean = true,
    val name: String = "",
    val durationMinutes: Int = DEFAULT_DURATION_MINUTES,
    val durationOptions: List<Int> = emptyList(),
    val every: String = "1",
    val unit: IntervalUnit = IntervalUnit.WEEK,
    val fromAge: String = "",
    val untilAge: String = "",
    val recurrence: Recurrence? = null,
    val everyError: Boolean = false,
    val ageError: Boolean = false,
    val canSave: Boolean = false,
    val isSaving: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val showSaveError: Boolean = false,
)

private const val DEFAULT_DURATION_MINUTES = 60
