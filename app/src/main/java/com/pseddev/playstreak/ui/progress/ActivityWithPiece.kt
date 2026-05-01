package com.pseddev.mystreak.ui.progress

import com.pseddev.mystreak.data.entities.Activity
import com.pseddev.mystreak.data.entities.PieceOrTechnique

data class ActivityWithPiece(
    val activity: Activity,
    val pieceOrTechnique: PieceOrTechnique
)