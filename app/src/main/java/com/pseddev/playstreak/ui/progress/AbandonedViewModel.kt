package com.pseddev.mystreak.ui.progress

import androidx.lifecycle.*
import com.pseddev.mystreak.data.entities.ItemType
import com.pseddev.mystreak.data.entities.PieceOrTechnique
import com.pseddev.mystreak.data.repository.PianoRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*

enum class AbandonedSortDirection {
    ASCENDING,
    DESCENDING
}

data class AbandonedItem(
    val piece: PieceOrTechnique,
    val lastActivityDate: Long?,
    val daysSinceLastActivity: Int
)

class AbandonedViewModel(private val repository: PianoRepository) : ViewModel() {

    private val now = System.currentTimeMillis()
    private val thirtyOneDaysAgo = now - (31 * 24 * 60 * 60 * 1000L)
    private val sortDirection = MutableStateFlow(AbandonedSortDirection.DESCENDING)

    val abandonedPieces: LiveData<List<AbandonedItem>> =
        combine(
            repository.getAllPiecesAndTechniques(),
            repository.getAllActivities(),
            sortDirection
        ) { pieces, activities, currentSortDirection ->
            val pieceActivities = activities.groupBy { it.pieceOrTechniqueId }

            val abandoned = mutableListOf<AbandonedItem>()

            pieces.filter {
                it.type == ItemType.PIECE && !it.isFavorite
            }.forEach { piece ->
                val pieceActivitiesForPiece = pieceActivities[piece.id] ?: emptyList()
                val lastActivity = pieceActivitiesForPiece.maxByOrNull { it.timestamp }
                val lastActivityDate = lastActivity?.timestamp

                val daysSince = if (lastActivityDate != null) {
                    ((now - lastActivityDate) / (24 * 60 * 60 * 1000)).toInt()
                } else {
                    Int.MAX_VALUE
                }

                // Include pieces that haven't been practiced in 31+ days (or never)
                if (lastActivityDate == null || lastActivityDate < thirtyOneDaysAgo) {
                    abandoned.add(
                        AbandonedItem(
                            piece = piece,
                            lastActivityDate = lastActivityDate,
                            daysSinceLastActivity = daysSince
                        )
                    )
                }
            }

            // Sort: pieces with activity first (most recent first), then never-practiced pieces (alphabetical)
            val sorted = abandoned.sortedWith(
                compareBy<AbandonedItem> { it.lastActivityDate == null }
                    .thenByDescending { it.lastActivityDate ?: 0 }
                    .thenBy { it.piece.name.lowercase() }
            )

            // Apply sort direction
            if (currentSortDirection == AbandonedSortDirection.DESCENDING) {
                sorted.reversed()
            } else {
                sorted
            }
        }
        .asLiveData()

    fun toggleSortDirection() {
        sortDirection.value = if (sortDirection.value == AbandonedSortDirection.ASCENDING) {
            AbandonedSortDirection.DESCENDING
        } else {
            AbandonedSortDirection.ASCENDING
        }
    }

    fun getCurrentSortDirection(): AbandonedSortDirection = sortDirection.value
}

class AbandonedViewModelFactory(private val repository: PianoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AbandonedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AbandonedViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}