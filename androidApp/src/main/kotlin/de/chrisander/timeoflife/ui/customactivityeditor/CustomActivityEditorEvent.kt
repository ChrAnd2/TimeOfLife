package de.chrisander.timeoflife.ui.customactivityeditor

import de.chrisander.timeoflife.ui.customactivityeditor.model.IntervalUnit

sealed interface CustomActivityEditorEvent {
    data class NameChanged(val name: String) : CustomActivityEditorEvent
    data class DurationSelected(val minutes: Int) : CustomActivityEditorEvent
    data class EveryChanged(val every: String) : CustomActivityEditorEvent
    data class UnitSelected(val unit: IntervalUnit) : CustomActivityEditorEvent
    data class FromAgeChanged(val fromAge: String) : CustomActivityEditorEvent
    data class UntilAgeChanged(val untilAge: String) : CustomActivityEditorEvent
    data object Save : CustomActivityEditorEvent
    data object Delete : CustomActivityEditorEvent
    data object ConfirmDelete : CustomActivityEditorEvent
    data object DismissDelete : CustomActivityEditorEvent
    data object Back : CustomActivityEditorEvent
    data object DismissError : CustomActivityEditorEvent
}
