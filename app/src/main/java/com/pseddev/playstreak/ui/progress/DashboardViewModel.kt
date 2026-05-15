package com.pseddev.mystreak.ui.progress

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.pseddev.mystreak.data.entities.Activity
import com.pseddev.mystreak.data.entities.TaskKind
import com.pseddev.mystreak.data.entities.TaskPriority
import com.pseddev.mystreak.data.repository.PianoRepository
import com.pseddev.mystreak.utils.ProUserManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val repository: PianoRepository,
    private val context: android.content.Context
) : ViewModel() {

    private val proUserManager = ProUserManager.getInstance(context)
    private val suggestionsService = SuggestionsService(proUserManager)

    private val todayStartMillis = MutableStateFlow(startOfDay(System.currentTimeMillis()))

    val todayActivities: LiveData<List<ActivityWithPiece>> =
        todayStartMillis.flatMapLatest { todayStart ->
            combine(
                repository.getActivitiesForDateRange(todayStart, todayStart + DAY_MILLIS),
                repository.getAllPiecesAndTechniques()
            ) { activities, pieces ->
                activities.mapNotNull { activity ->
                    val piece = pieces.find { it.id == activity.pieceOrTechniqueId }
                    piece?.let { ActivityWithPiece(activity, it) }
                }.sortedBy { it.activity.timestamp }
            }
        }.asLiveData()

    val yesterdayActivities: LiveData<List<ActivityWithPiece>> =
        todayStartMillis.flatMapLatest { todayStart ->
            val yesterdayStart = todayStart - DAY_MILLIS
            combine(
                repository.getActivitiesForDateRange(yesterdayStart, todayStart),
                repository.getAllPiecesAndTechniques()
            ) { activities, pieces ->
                activities.mapNotNull { activity ->
                    val piece = pieces.find { it.id == activity.pieceOrTechniqueId }
                    piece?.let { ActivityWithPiece(activity, it) }
                }.sortedBy { it.activity.timestamp }
            }
        }.asLiveData()

    val weekSummary: LiveData<String> =
        todayStartMillis.flatMapLatest { todayStart ->
            combine(
                repository.getActivitiesForDateRange(todayStart - (6 * DAY_MILLIS), todayStart + DAY_MILLIS),
                repository.getAllPiecesAndTechniques()
            ) { activities, tasks ->
                val activeTaskIds = tasks.filter { it.isActive }.map { it.id }.toSet()
                val highPriorityTaskIds = tasks
                    .filter { it.isActive && it.taskKind == TaskKind.STANDARD && it.priority == TaskPriority.HIGH }
                    .map { it.id }
                    .toSet()
                val activeActivities = activities.filter { it.taskId in activeTaskIds }
                val highPriorityActivities = activeActivities.count { it.taskId in highPriorityTaskIds }

                buildString {
                    append("- ${activeActivities.size} activit${if (activeActivities.size != 1) "ies" else "y"} logged\n")
                    append("- $highPriorityActivities high priority activit${if (highPriorityActivities != 1) "ies" else "y"} logged")
                }
            }
        }.asLiveData()

    val highPriorityOutstanding: LiveData<List<String>> =
        todayStartMillis.flatMapLatest { todayStart ->
            combine(
                repository.getActiveHighPriorityTasks(),
                repository.getActivitiesForDateRange(todayStart, todayStart + DAY_MILLIS)
            ) { highPriorityTasks, todayActivities ->
                val completedTaskIds = todayActivities.map { it.taskId }.toSet()
                highPriorityTasks.filter { it.id !in completedTaskIds }.map { it.name }
            }
        }.asLiveData()

    val performanceSuggestions: LiveData<List<SuggestionItem>> =
        repository.getAllPiecesAndTechniques()
            .combine(repository.getAllActivities()) { pieces, activities ->
                suggestionsService.generatePerformanceSuggestions(pieces, activities)
            }
            .asLiveData()

    val suggestions: LiveData<List<SuggestionItem>> =
        repository.getAllPiecesAndTechniques()
            .combine(repository.getAllActivities()) { pieces, activities ->
                val practiceSuggestions = suggestionsService.generatePracticeSuggestions(pieces, activities)
                val dashboardFavoriteLimit = if (proUserManager.isProUser())
                    ProUserManager.PRO_USER_PRACTICE_FAVORITE_SUGGESTIONS
                else ProUserManager.FREE_USER_PRACTICE_FAVORITE_SUGGESTIONS
                val dashboardNonFavoriteLimit = if (proUserManager.isProUser())
                    ProUserManager.PRO_USER_PRACTICE_NON_FAVORITE_SUGGESTIONS
                else ProUserManager.FREE_USER_PRACTICE_NON_FAVORITE_SUGGESTIONS

                val favoritesPractice = practiceSuggestions.filter { it.piece.isFavorite }.take(dashboardFavoriteLimit)
                val nonFavoritesPractice = practiceSuggestions.filter { !it.piece.isFavorite }.take(dashboardNonFavoriteLimit)

                favoritesPractice + nonFavoritesPractice
            }
            .asLiveData()

    val currentStreak: LiveData<Int> = combine(
        repository.getAllActivities(),
        repository.getAllPiecesAndTechniques(),
        todayStartMillis
    ) { activities, tasks, _ ->
        repository.calculateCurrentStreak(activities, tasks)
    }.asLiveData()

    init {
        viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val nextDayStart = startOfDay(now) + DAY_MILLIS
                delay((nextDayStart - now).coerceAtLeast(1L) + 1000L)
                refreshDateRanges()
            }
        }
    }

    fun refreshDateRanges() {
        val currentTodayStart = startOfDay(System.currentTimeMillis())
        if (todayStartMillis.value != currentTodayStart) {
            todayStartMillis.value = currentTodayStart
        }
    }

    suspend fun calculateStreak(): Int = repository.calculateCurrentStreak()

    fun deleteActivity(activity: Activity) {
        viewModelScope.launch {
            repository.deleteActivity(activity)
        }
    }

    private fun startOfDay(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private companion object {
        const val DAY_MILLIS = 24L * 60L * 60L * 1000L
    }
}

class DashboardViewModelFactory(
    private val repository: PianoRepository,
    private val context: android.content.Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
