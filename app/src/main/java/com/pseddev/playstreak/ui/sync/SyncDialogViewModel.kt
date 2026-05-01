package com.pseddev.mystreak.ui.sync

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pseddev.mystreak.data.models.SyncOperationResult
import com.pseddev.mystreak.data.repository.PianoRepository
import com.pseddev.mystreak.ui.importexport.Event
import com.pseddev.mystreak.utils.GoogleDriveHelper
import com.pseddev.mystreak.utils.SyncManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SyncDialogViewModel(
    private val repository: PianoRepository,
    private val context: Context
) : ViewModel() {

    private val driveHelper = GoogleDriveHelper(context)
    private val syncManager = SyncManager(context, repository, driveHelper)

    private val _syncResult = MutableLiveData<Event<SyncOperationResult>>()
    val syncResult: LiveData<Event<SyncOperationResult>> = _syncResult

    fun shouldShowSyncDialog(): Boolean {
        // Show dialog if:
        // 1. User is signed in to Google Drive
        // 2. Sync is enabled
        // 3. It's been more than 24 hours since last sync (or never synced)

        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account == null) return false

        if (!syncManager.isSyncEnabled()) return false

        val lastSyncTime = syncManager.getLastSyncTime()
        val currentTime = System.currentTimeMillis()
        val twentyFourHoursInMillis = 24 * 60 * 60 * 1000

        return lastSyncTime == 0L || (currentTime - lastSyncTime) > twentyFourHoursInMillis
    }

    fun performStartupSync() {
        viewModelScope.launch {
            try {
                // Initialize Drive service
                val account = GoogleSignIn.getLastSignedInAccount(context)
                if (account != null) {
                    withContext(Dispatchers.IO) {
                        driveHelper.initializeDriveService(account)
                    }

                    // Perform smart sync (will decide whether to upload or download)
                    val result = withContext(Dispatchers.IO) {
                        syncManager.performSmartSync()
                    }

                    _syncResult.value = Event(result)
                } else {
                    _syncResult.value = Event(
                        SyncOperationResult(
                            success = false,
                            message = "Not signed in to Google Drive"
                        )
                    )
                }
            } catch (e: Exception) {
                _syncResult.value = Event(
                    SyncOperationResult(
                        success = false,
                        message = "Sync failed: ${e.message}"
                    )
                )
            }
        }
    }
}

class SyncDialogViewModelFactory(
    private val repository: PianoRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SyncDialogViewModel::class.java)) {
            return SyncDialogViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}