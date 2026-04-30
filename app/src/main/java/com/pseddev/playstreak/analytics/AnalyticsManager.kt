package com.pseddev.playstreak.analytics

import android.content.Context
import android.content.SharedPreferences
import com.pseddev.playstreak.BuildConfig
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.pseddev.playstreak.PlayStreakApplication
import com.pseddev.playstreak.data.entities.ActivityType
import com.pseddev.playstreak.data.entities.ItemType
import java.text.SimpleDateFormat
import java.util.*

/**
 * Centralized analytics manager for tracking user events in PlayStreak
 * Following Feature #36 implementation requirements
 */
class AnalyticsManager(private val context: Context) {
    
    // Firebase Analytics enabled (performance issue was emulator-related)
    private val analyticsEnabled = !BuildConfig.DEBUG
    
    private val firebaseAnalytics: FirebaseAnalytics by lazy {
        (context.applicationContext as PlayStreakApplication).firebaseAnalytics
    }
    
    // SharedPreferences for streak milestone deduplication
    private val streakPrefs: SharedPreferences by lazy {
        context.getSharedPreferences("streak_milestones", Context.MODE_PRIVATE)
    }
    
    // Date format for daily tracking keys
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    companion object {
        // Event names - following Firebase naming conventions
        private const val EVENT_ACTIVITY_LOGGED = "activity_logged"
        private const val EVENT_STREAK_ACHIEVED = "streak_achieved"
        private const val EVENT_PIECE_ADDED = "piece_added"
        private const val EVENT_DATA_OPERATION = "data_operation"
        private const val EVENT_DATA_PRUNING = "data_pruning"
        private const val EVENT_ACHIEVEMENT_UNLOCKED = "achievement_unlocked"
        
        // Parameter names
        private const val PARAM_ACTIVITY_TYPE = "activity_type"
        private const val PARAM_PIECE_TYPE = "piece_type"
        private const val PARAM_HAS_DURATION = "has_duration"
        private const val PARAM_SOURCE = "source"
        private const val PARAM_STREAK_LENGTH = "streak_length"
        private const val PARAM_EMOJI_LEVEL = "emoji_level"
        private const val PARAM_TOTAL_PIECES = "total_pieces"
        private const val PARAM_OPERATION_TYPE = "operation_type"
        private const val PARAM_ACHIEVEMENT_TYPE = "achievement_type"
        private const val PARAM_ACHIEVEMENT_CATEGORY = "achievement_category"
        private const val PARAM_FORMAT = "format"
        private const val PARAM_ACTIVITY_COUNT = "activity_count"
        private const val PARAM_DELETED_COUNT = "deleted_count"
        private const val PARAM_SUCCESS = "success"
    }
    
    /**
     * Track when user logs a practice or performance activity
     * @param source the entry point: "main_flow", "dashboard_quick", "calendar_quick", "suggestion"
     */
    fun trackActivityLogged(
        activityType: ActivityType,
        pieceType: ItemType,
        hasDuration: Boolean,
        source: String = "unknown"
    ) {
        if (!analyticsEnabled) return
        firebaseAnalytics.logEvent(EVENT_ACTIVITY_LOGGED) {
            param(PARAM_ACTIVITY_TYPE, activityType.name)
            param(PARAM_PIECE_TYPE, pieceType.name)
            param(PARAM_HAS_DURATION, if (hasDuration) 1L else 0L)
            param(PARAM_SOURCE, source)
        }
    }
    
    /**
     * Track when user achieves a practice streak milestone
     * Implements deduplication to ensure each milestone is only sent once per day
     */
    fun trackStreakAchieved(streakLength: Int, emojiLevel: String) {
        if (!analyticsEnabled) return
        
        // Check if this milestone event was already sent today
        val today = dateFormat.format(Date())
        val milestoneKey = "milestone_${streakLength}_$today"
        
        if (streakPrefs.getBoolean(milestoneKey, false)) {
            // Already sent this milestone today, skip
            return
        }
        
        // Send the analytics event
        firebaseAnalytics.logEvent(EVENT_STREAK_ACHIEVED) {
            param(PARAM_STREAK_LENGTH, streakLength.toLong())
            param(PARAM_EMOJI_LEVEL, emojiLevel)
        }
        
        // Mark this milestone as sent for today
        streakPrefs.edit().putBoolean(milestoneKey, true).apply()
        
        // Clean up old milestone tracking data (keep only last 7 days)
        cleanupOldMilestoneData()
    }
    
