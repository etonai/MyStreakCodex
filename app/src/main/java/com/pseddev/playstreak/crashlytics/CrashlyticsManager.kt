package com.pseddev.playstreak.crashlytics

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.pseddev.playstreak.BuildConfig
import com.pseddev.playstreak.PlayStreakApplication

/**
 * Centralized crash reporting manager for PlayStreak
 * Following Feature #36 implementation requirements
 */
class CrashlyticsManager(private val context: Context) {
    
    private val firebaseCrashlytics: FirebaseCrashlytics by lazy {
        (context.applicationContext as PlayStreakApplication).firebaseCrashlytics
    }
    
    /**
     * Record a non-fatal exception for monitoring
     */
    fun recordException(exception: Exception) {
        if (BuildConfig.DEBUG) return
        firebaseCrashlytics.recordException(exception)
    }
    
    /**
     * Log a message for crash context
     */
    fun log(message: String) {
        if (BuildConfig.DEBUG) return
        firebaseCrashlytics.log(message)
    }
    
    /**
     * Set custom key-value pair for crash reporting context
     */
    fun setCustomKey(key: String, value: String) {
        if (BuildConfig.DEBUG) return
        firebaseCrashlytics.setCustomKey(key, value)
    }
    
    /**
     * Set custom key-value pair for crash reporting context (boolean)
     */
    fun setCustomKey(key: String, value: Boolean) {
        if (BuildConfig.DEBUG) return
        firebaseCrashlytics.setCustomKey(key, value)
    }
    
    /**
     * Set custom key-value pair for crash reporting context (int)
     */
    fun setCustomKey(key: String, value: Int) {
        if (BuildConfig.DEBUG) return
        firebaseCrashlytics.setCustomKey(key, value)
    }
    
    /**
     * Set user identifier for crash reporting
     */
    fun setUserId(userId: String) {
        if (BuildConfig.DEBUG) return
        firebaseCrashlytics.setUserId(userId)
    }
    
    /**
     * Force a crash for testing purposes (DEBUG ONLY)
     * This should only be used during development testing
     */
    fun forceCrashForTesting() {
        throw RuntimeException("Test crash triggered for Firebase Crashlytics verification")
    }
    
    /**
     * Record database operation error context
     */
    fun recordDatabaseError(operation: String, exception: Exception) {
        setCustomKey("database_operation", operation)
        setCustomKey("error_type", "database")
        log("Database operation failed: $operation")
        recordException(exception)
    }
    
    /**
     * Record CSV import/export error context
     */
    fun recordCsvError(operation: String, activityCount: Int, exception: Exception) {
        setCustomKey("csv_operation", operation)
        setCustomKey("csv_activity_count", activityCount)
        setCustomKey("error_type", "csv")
        log("CSV operation failed: $operation with $activityCount activities")
        recordException(exception)
    }
    
    /**
     * Record sync/Google Drive error context
     */
    fun recordSyncError(operation: String, exception: Exception) {
        setCustomKey("sync_operation", operation)
        setCustomKey("error_type", "sync")
        log("Sync operation failed: $operation")
        recordException(exception)
    }
}
