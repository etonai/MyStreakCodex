package com.pseddev.mystreak.utils

import android.content.Context
import android.os.Build
import android.util.Log
import com.pseddev.mystreak.BuildConfig
import com.pseddev.mystreak.data.models.BackupMetadata
import com.pseddev.mystreak.data.models.SyncData
import com.pseddev.mystreak.data.models.SyncOperationResult
import com.pseddev.mystreak.data.repository.PianoRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.first
import java.util.*

class SyncManager(
    private val context: Context,
    private val repository: PianoRepository,
    private val driveHelper: GoogleDriveHelper
) {

    companion object {
        private const val TAG = "SyncManager"
        private const val PREFS_NAME = "piano_tracker_sync_prefs"
        private const val KEY_LAST_SYNC = "last_sync_time"
        private const val KEY_SYNC_COUNT = "sync_count"
        private const val KEY_SYNC_ENABLED = "sync_enabled"
        private const val KEY_GOOGLE_EMAIL = "google_email"
    }

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    private val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    suspend fun performSync(): SyncOperationResult {
        return try {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Starting sync operation")
            }

            // Get current data from local database
            val activities = repository.getAllActivities().first()
            val pieces = repository.getAllPiecesAndTechniques().first()

            // Create sync data
            val syncData = SyncData(
                piecesTechniques = pieces,
                activities = activities
            )

            // Create metadata
            val syncCount = getSyncCount()
            val metadata = BackupMetadata(
                deviceId = getDeviceId(),
                appVersion = "1.0", // Hardcoded version
                totalActivities = activities.size,
                totalPieces = pieces.size,
                syncCount = syncCount + 1
            )

            // Convert to JSON
            val jsonData = gson.toJson(syncData)
            val jsonMetadata = gson.toJson(metadata)

            // Upload to Drive
            val uploadResult = driveHelper.uploadDataToDrive(jsonData, jsonMetadata)

            if (uploadResult.isSuccess) {
                // Update sync preferences
                updateSyncPreferences(syncCount + 1)

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Sync completed successfully")
                }
                SyncOperationResult(
                    success = true,
                    message = "Sync completed successfully",
                    syncedActivities = activities.size,
                    syncedPieces = pieces.size
                )
            } else {
                Log.e(TAG, "Sync failed: ${uploadResult.exceptionOrNull()?.message}")
                SyncOperationResult(
                    success = false,
                    message = uploadResult.exceptionOrNull()?.message ?: "Unknown error"
                )
            }

        } catch (e: Exception) {
            Log.e(TAG, "Sync failed with exception", e)
            SyncOperationResult(
                success = false,
                message = e.message ?: "Unknown error"
            )
        }
    }

    suspend fun restoreFromDrive(): SyncOperationResult {
        return try {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Starting restore operation")
            }

            // Download from Drive
            val downloadResult = driveHelper.downloadDataFromDrive()

            if (downloadResult.isSuccess) {
                val jsonData = downloadResult.getOrNull()
                if (jsonData != null) {
                    // Parse JSON
                    val syncData = gson.fromJson(jsonData, SyncData::class.java)

                    // Clear local data
                    repository.deleteAllActivities()
                    repository.deleteAllPiecesAndTechniques()

                    // Restore pieces/techniques
                    syncData.piecesTechniques.forEach { piece ->
                        repository.insertPieceOrTechnique(piece)
                    }

                    // Restore activities
                    syncData.activities.forEach { activity ->
                        repository.insertActivity(activity)
                    }

                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Restore completed successfully")
                    }
                    SyncOperationResult(
                        success = true,
                        message = "Data restored successfully",
                        syncedActivities = syncData.activities.size,
                        syncedPieces = syncData.piecesTechniques.size
                    )
                } else {
                    SyncOperationResult(
                        success = false,
                        message = "No data to restore"
                    )
                }
            } else {
                Log.e(TAG, "Restore failed: ${downloadResult.exceptionOrNull()?.message}")
                SyncOperationResult(
                    success = false,
                    message = downloadResult.exceptionOrNull()?.message ?: "Unknown error"
                )
            }

        } catch (e: Exception) {
            Log.e(TAG, "Restore failed with exception", e)
            SyncOperationResult(
                success = false,
                message = e.message ?: "Unknown error"
            )
        }
    }

    suspend fun performSmartSync(): SyncOperationResult {
        return try {
            // Check if there's data in Drive
            val lastDriveSyncTime = driveHelper.getLastSyncTime()

            if (lastDriveSyncTime.isSuccess) {
                val driveTime = lastDriveSyncTime.getOrNull()
                val localTime = getLastSyncTime()

                // If no Drive data or local is newer, upload
                if (driveTime == null || (localTime > 0 && localTime > driveTime.time)) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Local data is newer, performing upload")
                    }
                    performSync()
                } else {
                    // Drive data is newer, ask user or auto-download
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Drive data is newer, performing download")
                    }
                    restoreFromDrive()
                }
            } else {
                // Error getting Drive sync time, default to upload
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Could not get Drive sync time, performing upload")
                }
                performSync()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Smart sync failed", e)
            SyncOperationResult(
                success = false,
                message = e.message ?: "Unknown error"
            )
        }
    }

    private fun getDeviceId(): String {
        return "${Build.MANUFACTURER}_${Build.MODEL}_${Build.ID}".replace(" ", "_")
    }

    private fun updateSyncPreferences(newSyncCount: Int) {
        sharedPrefs.edit().apply {
            putLong(KEY_LAST_SYNC, System.currentTimeMillis())
            putInt(KEY_SYNC_COUNT, newSyncCount)
            apply()
        }
    }

    fun getLastSyncTime(): Long {
        return sharedPrefs.getLong(KEY_LAST_SYNC, 0L)
    }

    fun getSyncCount(): Int {
        return sharedPrefs.getInt(KEY_SYNC_COUNT, 0)
    }

    fun isSyncEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_SYNC_ENABLED, false)
    }

    fun setSyncEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_SYNC_ENABLED, enabled).apply()
    }

    fun setGoogleEmail(email: String?) {
        sharedPrefs.edit().putString(KEY_GOOGLE_EMAIL, email).apply()
    }

    fun getGoogleEmail(): String? {
        return sharedPrefs.getString(KEY_GOOGLE_EMAIL, null)
    }

    fun clearSyncData() {
        sharedPrefs.edit().clear().apply()
    }
}