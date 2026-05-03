package com.pseddev.mystreak.ui.progress

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

private val SuccessLevel.label: String
    get() = when (this) {
        SuccessLevel.MINIMUM -> "Minimum"
        SuccessLevel.MEDIUM -> "Medium"
        SuccessLevel.HIGH -> "High"
    }
