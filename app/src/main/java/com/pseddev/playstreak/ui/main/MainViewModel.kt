package com.pseddev.mystreak.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.pseddev.mystreak.data.repository.PianoRepository
import com.pseddev.mystreak.data.entities.ItemType
import com.pseddev.mystreak.utils.StreakCalculator
import kotlinx.coroutines.flow.map

class MainViewModel(private val repository: PianoRepository) : ViewModel() {

    private val streakCalculator = StreakCalculator()

    val currentStreak: LiveData<Int> = repository.getAllActivities()
        .map { activities ->
            streakCalculator.calculateCurrentStreak(activities)
        }
        .asLiveData()

    val favoritesCount: LiveData<Int> = repository.getFavorites()
        .map { favorites ->
            favorites.size
        }
        .asLiveData()

    val piecesCount: LiveData<Int> = repository.getAllPiecesAndTechniques()
        .map { items ->
            items.size  // Count both pieces and techniques for limit display
        }
        .asLiveData()

    val activitiesCount: LiveData<Int> = repository.getAllActivities()
        .map { activities ->
            activities.size
        }
        .asLiveData()
}

class MainViewModelFactory(private val repository: PianoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}