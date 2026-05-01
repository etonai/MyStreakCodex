package com.pseddev.mystreak.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages app preferences using SharedPreferences for local storage.
 * Handles calendar activities detail mode toggle and other user preferences.
 */
class PreferencesManager private constructor(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Check if calendar activities detail mode is enabled
     * @return true if detail mode is enabled, false for regular mode
     */
    fun isCalendarDetailModeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_CALENDAR_ACTIVITIES_DETAIL_MODE, false)
    }

    /**
     * Set the calendar activities detail mode preference
     * @param enabled true to enable detail mode, false for regular mode
     */
    fun setCalendarDetailModeEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_CALENDAR_ACTIVITIES_DETAIL_MODE, enabled)
            .apply()
    }

    /**
     * Toggle calendar activities detail mode
     * @return the new detail mode status after toggling
     */
    fun toggleCalendarDetailMode(): Boolean {
        val newStatus = !isCalendarDetailModeEnabled()
        setCalendarDetailModeEnabled(newStatus)
        return newStatus
    }

    companion object {
        private const val PREFS_NAME = "mystreak_preferences"
        private const val KEY_CALENDAR_ACTIVITIES_DETAIL_MODE = "calendar_activities_detail_mode"

        @Volatile
        private var INSTANCE: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferencesManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
