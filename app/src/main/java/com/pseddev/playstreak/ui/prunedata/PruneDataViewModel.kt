package com.pseddev.mystreak.ui.prunedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.pseddev.mystreak.analytics.AnalyticsManager
import com.pseddev.mystreak.data.repository.PianoRepository
import com.pseddev.mystreak.utils.ConfigurationManager
import com.pseddev.mystreak.utils.ProUserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Result types for pruning operations
sealed class PruningResult {
    data class Success(val deletedCount: Int) : PruningResult()
    object NoActivitiesToPrune : PruningResult()
    data class Error(val message: String) : PruningResult()
}

// Data class for activity counts
data class ActivityCounts(
    val stored: Int,
    val lifetime: Int,
    val maximum: Int
)

class PruneDataViewModel(
    private val repository: PianoRepository,
    private val context: android.content.Context
) : ViewModel() {

    private val configurationManager = ConfigurationManager.getInstance(context)
    private val proUserManager = ProUserManager.getInstance(context)
    private val analyticsManager = AnalyticsManager(context)

    private val _activityCounts = MutableLiveData<ActivityCounts>()
    val activityCounts: LiveData<ActivityCounts> = _activityCounts

    // Backward compatibility for existing code
    val activityCount: LiveData<Int> = _activityCounts.map { it.stored }

    private val _pruningInProgress = MutableLiveData<Boolean>()
    val pruningInProgress: LiveData<Boolean> = _pruningInProgress

    private val _pruningResult = MutableLiveData<PruningResult?>()
    val pruningResult: LiveData<PruningResult?> = _pruningResult

    init {
        loadActivityCount()
    }

    private fun loadActivityCount() {
        viewModelScope.launch {
            try {
                val storedCount = repository.getTotalActivityCount()

                // Initialize lifetime counter for existing users if needed
                configurationManager.initializeLifetimeCounter(storedCount)

                val lifetimeCount = configurationManager.getLifetimeActivityCount()
                val maximumCount = proUserManager.getActivityLimit()

                _activityCounts.value = ActivityCounts(
                    stored = storedCount,
                    lifetime = maxOf(lifetimeCount, storedCount), // Ensure lifetime >= stored
                    maximum = maximumCount
                )
            } catch (e: Exception) {
                android.util.Log.e("PruneDataViewModel", "Error loading activity count", e)
                _activityCounts.value = ActivityCounts(stored = 0, lifetime = 0, maximum = 0)
            }
        }
    }

    fun pruneOldestActivities(count: Int = 100) {
        viewModelScope.launch {
            _pruningInProgress.value = true
            _pruningResult.value = null

            try {
                val result = withContext(Dispatchers.IO) {
                    // Get oldest activities by timestamp
                    val oldestActivities = repository.getOldestActivities(count)

                    if (oldestActivities.isEmpty()) {
                        return@withContext PruningResult.NoActivitiesToPrune
                    }

                    // Delete activities WITHOUT updating piece statistics
                    val deletedCount = repository.deleteActivitiesWithoutStatsUpdate(
                        oldestActivities.map { it.id }
                    )

                    PruningResult.Success(deletedCount)
                }

                _pruningResult.value = result

                // Track analytics for data pruning operation
                when (result) {
                    is PruningResult.Success -> {
                        analyticsManager.trackDataPruning(
                            deletedCount = result.deletedCount,
                            success = true
                        )
                        // Refresh activity count after successful pruning
                        loadActivityCount()
                    }
                    is PruningResult.NoActivitiesToPrune -> {
                        analyticsManager.trackDataPruning(
                            deletedCount = 0,
                            success = true
                        )
                    }
                    is PruningResult.Error -> {
                        analyticsManager.trackDataPruning(
                            deletedCount = 0,
                            success = false
                        )
                    }
                }

            } catch (e: Exception) {
                android.util.Log.e("PruneDataViewModel", "Error during pruning operation", e)
                _pruningResult.value = PruningResult.Error(e.message ?: "Unknown error occurred")

                // Track failed pruning operation
                analyticsManager.trackDataPruning(
                    deletedCount = 0,
                    success = false
                )
            } finally {
                _pruningInProgress.value = false
            }
        }
    }

    fun clearPruningResult() {
        _pruningResult.value = null
    }
}

class PruneDataViewModelFactory(
    private val repository: PianoRepository,
    private val context: android.content.Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PruneDataViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PruneDataViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}