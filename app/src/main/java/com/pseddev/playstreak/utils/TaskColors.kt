package com.pseddev.mystreak.utils

import com.pseddev.mystreak.data.entities.PieceOrTechnique
import com.pseddev.mystreak.data.entities.TaskKind

object TaskColors {
    const val DEFAULT_TASK_COLOR = "#66B2FF"
    const val ROUTINE_COLOR = "#D3D3D3"

    fun storedColorFor(taskKind: TaskKind, selectedColor: String): String {
        return if (taskKind == TaskKind.ROUTINE) ROUTINE_COLOR else selectedColor
    }

    fun displayColorFor(task: PieceOrTechnique): String {
        return storedColorFor(task.taskKind, task.color)
    }
}
