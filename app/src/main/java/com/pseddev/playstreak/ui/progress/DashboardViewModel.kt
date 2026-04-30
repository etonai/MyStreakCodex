package com.pseddev.playstreak.ui.progress

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.pseddev.playstreak.data.entities.Activity
import com.pseddev.playstreak.data.repository.PianoRepository
import com.pseddev.playstreak.utils.ProUserManager
import com.pseddev.playstreak.utils.StreakCalculator
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar

class DashboardViewModel(
    private val repository: PianoRepository,
    private val context: android.content.Context
) : ViewModel() {

    private val proUserManager = ProUserManager.getInstance(context)
    private val suggestionsService = SuggestionsService(proUserManager)
    private val streakCalculator = StreakCalculator()

    private val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private val todayEnd = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis

    private val yesterdayStart = todayStart - 24 * 60 * 60 * 1000
    private val yesterdayEnd = todayEnd - 24 * 60 * 60 * 1000

    private val sevenDaysAgoStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.DAY_OF_YEAR, -6)
    }.timeInMillis

    val todayActivities: LiveData<List<ActivityWithPiece>> =
        combine(
            repository.getActivitiesForDateRange(todayStart, todayEnd),
            repository.getAllPiecesAndTechniques()
        ) { activities, pieces ->
            activities.mapNotNull { activity ->
                val piece = pieces.find { it.id == activity.pieceOrTechniqueId }
                piece?.let { ActivityWithPiece(activity, it) }
            }.sortedBy { it.activity.timestamp }
        }.asLiveData()

    val yesterdayActivities: LiveData<List<ActivityWithPiece>> =
        combine(
            repository.getActivitiesForDateRange(yesterdayStart, yesterdayEnd),
            repository.getAllPiecesAndTechniques()
        ) { activities, pieces ->
            activities.mapNotNull { activity ->
                val piece = pieces.find { it.id == activity.pieceOrTechniqueId }
                piece?.let { ActivityWithPiece(activity, it) }
            }.sortedBy { it.activity.timestamp }
        }.asLiveData()

    val weekSummary: LiveData<String> = repository.getRollingWeekSummaryText().asLiveData()

    val highPriorityOutstanding: LiveData<List<String>> =
        repository.getHighPriorityOutstandingTasksForToday()
            .map { tasks -> tasks.map { it.name } }
            .asLiveData()

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

    val currentStreak: LiveData<Int> = repository.getAllActivities()
        .map { activities ->
            streakCalculator.calculateCurrentStreak(activities)
        }
        .asLiveData()

    suspend fun calculateStreak(): Int = repository.calculateCurrentStreak()

    fun deleteActivity(activity: Activity) {
        viewModelScope.launch {
            repository.deleteActivity(activity)
        }
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
