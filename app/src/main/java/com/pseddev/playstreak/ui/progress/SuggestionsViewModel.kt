package com.pseddev.mystreak.ui.progress

import androidx.lifecycle.*
import com.pseddev.mystreak.data.entities.ActivityType
import com.pseddev.mystreak.data.entities.ItemType
import com.pseddev.mystreak.data.entities.PieceOrTechnique
import com.pseddev.mystreak.data.repository.PianoRepository
import com.pseddev.mystreak.utils.ProUserManager
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import java.util.*
import java.util.Calendar

data class SuggestionItem(
    val piece: PieceOrTechnique,
    val lastActivityDate: Long?,
    val daysSinceLastActivity: Int,
    val suggestionReason: String,
    val suggestionType: SuggestionType = SuggestionType.PRACTICE
)

enum class SuggestionType {
    PRACTICE,
    PERFORMANCE
}

class SuggestionsViewModel(
    private val repository: PianoRepository,
    private val context: android.content.Context
) : ViewModel() {

    private val proUserManager = ProUserManager.getInstance(context)
    private val suggestionsService = SuggestionsService(proUserManager)

    val practiceSuggestions: LiveData<List<SuggestionItem>> =
        repository.getAllPiecesAndTechniques()
            .combine(repository.getAllActivities()) { pieces, activities ->
                suggestionsService.generatePracticeSuggestions(pieces, activities)
            }
            .asLiveData()

    val performanceSuggestions: LiveData<List<SuggestionItem>> =
        repository.getAllPiecesAndTechniques()
            .combine(repository.getAllActivities()) { pieces, activities ->
                suggestionsService.generatePerformanceSuggestions(pieces, activities)
            }
            .asLiveData()
}

class SuggestionsViewModelFactory(
    private val repository: PianoRepository,
    private val context: android.content.Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SuggestionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SuggestionsViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}