    /**
     * Track when user adds a new piece or technique
     * @param source the entry point: "pieces_tab", "during_activity_creation"
     */
    fun trackPieceAdded(
        pieceType: ItemType, 
        totalPieceCount: Int,
        source: String = "unknown"
    ) {
        if (!analyticsEnabled) return
        firebaseAnalytics.logEvent(EVENT_PIECE_ADDED) {
            param(PARAM_PIECE_TYPE, pieceType.name)
            param(PARAM_TOTAL_PIECES, totalPieceCount.toLong())
            param(PARAM_SOURCE, source)
        }
    }
    
    /**
     * Track achievement unlocked events
     * @param achievementType the type of achievement unlocked
     * @param category the category: "first_action" or "streak_milestone"
     */
    fun trackAchievementUnlocked(
        achievementType: String,
        category: String
    ) {
        if (!analyticsEnabled) return
        firebaseAnalytics.logEvent(EVENT_ACHIEVEMENT_UNLOCKED) {
            param(PARAM_ACHIEVEMENT_TYPE, achievementType)
            param(PARAM_ACHIEVEMENT_CATEGORY, category)
        }
    }
    
    /**
     * Track data import/export operations (JSON, CSV)
     */
    fun trackDataOperation(
        operationType: String, // "import" or "export"
        format: String, // "json" or "csv"
        activityCount: Int,
        success: Boolean
    ) {
        if (!analyticsEnabled) return
        firebaseAnalytics.logEvent(EVENT_DATA_OPERATION) {
            param(PARAM_OPERATION_TYPE, operationType)
            param(PARAM_FORMAT, format)
            param(PARAM_ACTIVITY_COUNT, activityCount.toLong())
            param(PARAM_SUCCESS, if (success) 1L else 0L)
        }
    }

    /**
     * Track CSV import/export operations (deprecated - use trackDataOperation instead)
     */
    @Deprecated("Use trackDataOperation with format='csv' instead")
    fun trackCsvOperation(
        operationType: String, // "import" or "export"
        activityCount: Int,
        success: Boolean
    ) {
        trackDataOperation(operationType, "csv", activityCount, success)
    }

    /**
     * Track data pruning operations (deletion of oldest activities)
     */
    fun trackDataPruning(
        deletedCount: Int,
        success: Boolean
    ) {
        if (!analyticsEnabled) return
        firebaseAnalytics.logEvent(EVENT_DATA_PRUNING) {
            param(PARAM_DELETED_COUNT, deletedCount.toLong())
            param(PARAM_SUCCESS, if (success) 1L else 0L)
        }
    }
    
    /**
     * Helper function to determine emoji level from streak length
     * Based on emoji progression: 🎵 (3-4) → 🎶 (5-7) → 🔥 (8-13) → 🔥🔥🔥 (14-29) → ⭐⭐⭐ (30-60) → 💎💎💎 (61-99) → 🚀🚀🚀 (100+)
     */
    fun getEmojiLevelForStreak(streakLength: Int): String {
        return when {
            streakLength >= 100 -> "triple_rocket"
            streakLength >= 61 -> "triple_diamond"
            streakLength >= 30 -> "triple_star"
            streakLength >= 14 -> "triple_fire"
            streakLength >= 8 -> "fire"
            streakLength >= 5 -> "musical_notes"
            streakLength >= 3 -> "musical_note"
            else -> "none"
        }
    }
    
    /**
     * Clean up old milestone tracking data to prevent SharedPreferences from growing indefinitely
     * Keeps only the last 7 days of milestone tracking data
     */
    private fun cleanupOldMilestoneData() {
        val editor = streakPrefs.edit()
        val allKeys = streakPrefs.all.keys
        val sevenDaysAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }.time
        val cutoffDate = dateFormat.format(sevenDaysAgo)
        
        allKeys.forEach { key ->
            // Extract date from key format: "milestone_{number}_{date}"
            if (key.startsWith("milestone_")) {
                val parts = key.split("_")
                if (parts.size == 3) {
                    val dateString = parts[2]
                    // Remove keys older than 7 days
                    if (dateString < cutoffDate) {
                        editor.remove(key)
                    }
                }
            }
        }
        
        editor.apply()
    }
    
    /**
     * Force Analytics to sync immediately for testing purposes (DEBUG ONLY)
     * This should only be used during development testing
     */
    fun forceAnalyticsSyncForTesting() {
        if (!analyticsEnabled) return
        // Set a very short session timeout to force immediate event sending
        firebaseAnalytics.setSessionTimeoutDuration(1000) // 1 second
        // This will cause analytics to send batched events immediately
    }
}
