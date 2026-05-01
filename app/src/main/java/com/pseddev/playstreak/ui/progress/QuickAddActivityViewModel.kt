package com.pseddev.mystreak.ui.progress

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.pseddev.mystreak.analytics.AnalyticsManager
import com.pseddev.mystreak.data.entities.Activity
import com.pseddev.mystreak.data.entities.PieceOrTechnique
import com.pseddev.mystreak.data.repository.PianoRepository
import com.pseddev.mystreak.utils.ProUserManager
import com.pseddev.mystreak.utils.AchievementManager
import com.pseddev.mystreak.data.entities.AchievementType
import com.pseddev.mystreak.data.entities.ActivityType
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class QuickAddActivityViewModel(
    private val repository: PianoRepository,
    private val context: Context
) : ViewModel() {

    private val proUserManager = ProUserManager.getInstance(context)
    private val analyticsManager = AnalyticsManager(context)
    private val achievementManager = AchievementManager(context, repository)

    private val _addResult = MutableLiveData<Result<Unit>>()
    val addResult: LiveData<Result<Unit>> = _addResult

    fun getTask(taskId: Long): LiveData<PieceOrTechnique?> {
        return repository.getAllPiecesAndTechniques()
            .map { tasks -> tasks.find { it.id == taskId } }
            .asLiveData()
    }

    fun addActivity(activity: Activity, source: String = "dashboard_quick") {
        viewModelScope.launch {
            try {
                // Check activity limit before adding
                val currentActivityCount = repository.getActivityCount()
                val activityLimit = proUserManager.getActivityLimit()

                if (currentActivityCount >= activityLimit) {
                    val limitMessage = "You have reached the activity limit of $activityLimit activities. Cannot add more activities."
                    _addResult.postValue(Result.failure(IllegalStateException(limitMessage)))
                    return@launch
                }

                repository.insertActivity(activity)

                // Check for first activity achievements
                checkFirstActivityAchievements(activity.activityType, activity.performanceType)

                // Track analytics event with provided source context
                val piece = repository.getPieceOrTechniqueById(activity.pieceOrTechniqueId)
                piece?.let {
                    analyticsManager.trackActivityLogged(
                        activityType = activity.activityType,
                        pieceType = it.type,
                        hasDuration = activity.minutes > 0,
                        source = source
                    )
                }

                // Check for streak achievements
                val newStreak = repository.calculateCurrentStreak()
                trackStreakAchievement(newStreak)

                _addResult.postValue(Result.success(Unit))
            } catch (e: Exception) {
                _addResult.postValue(Result.failure(e))
            }
        }
    }

    /**
     * Check for first activity achievements
     */
    private suspend fun checkFirstActivityAchievements(activityType: ActivityType, performanceType: String) {
        when (activityType) {
            ActivityType.PRACTICE -> {
                achievementManager.unlockAchievement(AchievementType.FIRST_PRACTICE)
            }
            ActivityType.PERFORMANCE -> {
                achievementManager.unlockAchievement(AchievementType.FIRST_PERFORMANCE)

                // Check for specific performance type achievements
                when (performanceType.lowercase()) {
                    "online" -> {
                        achievementManager.unlockAchievement(AchievementType.FIRST_ONLINE_PERFORMANCE)
                    }
                    "live" -> {
                        achievementManager.unlockAchievement(AchievementType.FIRST_LIVE_PERFORMANCE)
                    }
                }
            }
        }
    }

    /**
     * Track streak achievement milestones
     */
    private suspend fun trackStreakAchievement(streakLength: Int) {
        // Map streak lengths to achievement types
        val achievementType = when (streakLength) {
            3 -> AchievementType.STREAK_3_DAYS
            5 -> AchievementType.STREAK_5_DAYS
            8 -> AchievementType.STREAK_8_DAYS
            14 -> AchievementType.STREAK_14_DAYS
            30 -> AchievementType.STREAK_30_DAYS
            61 -> AchievementType.STREAK_61_DAYS
            100 -> AchievementType.STREAK_100_DAYS
            else -> null
        }

        // Only track milestones at specific levels and unlock achievement
        achievementType?.let { type ->
            achievementManager.unlockAchievement(type)
        }
    }
}

class QuickAddActivityViewModelFactory(
    private val repository: PianoRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuickAddActivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuickAddActivityViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
