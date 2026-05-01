package com.pseddev.mystreak.utils

import android.content.Context
import android.content.SharedPreferences
import com.pseddev.mystreak.BuildConfig

/**
 * Manages Pro user status using SharedPreferences for local storage.
 * This is a simple implementation for development and testing purposes.
 * In production, this would integrate with Google Play Billing or similar service.
 */
class ProUserManager private constructor(context: Context) {


    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Check if the current user is a Pro subscriber
     * @return true if user has Pro status, false for free user
     */
    fun isProUser(): Boolean {
        return BuildConfig.IS_PRO_VERSION || sharedPreferences.getBoolean(KEY_IS_PRO_USER, false)
    }

    /**
     * Set the Pro user status
     * @param isPro true to grant Pro status, false for free user
     */
    fun setProUser(isPro: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_IS_PRO_USER, isPro)
            .apply()
    }

    /**
     * Toggle Pro user status (useful for testing)
     * @return the new Pro status after toggling
     */
    fun toggleProStatus(): Boolean {
        val newStatus = !isProUser()
        setProUser(newStatus)
        return newStatus
    }

    /**
     * Reset Pro status to free user (useful for testing)
     */
    fun resetToFreeUser() {
        setProUser(false)
    }

    /**
     * Check if user can add more favorites based on Pro status and current count
     * @param currentFavoriteCount the current number of favorites the user has
     * @return true if user can add more favorites, false if at limit
     */
    fun canAddMoreFavorites(currentFavoriteCount: Int): Boolean {
        return if (isProUser()) {
            true // Pro users have unlimited favorites
        } else {
            currentFavoriteCount < FREE_USER_FAVORITE_LIMIT // Free users: only if under 4
        }
    }

    /**
     * Check if user can add more pieces/techniques based on Pro status and current count
     * @param currentPieceCount the current total number of pieces and techniques the user has
     * @return true if user can add more pieces/techniques, false if at limit
     */
    fun canAddMorePieces(currentPieceCount: Int): Boolean {
        val limit = getPieceLimit()
        return currentPieceCount < limit
    }

    /**
     * Get the piece limit for the current user type
     * @return the maximum number of pieces allowed for this user
     */
    fun getPieceLimit(): Int {
        return if (isProUser()) {
            PRO_USER_PIECE_LIMIT
        } else {
            FREE_USER_PIECE_LIMIT
        }
    }

    /**
     * Get the activity limit for the current user type
     * @return the maximum number of activities allowed for this user
     */
    fun getActivityLimit(): Int {
        return if (isProUser()) {
            PRO_USER_ACTIVITY_LIMIT
        } else {
            FREE_USER_ACTIVITY_LIMIT
        }
    }

    companion object {
        private const val PREFS_NAME = "mystreak_pro_prefs"
        private const val KEY_IS_PRO_USER = "is_pro_user"
        const val FREE_USER_FAVORITE_LIMIT = 4
        // Update activity and piece limits
        private const val FREE_USER_ACTIVITY_LIMIT = 1825
        private const val PRO_USER_ACTIVITY_LIMIT = 4000
        private const val FREE_USER_PIECE_LIMIT = 500
        private const val PRO_USER_PIECE_LIMIT = 550

        // Practice Suggestions Limits
        const val FREE_USER_PRACTICE_FAVORITE_SUGGESTIONS = 2
        const val PRO_USER_PRACTICE_FAVORITE_SUGGESTIONS = 4
        const val FREE_USER_PRACTICE_NON_FAVORITE_SUGGESTIONS = 2
        const val PRO_USER_PRACTICE_NON_FAVORITE_SUGGESTIONS = 4
        const val PRO_USER_PERFORMANCE_SUGGESTIONS = 5  // Free users get 0

        @Volatile
        private var INSTANCE: ProUserManager? = null

        fun getInstance(context: Context): ProUserManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ProUserManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
