package com.pseddev.playstreak.ui.favorites

import androidx.lifecycle.*
import com.pseddev.playstreak.data.entities.PieceOrTechnique
import com.pseddev.playstreak.data.entities.TaskPriority
import com.pseddev.playstreak.data.repository.PianoRepository
import com.pseddev.playstreak.utils.ProUserManager
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: PianoRepository,
    private val context: android.content.Context
) : ViewModel() {
    
    private val proUserManager = ProUserManager.getInstance(context)
    
    val allPiecesAndTechniques: LiveData<List<PieceOrTechnique>> = 
        repository.getAllPiecesAndTechniques().asLiveData()
    
    fun toggleFavorite(piece: PieceOrTechnique): Boolean {
        val currentlyFavorite = piece.isFavorite
        
        // If trying to add a favorite (not currently favorite), check limits for Free users
        if (!currentlyFavorite) {
            // Get current favorite count from the live data
            val currentFavoriteCount = allPiecesAndTechniques.value?.count { it.isFavorite } ?: 0
            
            if (!proUserManager.canAddMoreFavorites(currentFavoriteCount)) {
                return false // Cannot add more favorites - caller should show upgrade prompt
            }
        }
        
        // Proceed with toggle (either removing favorite or adding within limits)
        viewModelScope.launch {
            val updatedPiece = piece.copy(priority = if (currentlyFavorite) TaskPriority.LOW else TaskPriority.HIGH)
            repository.updatePieceOrTechnique(updatedPiece)
        }
        
        return true // Toggle was allowed and performed
    }
}

class FavoritesViewModelFactory(
    private val repository: PianoRepository,
    private val context: android.content.Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoritesViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
