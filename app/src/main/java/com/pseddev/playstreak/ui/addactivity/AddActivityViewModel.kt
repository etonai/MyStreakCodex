package com.pseddev.playstreak.ui.addactivity

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.pseddev.playstreak.analytics.AnalyticsManager
import com.pseddev.playstreak.crashlytics.CrashlyticsManager
import com.pseddev.playstreak.data.entities.Activity
import com.pseddev.playstreak.data.entities.ActivityType
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.data.entities.PieceOrTechnique
import com.pseddev.playstreak.data.repository.PianoRepository
import com.pseddev.playstreak.ui.progress.EditActivityStorage
import com.pseddev.playstreak.utils.ProUserManager
import com.pseddev.playstreak.utils.TextNormalizer
import com.pseddev.playstreak.utils.AchievementManager
import com.pseddev.playstreak.utils.AchievementCelebrationManager
import com.pseddev.playstreak.data.entities.AchievementType
import com.pseddev.playstreak.utils.AchievementDefinitions
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddActivityViewModel(
    private val repository: PianoRepository,
    private val context: Context
) : ViewModel() {
    
    private val analyticsManager = AnalyticsManager(context)
    private val crashlyticsManager = CrashlyticsManager(context)
    private val proUserManager = ProUserManager.getInstance(context)
    private val achievementManager = AchievementManager(context, repository)
    private val achievementCelebrationManager = AchievementCelebrationManager(context)
    
    private val _navigateToMain = MutableLiveData<Boolean>()
    val navigateToMain: LiveData<Boolean> = _navigateToMain
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    private val _editMode = MutableLiveData<Boolean>(false)
    val editMode: LiveData<Boolean> = _editMode
    
    private val _editActivity = MutableLiveData<Activity?>()
    val editActivity: LiveData<Activity?> = _editActivity
    
    private val _showCelebration = MutableLiveData<AchievementType?>()
    val showCelebration: LiveData<AchievementType?> = _showCelebration
    
    fun getPiecesAndTechniques(activityType: ActivityType): LiveData<List<PieceOrTechnique>> {
        return repository.getActiveTasks().asLiveData()
    }
    
    fun getFavorites(): LiveData<List<PieceOrTechnique>> {
        return repository.getActiveHighPriorityTasks().asLiveData()
    }

    fun getTask(taskId: Long): LiveData<PieceOrTechnique?> {
        return repository.getAllPiecesAndTechniques()
            .map { tasks -> tasks.find { it.id == taskId } }
            .asLiveData()
    }
    
    fun insertPieceOrTechnique(name: String, type: ItemType, onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            try {
                // Check for duplicate name first (case-insensitive)
                val normalizedName = TextNormalizer.normalizePieceName(name)
                if (repository.doesPieceNameExist(normalizedName)) {
                    _errorMessage.value = "This task already exists"
                    return@launch
                }
                
                // Check piece limit before adding
                val currentPieceCount = repository.getAllPiecesAndTechniques().first().size
                if (!proUserManager.canAddMorePieces(currentPieceCount)) {
                    val limit = proUserManager.getPieceLimit()
                    _errorMessage.value = "You have reached the task limit of $limit tasks. Cannot add more tasks."
                    return@launch
                }
                
                val id = repository.insertPieceOrTechnique(
                    PieceOrTechnique(
                        name = normalizedName,
                        type = type,
                        isFavorite = false
                    )
                )
                
                // Check for first piece/technique achievements
                val achievementType = if (type == ItemType.PIECE) {
                    AchievementType.FIRST_PIECE
                } else {
                    AchievementType.FIRST_TECHNIQUE
                }
                
                if (!achievementManager.isAchievementUnlocked(achievementType)) {
                    achievementManager.unlockAchievement(achievementType)
                    
                    // Trigger celebration for any achievement unlock
                    _showCelebration.value = achievementType
                }
                
                // Track analytics for piece addition during activity creation
                val newPieceCount = repository.getAllPiecesAndTechniques().first().size
                analyticsManager.trackPieceAdded(
                    pieceType = type,
                    totalPieceCount = newPieceCount,
                    source = "during_activity_creation"
                )
                
                onComplete(id)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add task: ${e.message}"
            }
        }
    }
    
    fun saveActivity(
        pieceId: Long,
        activityType: ActivityType,
        level: Int,
        performanceType: String,
        minutes: Int,
        notes: String
    ) {
        // Use pre-populated date from calendar if available, otherwise use current time
        val timestamp = EditActivityStorage.getPrePopulatedDate() ?: System.currentTimeMillis()
        // Clear pre-populated date after using it
        EditActivityStorage.clearPrePopulatedDate()
        saveActivity(pieceId, activityType, level, performanceType, minutes, notes, timestamp)
    }
    
    fun saveActivity(
        pieceId: Long,
        activityType: ActivityType,
        level: Int,
        performanceType: String,
        minutes: Int,
        notes: String,
        timestamp: Long
    ) {
        viewModelScope.launch {
            try {
                // Check activity limit before adding
                val currentActivityCount = repository.getActivityCount()
                val activityLimit = proUserManager.getActivityLimit()
                
                if (currentActivityCount >= activityLimit) {
                    _errorMessage.value = "You have reached the activity limit of $activityLimit activities. Cannot add more activities."
                    return@launch
                }
                
                // Get piece information for analytics
                val piece = repository.getPieceOrTechniqueById(pieceId)
                
                repository.insertActivity(
                    Activity(
                        timestamp = timestamp,
                        pieceOrTechniqueId = pieceId,
                        activityType = activityType,
                        level = level,
                        performanceType = performanceType,
                        minutes = minutes,
                        notes = TextNormalizer.normalizeUserInput(notes)
                    )
                )
                
                // Check for first activity achievements
                checkFirstActivityAchievements(activityType, performanceType)
                
                // Track analytics event for activity logged
                piece?.let {
                    analyticsManager.trackActivityLogged(
                        activityType = activityType,
                        pieceType = it.type,
                        hasDuration = minutes > 0,
                        source = "main_flow"
                    )
                }
                
                // Check for streak achievements after activity is added
                val newStreak = repository.calculateCurrentStreak()
                trackStreakAchievement(newStreak)
                
                _navigateToMain.value = true
            } catch (e: Exception) {
                // Record crash context for activity creation error
                crashlyticsManager.setCustomKey("activity_type", activityType.name)
                crashlyticsManager.setCustomKey("piece_id", pieceId.toInt())
                crashlyticsManager.setCustomKey("activity_level", level)
                crashlyticsManager.setCustomKey("activity_minutes", minutes)
                crashlyticsManager.recordDatabaseError("insert_activity", e)
                // Re-throw to let UI handle the error
                throw e
            }
        }
    }
    
    fun setEditMode(activity: Activity) {
        _editMode.value = true
        _editActivity.value = activity
    }
    
    fun updateActivity(
        activityId: Long,
        pieceId: Long,
        activityType: ActivityType,
        level: Int,
        performanceType: String,
        minutes: Int,
        notes: String,
        timestamp: Long
    ) {
        viewModelScope.launch {
            repository.updateActivity(
                Activity(
                    id = activityId,
                    timestamp = timestamp,
                    pieceOrTechniqueId = pieceId,
                    activityType = activityType,
                    level = level,
                    performanceType = performanceType,
                    minutes = minutes,
                    notes = TextNormalizer.normalizeUserInput(notes)
                )
            )
            _navigateToMain.value = true
        }
    }
    
    fun doneNavigating() {
        _navigateToMain.value = false
        clearEditMode()
        EditActivityStorage.clearEditActivity()
    }
    
    fun clearEditMode() {
        _editMode.value = false
        _editActivity.value = null
    }
    
    /**
     * Check for first activity achievements
     */
    private suspend fun checkFirstActivityAchievements(activityType: ActivityType, performanceType: String) {
        when (activityType) {
            ActivityType.PRACTICE -> {
                if (!achievementManager.isAchievementUnlocked(AchievementType.FIRST_PRACTICE)) {
                    achievementManager.unlockAchievement(AchievementType.FIRST_PRACTICE)
                    _showCelebration.value = AchievementType.FIRST_PRACTICE
                }
            }
            ActivityType.PERFORMANCE -> {
                if (!achievementManager.isAchievementUnlocked(AchievementType.FIRST_PERFORMANCE)) {
                    achievementManager.unlockAchievement(AchievementType.FIRST_PERFORMANCE)
                    _showCelebration.value = AchievementType.FIRST_PERFORMANCE
                }
                
                // Check for specific performance type achievements
                when (performanceType.lowercase()) {
                    "online" -> {
                        if (!achievementManager.isAchievementUnlocked(AchievementType.FIRST_ONLINE_PERFORMANCE)) {
                            achievementManager.unlockAchievement(AchievementType.FIRST_ONLINE_PERFORMANCE)
                            _showCelebration.value = AchievementType.FIRST_ONLINE_PERFORMANCE
                        }
                    }
                    "live" -> {
                        if (!achievementManager.isAchievementUnlocked(AchievementType.FIRST_LIVE_PERFORMANCE)) {
                            achievementManager.unlockAchievement(AchievementType.FIRST_LIVE_PERFORMANCE)
                            _showCelebration.value = AchievementType.FIRST_LIVE_PERFORMANCE
                        }
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
            if (!achievementManager.isAchievementUnlocked(type)) {
                achievementManager.unlockAchievement(type)
                _showCelebration.value = type
            }
        }
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    /**
     * Get the celebration manager for showing achievement celebrations
     */
    fun getCelebrationManager(): AchievementCelebrationManager {
        return achievementCelebrationManager
    }
    
    /**
     * Reset celebration event after it's been handled
     */
    fun onCelebrationHandled() {
        _showCelebration.value = null
    }
}

class AddActivityViewModelFactory(
    private val repository: PianoRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddActivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddActivityViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
