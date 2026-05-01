package com.pseddev.mystreak.data.models

import com.pseddev.mystreak.data.entities.Activity
import com.pseddev.mystreak.data.entities.PieceOrTechnique
import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * Data class representing the complete sync data for Google Drive
 */
data class SyncData(
    @SerializedName("format_version")
    val formatVersion: String = "1.0",

    @SerializedName("export_timestamp")
    val exportTimestamp: Long = System.currentTimeMillis(),

    @SerializedName("pieces_techniques")
    val piecesTechniques: List<PieceOrTechnique>,

    @SerializedName("activities")
    val activities: List<Activity>
)

/**
 * Metadata for the backup
 */
data class BackupMetadata(
    @SerializedName("last_sync_timestamp")
    val lastSyncTimestamp: Long = System.currentTimeMillis(),

    @SerializedName("device_id")
    val deviceId: String,

    @SerializedName("app_version")
    val appVersion: String,

    @SerializedName("total_activities")
    val totalActivities: Int,

    @SerializedName("total_pieces")
    val totalPieces: Int,

    @SerializedName("sync_count")
    val syncCount: Int = 0
)

/**
 * Sync state stored in SharedPreferences
 */
data class SyncState(
    val isEnabled: Boolean = false,
    val lastSyncTime: Long = 0L,
    val syncCount: Int = 0,
    val googleAccountEmail: String? = null,
    val lastSyncResult: SyncResult = SyncResult.NEVER_SYNCED
)

enum class SyncResult {
    SUCCESS,
    FAILED,
    NEVER_SYNCED
}

/**
 * Result of a sync operation
 */
data class SyncOperationResult(
    val success: Boolean,
    val message: String,
    val syncedActivities: Int = 0,
    val syncedPieces: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)