package com.pseddev.mystreak.utils

import android.content.Context
import android.util.Log
import com.pseddev.mystreak.data.AppDatabase
import com.pseddev.mystreak.data.entities.Achievement
import com.pseddev.mystreak.data.entities.AchievementType
import com.pseddev.mystreak.data.entities.ActivityType
import com.pseddev.mystreak.data.entities.ItemType
import com.pseddev.mystreak.data.repository.PianoRepository
import com.pseddev.mystreak.analytics.AnalyticsManager
import kotlinx.coroutines.flow.first

class AchievementManager(
    private val context: Context,
    private val repository: PianoRepository
) {

    private val database = AppDatabase.getDatabase(context)
    private val achievementDao = database.achievementDao()
    private val analyticsManager = AnalyticsManager(context)

    /**
     * Initialize achievements system - should be called on app startup
     * This will create all achievement definitions and detect retroactive achievements
     */
    suspend fun initializeAchievements() {
        try {
            Log.d("AchievementManager", "Initializing achievements system")

            val existingCount = achievementDao.getTotalCount()
            val unlockedCount = achievementDao.getUnlockedCount()
            val achievementDefinitions = AchievementDefinitions.getAllAchievementDefinitions()

            Log.d("AchievementManager", "Database state: $existingCount total achievements, $unlockedCount unlocked")

            if (existingCount == 0) {
                // First time initialization - insert all achievement definitions
                achievementDao.insertAllAchievements(achievementDefinitions)
                Log.d("AchievementManager", "Inserted ${achievementDefinitions.size} achievement definitions")

                // Always detect retroactive achievements when table is empty
                Log.d("AchievementManager", "Running retroactive achievement detection...")
                detectRetroactiveAchievements()
                Log.d("AchievementManager", "Retroactive achievement detection completed")
            } else {
                // Update existing achievement definitions while preserving unlock state
                Log.d("AchievementManager", "Updating existing achievement definitions while preserving unlock state")
                updateAchievementDefinitionsPreservingState(achievementDefinitions)
                Log.d("AchievementManager", "Updated achievement definitions")

                // Run retroactive detection if no achievements are unlocked
                if (unlockedCount == 0) {
                    Log.d("AchievementManager", "No achievements unlocked, running retroactive detection...")
                    detectRetroactiveAchievements()
                    Log.d("AchievementManager", "Retroactive achievement detection completed")
                } else {
                    Log.d("AchievementManager", "Found $unlockedCount unlocked achievements, skipping retroactive detection")
                }
            }

            Log.d("AchievementManager", "Achievement initialization completed")
        } catch (e: Exception) {
            Log.e("AchievementManager", "Error initializing achievements", e)
        }
    }

    /**
     * Detect and unlock achievements based on existing user data
     */
    private suspend fun detectRetroactiveAchievements() {
        try {
            Log.d("AchievementManager", "Starting retroactive achievement detection")

            val pieces = repository.getAllPiecesAndTechniques().first()
            val activities = repository.getAllActivities().first()
            val currentTime = System.currentTimeMillis()

            // Check for first piece achievement
            val firstPiece = pieces.filter { it.type == ItemType.PIECE }.minByOrNull { it.dateCreated }
            if (firstPiece != null) {
                unlockAchievement(AchievementType.FIRST_PIECE, firstPiece.dateCreated)
            }

            // Check for first technique achievement
            val firstTechnique = pieces.filter { it.type == ItemType.TECHNIQUE }.minByOrNull { it.dateCreated }
            if (firstTechnique != null) {
                unlockAchievement(AchievementType.FIRST_TECHNIQUE, firstTechnique.dateCreated)
            }

            // Check for first practice achievement
            val firstPractice = activities.filter { it.activityType == ActivityType.PRACTICE }.minByOrNull { it.timestamp }
            if (firstPractice != null) {
                unlockAchievement(AchievementType.FIRST_PRACTICE, firstPractice.timestamp)
            }

            // Check for first performance achievement
            val firstPerformance = activities.filter { it.activityType == ActivityType.PERFORMANCE }.minByOrNull { it.timestamp }
            if (firstPerformance != null) {
                unlockAchievement(AchievementType.FIRST_PERFORMANCE, firstPerformance.timestamp)

                // Check for specific performance types
                val firstOnline = activities.filter {
                    it.activityType == ActivityType.PERFORMANCE && it.performanceType.lowercase() == "online"
                }.minByOrNull { it.timestamp }

                if (firstOnline != null) {
                    unlockAchievement(AchievementType.FIRST_ONLINE_PERFORMANCE, firstOnline.timestamp)
                }

                val firstLive = activities.filter {
                    it.activityType == ActivityType.PERFORMANCE && it.performanceType.lowercase() == "live"
                }.minByOrNull { it.timestamp }

                if (firstLive != null) {
                    unlockAchievement(AchievementType.FIRST_LIVE_PERFORMANCE, firstLive.timestamp)
                }
            }

            // Check for streak achievements
            detectRetroactiveStreakAchievements(activities)

            Log.d("AchievementManager", "Retroactive achievement detection completed")
        } catch (e: Exception) {
            Log.e("AchievementManager", "Error detecting retroactive achievements", e)
        }
    }

    /**
     * Detect retroactive streak achievements based on existing activity data
     */
    private suspend fun detectRetroactiveStreakAchievements(activities: List<com.pseddev.mystreak.data.entities.Activity>) {
        try {
            if (activities.isEmpty()) return

            val streakCalculator = StreakCalculator()
            val currentStreak = streakCalculator.calculateCurrentStreak(activities)

            // For retroactive detection, use current streak as the highest achieved
            // Note: This assumes current streak represents the user's best achievement
            val useStreak = currentStreak

            val streakMilestones = listOf(3, 5, 8, 14, 30, 61, 100)
            val achievementTypes = listOf(
                AchievementType.STREAK_3_DAYS,
                AchievementType.STREAK_5_DAYS,
                AchievementType.STREAK_8_DAYS,
                AchievementType.STREAK_14_DAYS,
                AchievementType.STREAK_30_DAYS,
                AchievementType.STREAK_61_DAYS,
                AchievementType.STREAK_100_DAYS
            )

            // Find the date when each milestone was first reached
            for (i in streakMilestones.indices) {
                val milestone = streakMilestones[i]
                if (useStreak >= milestone) {
                    // Find approximate date when this milestone was first achieved
                    val milestoneDate = findStreakMilestoneDate(activities, milestone)
                    if (milestoneDate != null) {
                        unlockAchievement(achievementTypes[i], milestoneDate)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AchievementManager", "Error detecting retroactive streak achievements", e)
        }
    }

    /**
     * Find the actual date when a streak milestone was first reached
     */
    private fun findStreakMilestoneDate(activities: List<com.pseddev.mystreak.data.entities.Activity>, milestone: Int): Long? {
        if (activities.isEmpty()) return null

        val streakCalculator = StreakCalculator()
        return streakCalculator.findStreakMilestoneDate(activities, milestone)
    }

    /**
     * Unlock an achievement if it's not already unlocked
     */
    suspend fun unlockAchievement(type: AchievementType, unlockedAt: Long = System.currentTimeMillis()) {
        try {
            val isAlreadyUnlocked = achievementDao.isAchievementUnlocked(type)
            if (!isAlreadyUnlocked) {
                achievementDao.unlockAchievement(type, unlockedAt)
                Log.d("AchievementManager", "Unlocked achievement: $type at $unlockedAt")

                // Track analytics for achievement unlock
                val category = when (type) {
                    AchievementType.FIRST_PIECE,
                    AchievementType.FIRST_TECHNIQUE,
                    AchievementType.FIRST_PRACTICE,
                    AchievementType.FIRST_PERFORMANCE,
                    AchievementType.FIRST_ONLINE_PERFORMANCE,
                    AchievementType.FIRST_LIVE_PERFORMANCE -> "first_action"

                    AchievementType.STREAK_3_DAYS,
                    AchievementType.STREAK_5_DAYS,
                    AchievementType.STREAK_8_DAYS,
                    AchievementType.STREAK_14_DAYS,
                    AchievementType.STREAK_30_DAYS,
                    AchievementType.STREAK_61_DAYS,
                    AchievementType.STREAK_100_DAYS -> "streak_milestone"
                }

                analyticsManager.trackAchievementUnlocked(type.name, category)
            }
        } catch (e: Exception) {
            Log.e("AchievementManager", "Error unlocking achievement $type", e)
        }
    }

    /**
     * Check if an achievement is unlocked
     */
    suspend fun isAchievementUnlocked(type: AchievementType): Boolean {
        return try {
            achievementDao.isAchievementUnlocked(type)
        } catch (e: Exception) {
            Log.e("AchievementManager", "Error checking if achievement is unlocked: $type", e)
            false
        }
    }

    /**
     * Get achievement counts for display
     */
    suspend fun getAchievementCounts(): Pair<Int, Int> {
        return try {
            val unlocked = achievementDao.getUnlockedCount()
            val total = achievementDao.getTotalCount()
            Pair(unlocked, total)
        } catch (e: Exception) {
            Log.e("AchievementManager", "Error getting achievement counts", e)
            Pair(0, 0)
        }
    }

    /**
     * Update achievement definitions while preserving existing unlock state
     */
    private suspend fun updateAchievementDefinitionsPreservingState(newDefinitions: List<Achievement>) {
        try {
            for (newAchievement in newDefinitions) {
                val existingAchievement = achievementDao.getAchievementByType(newAchievement.type)

                if (existingAchievement != null) {
                    // Preserve unlock state and date from existing achievement
                    val updatedAchievement = newAchievement.copy(
                        isUnlocked = existingAchievement.isUnlocked,
                        unlockedAt = existingAchievement.unlockedAt,
                        dateCreated = existingAchievement.dateCreated
                    )
                    achievementDao.insertAchievement(updatedAchievement)
                    Log.d("AchievementManager", "Updated existing achievement: ${newAchievement.type} (locked: ${!existingAchievement.isUnlocked})")
                } else {
                    // New achievement, insert as-is
                    achievementDao.insertAchievement(newAchievement)
                    Log.d("AchievementManager", "Inserted new achievement: ${newAchievement.type}")
                }
            }
        } catch (e: Exception) {
            Log.e("AchievementManager", "Error updating achievement definitions", e)
        }
    }

    /**
     * Reset all achievements for debugging purposes (debug builds only)
     */
    suspend fun resetAllAchievements() {
        try {
            Log.d("AchievementManager", "Resetting all achievements for debug")

            // Reset all achievement unlock states
            achievementDao.resetAllAchievements()

            // Re-run retroactive detection to repopulate based on current data
            detectRetroactiveAchievements()

            Log.d("AchievementManager", "Achievement reset completed")
        } catch (e: Exception) {
            Log.e("AchievementManager", "Error resetting achievements", e)
        }
    }
}