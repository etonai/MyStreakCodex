package com.pseddev.mystreak.ui.achievements

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.pseddev.mystreak.data.AppDatabase
import com.pseddev.mystreak.data.entities.Achievement
import com.pseddev.mystreak.data.entities.AchievementType
import com.pseddev.mystreak.data.repository.PianoRepository
import com.pseddev.mystreak.utils.AchievementManager
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class AchievementCategory(
    val categoryName: String,
    val achievements: List<Achievement>
)

class AchievementsViewModel(
    private val repository: PianoRepository,
    private val context: Context
) : ViewModel() {

    private val database = AppDatabase.getDatabase(context)
    private val achievementDao = database.achievementDao()
    private val achievementManager = AchievementManager(context, repository)

    val categorizedAchievements: LiveData<List<AchievementCategory>> =
        achievementDao.getAllAchievements().map { achievements ->
            val firstActions = achievements.filter {
                it.type.name.startsWith("FIRST_")
            }.sortedBy { achievement ->
                // Sort in the specified order: First Steps, Skill Builder, Practice Makes Perfect,
                // Debut Performance, Digital Debut, Stage Presence
                when (achievement.type) {
                    AchievementType.FIRST_PIECE -> 1          // First Steps
                    AchievementType.FIRST_TECHNIQUE -> 2      // Skill Builder
                    AchievementType.FIRST_PRACTICE -> 3       // Practice Makes Perfect
                    AchievementType.FIRST_PERFORMANCE -> 4    // Debut Performance
                    AchievementType.FIRST_ONLINE_PERFORMANCE -> 5  // Digital Debut
                    AchievementType.FIRST_LIVE_PERFORMANCE -> 6    // Stage Presence
                    else -> 999 // fallback for any unexpected types
                }
            }
            val streakMilestones = achievements.filter {
                it.type.name.startsWith("STREAK_")
            }.sortedBy { achievement ->
                // Extract the number of days from STREAK_X_DAYS format for proper sorting
                val typeName = achievement.type.name
                val daysString = typeName.removePrefix("STREAK_").removeSuffix("_DAYS")
                daysString.toIntOrNull() ?: 0
            }

            listOf(
                AchievementCategory("First Actions", firstActions),
                AchievementCategory("Streak Milestones", streakMilestones)
            )
        }.asLiveData()

    val achievementCounts: LiveData<Pair<Int, Int>> =
        achievementDao.getAllAchievements().map { achievements ->
            val unlockedCount = achievements.count { it.isUnlocked }
            val totalCount = achievements.size
            Pair(unlockedCount, totalCount)
        }.asLiveData()

    init {
        // Initialize achievements system if needed
        viewModelScope.launch {
            achievementManager.initializeAchievements()
        }
    }
}

class AchievementsViewModelFactory(
    private val repository: PianoRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AchievementsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AchievementsViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}