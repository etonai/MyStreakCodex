package com.pseddev.mystreak.ui.importexport

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import com.pseddev.mystreak.BuildConfig
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pseddev.mystreak.analytics.AnalyticsManager
import com.pseddev.mystreak.crashlytics.CrashlyticsManager
import com.pseddev.mystreak.data.models.SyncOperationResult
import com.pseddev.mystreak.data.repository.PianoRepository
import com.pseddev.mystreak.utils.AchievementManager
import com.pseddev.mystreak.utils.CsvHandler
import com.pseddev.mystreak.utils.GoogleDriveHelper
import com.pseddev.mystreak.utils.ProUserManager
import com.pseddev.mystreak.utils.SyncManager
import com.pseddev.mystreak.data.models.JsonImportResult
import com.pseddev.mystreak.data.models.JsonValidationResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Reader
import java.io.Writer

class ImportExportViewModel(
    private val repository: PianoRepository,
    private val context: Context
) : ViewModel() {

    private val driveHelper = GoogleDriveHelper(context)
    private val syncManager = SyncManager(context, repository, driveHelper)
    private val proUserManager = ProUserManager.getInstance(context)
    private val crashlyticsManager = CrashlyticsManager(context)
    private val analyticsManager = AnalyticsManager(context)
    private val sharedPrefs = context.getSharedPreferences("play_streak_export_prefs", Context.MODE_PRIVATE)

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _exportResult = MutableLiveData<Event<Result<String>>>()
    val exportResult: LiveData<Event<Result<String>>> = _exportResult

    private val _importResult = MutableLiveData<Event<Result<CsvHandler.ImportResult>>>()
    val importResult: LiveData<Event<Result<CsvHandler.ImportResult>>> = _importResult

    private val _validationResult = MutableLiveData<Event<CsvHandler.CsvValidationResult>>()
    val validationResult: LiveData<Event<CsvHandler.CsvValidationResult>> = _validationResult

    private val _jsonImportResult = MutableLiveData<Event<Result<JsonImportResult>>>()
    val jsonImportResult: LiveData<Event<Result<JsonImportResult>>> = _jsonImportResult

    private val _jsonValidationResult = MutableLiveData<Event<JsonValidationResult>>()
    val jsonValidationResult: LiveData<Event<JsonValidationResult>> = _jsonValidationResult

    private val _purgeResult = MutableLiveData<Event<Result<String>>>()
    val purgeResult: LiveData<Event<Result<String>>> = _purgeResult

    private val _syncResult = MutableLiveData<Event<SyncOperationResult>>()
    val syncResult: LiveData<Event<SyncOperationResult>> = _syncResult

    private val _driveConnectionState = MutableLiveData<Boolean>()
    val driveConnectionState: LiveData<Boolean> = _driveConnectionState

    init {
        // Check initial sign-in state
        _driveConnectionState.value = isSignedIn()
    }

    suspend fun exportToCsv(writer: Writer) {
        if (BuildConfig.DEBUG) {
            Log.d("ExportDebug", "Starting CSV export")
        }
        _isLoading.value = true
        try {
            withContext(Dispatchers.IO) {
                repository.exportToCsv(writer)
            }
            // Update last export time
            sharedPrefs.edit().putLong("last_export_time", System.currentTimeMillis()).apply()
            _exportResult.value = Event(Result.success("CSV export successful!"))
        } catch (e: Exception) {
            Log.e("ExportDebug", "Exception in ViewModel: ${e.javaClass.simpleName} - ${e.message}", e)
            // Record crash context for CSV export error
            crashlyticsManager.recordCsvError("export", 0, e) // Activity count not available at this level
            // Add more detailed error information
            val errorMessage = "CSV export failed: ${e.javaClass.simpleName} - ${e.message}"
            _exportResult.value = Event(Result.failure(Exception(errorMessage, e)))
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun exportToJson(writer: Writer) {
        if (BuildConfig.DEBUG) {
            Log.d("JsonExport", "Starting JSON export")
        }
        _isLoading.value = true
        try {
            val activityCount = withContext(Dispatchers.IO) {
                repository.exportToJson(writer)
                repository.getTotalActivityCount() // Get activity count after export
            }
            // Update last export time
            sharedPrefs.edit().putLong("last_export_time", System.currentTimeMillis()).apply()

            // Track analytics for JSON export
            analyticsManager.trackDataOperation(
                operationType = "export",
                format = "json",
                activityCount = activityCount,
                success = true
            )

            _exportResult.value = Event(Result.success("JSON export successful!"))
        } catch (e: Exception) {
            Log.e("JsonExport", "Exception in ViewModel: ${e.javaClass.simpleName} - ${e.message}", e)
            // Record crash context for JSON export error
            crashlyticsManager.recordCsvError("json_export", 0, e) // Reuse CSV error tracking for JSON

            // Track failed JSON export
            analyticsManager.trackDataOperation(
                operationType = "export",
                format = "json",
                activityCount = 0,
                success = false
            )

            val errorMessage = "JSON export failed: ${e.javaClass.simpleName} - ${e.message}"
            _exportResult.value = Event(Result.failure(Exception(errorMessage, e)))
        } finally {
            _isLoading.value = false
        }
    }

    fun importFromCsv(csvContent: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // First validate the CSV against user limits
                val pieceLimit = proUserManager.getPieceLimit()
                val activityLimit = proUserManager.getActivityLimit()

                val validationResult = withContext(Dispatchers.IO) {
                    CsvHandler.validateCsv(csvContent.reader(), pieceLimit, activityLimit)
                }

                if (!validationResult.isValid) {
                    // Validation failed - show error dialog
                    _validationResult.value = Event(validationResult)
                } else {
                    // Validation passed - proceed with actual import using fresh reader
                    val importResult = withContext(Dispatchers.IO) {
                        repository.importFromCsv(csvContent.reader())
                    }

                    _importResult.value = Event(Result.success(importResult))
                }

            } catch (e: Exception) {
                // Record crash context for CSV import error
                crashlyticsManager.recordCsvError("import", 0, e) // Activity count not available at this level
                _validationResult.value = Event(
                    CsvHandler.CsvValidationResult(
                        isValid = false,
                        activityCount = 0,
                        uniquePieceCount = 0,
                        errors = listOf("Failed to validate CSV: ${e.message}")
                    )
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun importFromJson(jsonContent: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // First validate the JSON against user limits
                val pieceLimit = proUserManager.getPieceLimit()
                val activityLimit = proUserManager.getActivityLimit()

                val validationResult = withContext(Dispatchers.IO) {
                    repository.validateJsonForImport(jsonContent.reader(), pieceLimit, activityLimit)
                }

                if (!validationResult.isValid) {
                    // Validation failed - show error dialog
                    _jsonValidationResult.value = Event(validationResult)

                    // Track failed JSON import (validation failed)
                    analyticsManager.trackDataOperation(
                        operationType = "import",
                        format = "json",
                        activityCount = validationResult.activityCount,
                        success = false
                    )
                } else {
                    // Validation passed - proceed with actual import using fresh reader
                    val importResult = withContext(Dispatchers.IO) {
                        repository.importFromJson(jsonContent.reader())
                    }

                    // Track successful JSON import
                    analyticsManager.trackDataOperation(
                        operationType = "import",
                        format = "json",
                        activityCount = importResult.activitiesImported,
                        success = importResult.success
                    )

                    _jsonImportResult.value = Event(Result.success(importResult))
                }

            } catch (e: Exception) {
                // Record crash context for JSON import error
                crashlyticsManager.recordCsvError("json_import", 0, e) // Reuse CSV error tracking for JSON

                // Track failed JSON import (exception occurred)
                analyticsManager.trackDataOperation(
                    operationType = "import",
                    format = "json",
                    activityCount = 0,
                    success = false
                )
                _jsonValidationResult.value = Event(
                    JsonValidationResult(
                        isValid = false,
                        pieceCount = 0,
                        activityCount = 0,
                        achievementCount = 0,
                        errors = listOf("Failed to validate JSON: ${e.message}"),
                        formatVersion = null
                    )
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun purgeAllData() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.deleteAllActivities()
                    repository.deleteAllPiecesAndTechniques()
                    repository.deleteAllAchievements()

                    // Reinitialize achievements after purging
                    val achievementManager = AchievementManager(context, repository)
                    achievementManager.initializeAchievements()
                }
                _purgeResult.value = Event(Result.success("All data has been purged successfully."))
            } catch (e: Exception) {
                // Record crash context for database purge error
                crashlyticsManager.recordDatabaseError("purge_all_data", e)
                _purgeResult.value = Event(Result.failure(e))
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun isSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(context) != null
    }

    fun getSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    fun getLastSyncTime(): Long {
        return syncManager.getLastSyncTime()
    }

    fun getLastExportTime(): Long {
        return sharedPrefs.getLong("last_export_time", 0L)
    }

    fun getSignInIntent(): Intent {
        return driveHelper.getSignInIntent()
    }

    fun handleSignInSuccess(account: GoogleSignInAccount) {
        syncManager.setGoogleEmail(account.email)
        syncManager.setSyncEnabled(true)
        _driveConnectionState.value = true
    }

    fun syncToDrive() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    syncManager.performSync()
                }
                _syncResult.value = Event(result)
            } catch (e: Exception) {
                _syncResult.value = Event(
                    SyncOperationResult(
                        success = false,
                        message = "Sync failed: ${e.message}"
                    )
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun restoreFromDrive() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    syncManager.restoreFromDrive()
                }
                _syncResult.value = Event(result)
            } catch (e: Exception) {
                _syncResult.value = Event(
                    SyncOperationResult(
                        success = false,
                        message = "Restore failed: ${e.message}"
                    )
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun disconnectDrive() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    driveHelper.signOut()
                }
                syncManager.clearSyncData()
                _driveConnectionState.value = false
            } catch (e: Exception) {
                Log.e("ImportExportViewModel", "Failed to disconnect from Drive", e)
            }
        }
    }
}

// Event wrapper to handle one-time events
class Event<out T>(private val content: T) {
    private var hasBeenHandled = false

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
}

class ImportExportViewModelFactory(
    private val repository: PianoRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImportExportViewModel::class.java)) {
            return ImportExportViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}