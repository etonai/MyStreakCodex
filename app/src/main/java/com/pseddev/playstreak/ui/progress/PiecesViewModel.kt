package com.pseddev.mystreak.ui.progress

import androidx.lifecycle.*
import com.pseddev.mystreak.data.entities.Activity
import com.pseddev.mystreak.data.entities.ItemType
import com.pseddev.mystreak.data.entities.TaskPriority
import com.pseddev.mystreak.data.entities.TaskKind
import com.pseddev.mystreak.data.entities.PieceOrTechnique
import com.pseddev.mystreak.data.repository.PianoRepository
import com.pseddev.mystreak.utils.ProUserManager
import com.pseddev.mystreak.utils.TaskColors
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.util.Calendar

enum class SortType {
    ALPHABETICAL,
    LAST_DATE,
    ACTIVITY_COUNT,
    PRIORITY
}

enum class SortDirection {
    ASCENDING,
    DESCENDING
}

data class PieceWithStats(
    val piece: PieceOrTechnique,
    val activityCount: Int,
    val todayActivityCount: Int,
    val lastActivityDate: Long?
)

data class PieceDetails(
    val piece: PieceOrTechnique,
    val activities: List<Activity>,
    val lastActivity: Activity?
)

@OptIn(ExperimentalCoroutinesApi::class)
class PiecesViewModel(
    private val repository: PianoRepository,
    private val context: android.content.Context
) : ViewModel() {

    private val proUserManager = ProUserManager.getInstance(context)

    private val selectedPieceId = MutableStateFlow<Long?>(null)
    private val taskKindFilter = MutableStateFlow(TaskKind.STANDARD)
    private val sortType = MutableStateFlow(SortType.PRIORITY)
    private val sortDirection = MutableStateFlow(SortDirection.ASCENDING)
    private val todayStart: Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    private val todayEnd: Long = todayStart + 24L * 60L * 60L * 1000L

    val piecesWithStats: LiveData<List<PieceWithStats>> =
        combine(
            repository.getAllPiecesAndTechniques(),
            repository.getAllActivities(),
            taskKindFilter,
            sortType,
            sortDirection
        ) { pieces, activities, currentTaskKind, currentSortType, currentSortDirection ->
            val items = pieces
                .filter { it.taskKind == currentTaskKind }
                .map { piece ->
                    val pieceActivities = activities.filter { it.pieceOrTechniqueId == piece.id }
                    val todayCount = pieceActivities.count {
                        it.timestamp >= todayStart && it.timestamp < todayEnd
                    }

                    PieceWithStats(
                        piece = piece,
                        activityCount = pieceActivities.size,
                        todayActivityCount = todayCount,
                        lastActivityDate = pieceActivities.maxOfOrNull { it.timestamp }
                    )
                }

            // Apply sorting
            val sorted = when (currentSortType) {
                SortType.ALPHABETICAL -> items.sortedBy { it.piece.name.lowercase() }
                SortType.LAST_DATE -> items.sortedBy { it.lastActivityDate ?: 0L }
                SortType.ACTIVITY_COUNT -> items.sortedBy { it.activityCount }
                SortType.PRIORITY -> {
                    val nameAndIdOrder = compareBy<PieceWithStats>({ it.piece.name.lowercase() }, { it.piece.id })
                    val highItems = items.filter { it.piece.priority == TaskPriority.HIGH }.sortedWith(nameAndIdOrder)
                    val lowItems = items.filter { it.piece.priority == TaskPriority.LOW }.sortedWith(nameAndIdOrder)
                    if (currentSortDirection == SortDirection.ASCENDING) highItems + lowItems else lowItems + highItems
                }
            }

            // Apply direction (Priority sort handles its own direction above)
            if (currentSortType != SortType.PRIORITY && currentSortDirection == SortDirection.DESCENDING) {
                sorted.reversed()
            } else {
                sorted
            }
        }
        .asLiveData()

    val selectedPieceDetails: LiveData<PieceDetails?> =
        selectedPieceId.flatMapLatest { pieceId ->
            if (pieceId == null) {
                kotlinx.coroutines.flow.flowOf(null)
            } else {
                // Combine piece data and activities to ensure we get the latest statistics
                combine(
                    repository.getAllPiecesAndTechniques(),
                    repository.getActivitiesForPiece(pieceId)
                ) { allPieces, activities ->
                    val piece = allPieces.find { it.id == pieceId }
                    piece?.let {
                        PieceDetails(
                            piece = it,
                            activities = activities,
                            lastActivity = activities.maxByOrNull { it.timestamp }
                        )
                    }
                }
            }
        }
        .asLiveData()

    fun selectPiece(pieceId: Long) {
        selectedPieceId.value = pieceId
    }

    fun clearSelection() {
        selectedPieceId.value = null
    }

    fun setTaskKindFilter(taskKind: TaskKind) {
        taskKindFilter.value = taskKind
    }

    fun setSortType(type: SortType) {
        if (sortType.value == type) {
            return
        }

        sortType.value = type
        // Set appropriate default direction based on sort type
        sortDirection.value = when (type) {
            SortType.ALPHABETICAL -> SortDirection.ASCENDING  // A-Z makes sense
            SortType.LAST_DATE -> SortDirection.DESCENDING    // Newest first makes sense
            SortType.ACTIVITY_COUNT -> SortDirection.DESCENDING // Highest count first makes sense
            SortType.PRIORITY -> SortDirection.ASCENDING // High priority first by default
        }
    }

    fun toggleSortDirection() {
        sortDirection.value = if (sortDirection.value == SortDirection.ASCENDING) {
            SortDirection.DESCENDING
        } else {
            SortDirection.ASCENDING
        }
    }

    fun getCurrentSortType(): SortType = sortType.value
    fun getCurrentSortDirection(): SortDirection = sortDirection.value

    fun toggleFavorite(pieceWithStats: PieceWithStats): Boolean {
        val currentlyFavorite = pieceWithStats.piece.isFavorite

        // If trying to add a favorite (not currently favorite), check limits for Free users
        if (!currentlyFavorite) {
            // Get current favorite count from the live data
            val currentFavoriteCount = piecesWithStats.value?.count { it.piece.isFavorite } ?: 0

            if (!proUserManager.canAddMoreFavorites(currentFavoriteCount)) {
                return false // Cannot add more favorites - caller should show upgrade prompt
            }
        }

        // Proceed with toggle (either removing favorite or adding within limits)
        viewModelScope.launch {
            val updatedPiece = pieceWithStats.piece.copy(priority = if (currentlyFavorite) TaskPriority.LOW else TaskPriority.HIGH)
            repository.updatePieceOrTechnique(updatedPiece)
        }

        return true // Toggle was allowed and performed
    }

    fun deletePiece(pieceWithStats: PieceWithStats) {
        viewModelScope.launch {
            repository.deletePieceAndActivities(pieceWithStats.piece.id)
        }
    }

    fun updateTask(
        pieceId: Long,
        newName: String,
        color: String,
        priority: TaskPriority,
        taskKind: TaskKind,
        minimumSuccess: String,
        mediumSuccess: String,
        highSuccess: String,
        isActive: Boolean
    ) {
        viewModelScope.launch {
            try {
                val currentPiece = repository.getPieceOrTechniqueById(pieceId)
                currentPiece?.let { piece ->
                    val updatedPiece = piece.copy(
                        name = newName,
                        color = TaskColors.storedColorFor(taskKind, color),
                        priority = priority,
                        taskKind = taskKind,
                        minimumSuccess = minimumSuccess,
                        mediumSuccess = mediumSuccess,
                        highSuccess = highSuccess,
                        isActive = isActive,
                        lastUpdated = System.currentTimeMillis()
                    )
                    repository.updatePieceOrTechnique(updatedPiece)
                }
            } catch (e: Exception) {
                // Handle error - could emit to a LiveData for UI feedback
                android.util.Log.e("PiecesViewModel", "Error updating piece", e)
            }
        }
    }
}

class PiecesViewModelFactory(
    private val repository: PianoRepository,
    private val context: android.content.Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PiecesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PiecesViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
