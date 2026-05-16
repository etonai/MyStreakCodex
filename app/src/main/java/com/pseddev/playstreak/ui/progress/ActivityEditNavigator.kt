package com.pseddev.mystreak.ui.progress

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.pseddev.mystreak.R
import com.pseddev.mystreak.data.entities.TaskKind

fun Fragment.navigateToEditActivity(activityWithPiece: ActivityWithPiece) {
    val activity = activityWithPiece.activity
    val task = activityWithPiece.pieceOrTechnique

    EditActivityStorage.setEditActivity(
        activity,
        task.name,
        task.type
    )

    if (task.taskKind == TaskKind.ROUTINE) {
        findNavController().navigate(
            R.id.notesInputFragment,
            bundleOf(
                "activityType" to activity.activityType,
                "pieceId" to activity.pieceOrTechniqueId,
                "pieceName" to task.name,
                "level" to 3,
                "performanceType" to "routine"
            )
        )
    } else {
        findNavController().navigate(
            R.id.action_viewProgressFragment_to_selectLevelFragment,
            bundleOf(
                "activityType" to activity.activityType,
                "pieceId" to activity.pieceOrTechniqueId,
                "pieceName" to task.name,
                "itemType" to task.type
            )
        )
    }
}
