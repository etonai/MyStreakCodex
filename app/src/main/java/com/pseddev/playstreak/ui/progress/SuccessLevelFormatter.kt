package com.pseddev.mystreak.ui.progress

import com.pseddev.mystreak.data.entities.Activity
import com.pseddev.mystreak.data.entities.PieceOrTechnique
import com.pseddev.mystreak.data.entities.SuccessLevel

fun successLevelDescription(successLevel: SuccessLevel, task: PieceOrTechnique): String {
    val label = successLevel.label
    val description = when (successLevel) {
        SuccessLevel.MINIMUM -> task.minimumSuccess
        SuccessLevel.MEDIUM -> task.mediumSuccess
        SuccessLevel.HIGH -> task.highSuccess
    }.trim()

    return if (description.isBlank() || description.equals(label, ignoreCase = true)) {
        label
    } else {
        "$description ($label)"
    }
}

fun successLevelFromActivityLevel(level: Int): SuccessLevel? {
    return when (level) {
        1 -> SuccessLevel.MINIMUM
        2 -> SuccessLevel.MEDIUM
        3, 4 -> SuccessLevel.HIGH
        else -> null
    }
}

fun activityDescription(activity: Activity, task: PieceOrTechnique): String {
    val successText = successLevelDescription(activity.successLevel, task)
    val notes = activity.notes.trim()
    return if (notes.isEmpty()) successText else "$successText\nNotes: $notes"
}

private val SuccessLevel.label: String
    get() = when (this) {
        SuccessLevel.MINIMUM -> "Minimum"
        SuccessLevel.MEDIUM -> "Medium"
        SuccessLevel.HIGH -> "High"
    }
