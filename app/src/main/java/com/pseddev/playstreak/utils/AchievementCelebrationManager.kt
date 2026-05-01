package com.pseddev.mystreak.utils

import android.content.Context
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.pseddev.mystreak.data.entities.Achievement
import com.pseddev.mystreak.data.entities.AchievementType

/**
 * Manages achievement celebration notifications for all achievement types
 */
class AchievementCelebrationManager(private val context: Context) {

    private val celebrationsTemporarilyDisabled = true
    private val configurationManager = ConfigurationManager.getInstance(context)

    /**
     * Show celebration for a specific achievement if celebrations are enabled
     * @param view The view to anchor the snackbar to
     * @param achievement The achievement that was unlocked
     */
    fun showCelebration(view: View, achievement: Achievement) {
        if (celebrationsTemporarilyDisabled) {
            return
        }

        // Check if celebrations are enabled in configuration
        if (!configurationManager.isAchievementCelebrationEnabled()) {
            return
        }

        // Celebrate all achievement types

        // Create celebration message with "Achievement unlocked!" on first line
        val celebrationText = "Achievement unlocked!\n${achievement.iconEmoji} ${achievement.title}: ${achievement.description}"

        // Show snackbar with 5-second duration and larger height for multi-line text
        val snackbar = Snackbar.make(view, celebrationText, Snackbar.LENGTH_LONG)
            .setDuration(5000) // 5 seconds as specified

        // Make the snackbar text view multi-line to accommodate both lines
        val snackbarView = snackbar.view
        val textView = snackbarView.findViewById<android.widget.TextView>(com.google.android.material.R.id.snackbar_text)
        textView.maxLines = 3 // Allow up to 3 lines for the message

        snackbar.show()
    }


    /**
     * Check if celebrations are currently enabled
     * @return true if celebrations should be shown
     */
    fun areCelebrationsEnabled(): Boolean {
        if (celebrationsTemporarilyDisabled) {
            return false
        }

        return configurationManager.isAchievementCelebrationEnabled()
    }
}
