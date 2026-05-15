package com.pseddev.mystreak.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.pseddev.mystreak.data.repository.PianoRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class MainViewModel(private val repository: PianoRepository) : ViewModel() {

    val currentStreak: LiveData<Int> =
        combine(
            repository.getAllActivities(),
            repository.getAllPiecesAndTechniques()
        ) { activities, tasks ->
            repository.calculateCurrentStreak(activities, tasks)
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
