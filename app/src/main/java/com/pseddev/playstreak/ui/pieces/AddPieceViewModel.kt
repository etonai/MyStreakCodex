package com.pseddev.playstreak.ui.pieces

import android.content.Context
import androidx.lifecycle.*
import com.pseddev.playstreak.analytics.AnalyticsManager
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.data.entities.PieceOrTechnique
import com.pseddev.playstreak.data.entities.TaskPriority
import com.pseddev.playstreak.data.repository.PianoRepository
import com.pseddev.playstreak.utils.ProUserManager
import com.pseddev.playstreak.utils.TextNormalizer
import com.pseddev.playstreak.utils.AchievementManager
import com.pseddev.playstreak.utils.AchievementCelebrationManager
import com.pseddev.playstreak.data.entities.AchievementType
import com.pseddev.playstreak.utils.AchievementDefinitions
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

sealed class AddPieceResult {
    object Success : AddPieceResult()
    data class Error(val message: String) : AddPieceResult()
    data class PieceLimitReached(val currentCount: Int, val limit: Int, val isProUser: Boolean) : AddPieceResult()
    object DuplicateName : AddPieceResult()
}

class AddPieceViewModel(
    private val repository: PianoRepository,
    private val context: Context
) : ViewModel() {
    
    private val proUserManager = ProUserManager.getInstance(context)
    private val analyticsManager = AnalyticsManager(context)
    private val achievementManager = AchievementManager(context, repository)
    private val achievementCelebrationManager = AchievementCelebrationManager(context)
    private val _saveResult = MutableLiveData<AddPieceResult>()
    val saveResult: LiveData<AddPieceResult> = _saveResult
    
    private val _canAddFavorites = MutableLiveData<Boolean>()
    val canAddFavorites: LiveData<Boolean> = _canAddFavorites
    
    private val _showCelebration = MutableLiveData<AchievementType?>()
    val showCelebration: LiveData<AchievementType?> = _showCelebration
    
    init {
        checkFavoritesLimit()
    }
    
    private fun checkFavoritesLimit() {
        viewModelScope.launch {
            try {
                val currentFavoriteCount = repository.getAllPiecesAndTechniques().first()
                    .count { it.isFavorite }
                val canAdd = proUserManager.canAddMoreFavorites(currentFavoriteCount)
                _canAddFavorites.value = canAdd
            } catch (e: Exception) {
                // In case of error, assume they can add favorites to avoid blocking functionality
                _canAddFavorites.value = true
            }
        }
    }
    
    fun savePiece(name: String, type: ItemType, isFavorite: Boolean) {
        saveTask(
            name = name,
            color = "#66B2FF",
            priority = if (isFavorite) TaskPriority.HIGH else TaskPriority.LOW,
            minimumSuccess = "Minimum",
            mediumSuccess = "Medium",
            highSuccess = "High",
            isActive = true
        )
    }

    fun saveTask(
        name: String,
        color: String,
        priority: TaskPriority,
        minimumSuccess: String,
        mediumSuccess: String,
        highSuccess: String,
        isActive: Boolean
    ) {
        viewModelScope.launch {
            try {
                // Check for duplicate name first (case-insensitive)
                val normalizedName = TextNormalizer.normalizePieceName(name)
                if (repository.doesPieceNameExist(normalizedName)) {
                    _saveResult.value = AddPieceResult.DuplicateName
                    return@launch
                }
                
                // Check piece limit before saving
                val currentPieceCount = repository.getAllPiecesAndTechniques().first().size
                val limit = proUserManager.getPieceLimit()
                
                // Debug logging
                android.util.Log.d("AddPieceViewModel", "Current piece count: $currentPieceCount, Limit: $limit, Can add more: ${proUserManager.canAddMorePieces(currentPieceCount)}")
                
                if (!proUserManager.canAddMorePieces(currentPieceCount)) {
                    val limit = proUserManager.getPieceLimit()
                    _saveResult.value = AddPieceResult.PieceLimitReached(
                        currentCount = currentPieceCount,
                        limit = limit,
                        isProUser = proUserManager.isProUser()
                    )
                    return@launch
                }
                
                val piece = PieceOrTechnique(
                    name = normalizedName,
                    color = color,
                    priority = priority,
                    minimumSuccess = minimumSuccess,
                    mediumSuccess = mediumSuccess,
                    highSuccess = highSuccess,
                    isActive = isActive
                )
                
                repository.insertPieceOrTechnique(piece)
                
                // Check for first piece/technique achievements
                val achievementType = AchievementType.FIRST_PIECE
                
                if (!achievementManager.isAchievementUnlocked(achievementType)) {
                    achievementManager.unlockAchievement(achievementType)
                    
                    // Trigger celebration for any achievement unlock
                    _showCelebration.value = achievementType
                }
                
                // Track analytics for piece addition
                val newPieceCount = repository.getAllPiecesAndTechniques().first().size
                analyticsManager.trackPieceAdded(
                    pieceType = ItemType.PIECE,
                    totalPieceCount = newPieceCount,
                    source = "pieces_tab"
                )
                
                _saveResult.value = AddPieceResult.Success
                
            } catch (e: Exception) {
                _saveResult.value = AddPieceResult.Error("Failed to save piece: ${e.message}")
            }
        }
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

class AddPieceViewModelFactory(
    private val repository: PianoRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddPieceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddPieceViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
