package com.pseddev.playstreak.data.repository

import android.util.Log
import com.pseddev.playstreak.data.daos.ActivityDao
import com.pseddev.playstreak.data.daos.AchievementDao
import com.pseddev.playstreak.data.daos.DailyCalendarStateDao
import com.pseddev.playstreak.data.daos.PieceOrTechniqueDao
import com.pseddev.playstreak.data.entities.Achievement
import com.pseddev.playstreak.data.entities.Activity
import com.pseddev.playstreak.data.entities.ActivityType
import com.pseddev.playstreak.data.entities.CalendarColorLevel
import com.pseddev.playstreak.data.entities.DailyCalendarState
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.data.entities.PieceOrTechnique
import com.pseddev.playstreak.data.entities.TaskPriority
import com.pseddev.playstreak.ui.progress.ActivityWithPiece
import com.pseddev.playstreak.utils.CsvHandler
import com.pseddev.playstreak.utils.JsonExporter
import com.pseddev.playstreak.utils.JsonImporter
import com.pseddev.playstreak.utils.StreakCalculator
import com.pseddev.playstreak.utils.ConfigurationManager
import com.pseddev.playstreak.data.models.JsonImportResult
import com.pseddev.playstreak.data.models.JsonValidationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.Writer
import java.io.Reader
import java.util.Calendar
import java.util.TimeZone

class PianoRepository(
    private val pieceOrTechniqueDao: PieceOrTechniqueDao,
    private val activityDao: ActivityDao,
    private val dailyCalendarStateDao: DailyCalendarStateDao,
    private val achievementDao: AchievementDao,
    private val context: android.content.Context
) {
    
    private val configurationManager = ConfigurationManager.getInstance(context)
    
    fun getAllPiecesAndTechniques(): Flow<List<PieceOrTechnique>> = 
        pieceOrTechniqueDao.getAllPiecesAndTechniques()
    
    fun getFavorites(): Flow<List<PieceOrTechnique>> = 
        pieceOrTechniqueDao.getFavorites()

    fun getActiveTasks(): Flow<List<PieceOrTechnique>> =
        pieceOrTechniqueDao.getActiveTasks()

    fun getInactiveTasks(): Flow<List<PieceOrTechnique>> =
        pieceOrTechniqueDao.getInactiveTasks()

    fun getActiveHighPriorityTasks(): Flow<List<PieceOrTechnique>> =
        pieceOrTechniqueDao.getActiveHighPriorityTasks()
    
    fun getPieces(): Flow<List<PieceOrTechnique>> = 
        pieceOrTechniqueDao.getByType(ItemType.PIECE)
    
    fun getTechniques(): Flow<List<PieceOrTechnique>> = 
        pieceOrTechniqueDao.getByType(ItemType.TECHNIQUE)
    
    suspend fun insertPieceOrTechnique(item: PieceOrTechnique): Long = 
        pieceOrTechniqueDao.insert(item)
    
    suspend fun updatePieceOrTechnique(item: PieceOrTechnique) = 
        pieceOrTechniqueDao.update(item)
    
    suspend fun deletePieceOrTechnique(item: PieceOrTechnique) = 
        pieceOrTechniqueDao.delete(item)
    
    suspend fun doesPieceNameExist(name: String): Boolean = 
        pieceOrTechniqueDao.doesPieceNameExist(name)
    
    suspend fun deletePieceAndActivities(pieceId: Long) {
        // Count activities before deletion to update lifetime counter
        val activitiesToDelete = getActivitiesForPiece(pieceId).first()
        val activityCount = activitiesToDelete.size
        
        // First delete all activities for this piece
        activityDao.deleteActivitiesForPiece(pieceId)
        
        // Decrement lifetime activity counter for user deletions
        if (activityCount > 0) {
            configurationManager.decrementLifetimeActivityCount(activityCount)
        }
        
        // Then delete the piece itself
        val piece = pieceOrTechniqueDao.getById(pieceId)
        piece?.let { pieceOrTechniqueDao.delete(it) }
    }
    
    suspend fun deleteAllPiecesAndTechniques() = 
        pieceOrTechniqueDao.deleteAll()
    
    fun getAllActivities(): Flow<List<Activity>> = 
        activityDao.getAllActivities()
    
    fun getActivitiesForPiece(pieceId: Long): Flow<List<Activity>> = 
        activityDao.getActivitiesForPiece(pieceId)
    
    fun getActivitiesForDateRange(startTime: Long, endTime: Long): Flow<List<Activity>> = 
        activityDao.getActivitiesForDateRange(startTime, endTime)

    fun getFrozenCalendarStatesForDateRange(startTime: Long, endTime: Long): Flow<List<DailyCalendarState>> =
        dailyCalendarStateDao.getStatesForDateRange(startTime, endTime)
    
    fun getTodaysActivities(): Flow<List<Activity>> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endTime = calendar.timeInMillis
        return getActivitiesForDateRange(startTime, endTime)
    }
    
    fun getYesterdaysActivities(): Flow<List<Activity>> {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endTime = calendar.timeInMillis
        return getActivitiesForDateRange(startTime, endTime)
    }
    
    suspend fun insertActivity(activity: Activity) {
        requireNotFuture(activity)
        activityDao.insert(activity)
        
        // Increment lifetime activity counter
        configurationManager.incrementLifetimeActivityCount(1)
        
        updatePieceStatistics(activity.pieceOrTechniqueId)
    }
    
    suspend fun updateActivity(activity: Activity) {
        requireNotFuture(activity)
        activityDao.update(activity)
        updatePieceStatistics(activity.pieceOrTechniqueId)
    }
    
    suspend fun deleteActivity(activity: Activity) {
        val pieceId = activity.pieceOrTechniqueId
        activityDao.delete(activity)
        
        // Decrement lifetime activity counter for user deletions
        configurationManager.decrementLifetimeActivityCount(1)
        
        updatePieceStatistics(pieceId)
    }
    
    suspend fun deleteAllActivities() = 
        activityDao.deleteAll()

    suspend fun deleteAllCalendarStates() =
        dailyCalendarStateDao.deleteAll()
        
    suspend fun deleteAllAchievements() = 
        achievementDao.deleteAllAchievements()
    
    suspend fun getStreakCount(startTime: Long): Int = 
        activityDao.getStreakCount(startTime)
    
    suspend fun getActivityCount(): Int = 
        activityDao.getActivityCount()
    
    fun getAllActivitiesWithPieces(): Flow<List<ActivityWithPiece>> {
        return combine(
            getAllActivities(),
            getAllPiecesAndTechniques()
        ) { activities, pieces ->
            activities.mapNotNull { activity ->
                val piece = pieces.find { it.id == activity.pieceOrTechniqueId }
                piece?.let { ActivityWithPiece(activity, it) }
            }
        }
    }
    
    suspend fun calculateCurrentStreak(): Int {
        val activities = getAllActivities().first()
        return StreakCalculator().calculateCurrentStreak(activities)
    }

    fun getRollingWeekSummaryText(): Flow<String> {
        val (startTime, endTime) = getRollingSevenDayRange()
        return combine(
            getActivitiesForDateRange(startTime, endTime),
            getAllPiecesAndTechniques()
        ) { activities, tasks ->
            val activeTaskIds = tasks.filter { it.isActive }.map { it.id }.toSet()
            val highPriorityTaskIds = tasks
                .filter { it.isActive && it.priority == TaskPriority.HIGH }
                .map { it.id }
                .toSet()
            val activeActivities = activities.filter { it.taskId in activeTaskIds }
            val highPriorityActivities = activeActivities.count { it.taskId in highPriorityTaskIds }

            buildString {
                append("- ${activeActivities.size} activit${if (activeActivities.size != 1) "ies" else "y"} logged\n")
                append("- $highPriorityActivities high priority activit${if (highPriorityActivities != 1) "ies" else "y"} logged")
            }
        }
    }

    fun getHighPriorityOutstandingTasksForToday(): Flow<List<PieceOrTechnique>> {
        val (startTime, endTime) = getTodayRange()
        return combine(
            getActiveHighPriorityTasks(),
            getActivitiesForDateRange(startTime, endTime)
        ) { highPriorityTasks, todayActivities ->
            val completedTaskIds = todayActivities.map { it.taskId }.toSet()
            highPriorityTasks.filter { it.id !in completedTaskIds }
        }
    }

    fun getCalendarColorLevelsForDateRange(startTime: Long, endTime: Long): Flow<Map<Long, CalendarColorLevel>> {
        return combine(
            getActivitiesForDateRange(startTime, endTime),
            getAllPiecesAndTechniques(),
            getFrozenCalendarStatesForDateRange(startTime, endTime)
        ) { activities, tasks, frozenStates ->
            val frozenByDay = frozenStates.associateBy { it.dayStartMillis }
            val todayStart = startOfDay(System.currentTimeMillis())
            val dayStarts = generateDayStarts(startTime, endTime)

            dayStarts.associateWith { dayStart ->
                val dayEnd = dayStart + DAY_MILLIS
                if (dayStart < todayStart) {
                    frozenByDay[dayStart]?.colorLevel ?: CalendarColorLevel.NONE
                } else {
                    val dayActivities = activities.filter { it.timestamp >= dayStart && it.timestamp < dayEnd }
                    calculateCalendarColorLevel(dayActivities, tasks.filter { it.isActive })
                }
            }
        }
    }

    suspend fun freezePastCalendarDaysIfNeeded() {
        val todayStart = startOfDay(System.currentTimeMillis())
        val allActivities = getAllActivities().first()
        val allTasks = getAllPiecesAndTechniques().first()
        val activityDays = allActivities
            .map { startOfDay(it.timestamp) }
            .filter { it < todayStart }
            .toSet()

        activityDays.forEach { dayStart ->
            val existing = dailyCalendarStateDao.getStateForDay(dayStart)
            if (existing == null) {
                val dayActivities = allActivities.filter {
                    it.timestamp >= dayStart && it.timestamp < dayStart + DAY_MILLIS
                }
                val level = calculateCalendarColorLevel(dayActivities, allTasks.filter { it.isActive })
                dailyCalendarStateDao.insertIfAbsent(
                    DailyCalendarState(
                        dayStartMillis = dayStart,
                        colorLevel = level
                    )
                )
            }
        }
    }

    fun calculateCalendarColorLevel(
        activities: List<Activity>,
        activeTasks: List<PieceOrTechnique>
    ): CalendarColorLevel {
        if (activities.isEmpty()) return CalendarColorLevel.NONE

        val highPriorityTaskIds = activeTasks
            .filter { it.priority == TaskPriority.HIGH }
            .map { it.id }
            .toSet()
        val performedHighPriorityTaskIds = activities
            .map { it.taskId }
            .filter { it in highPriorityTaskIds }
            .toSet()

        if (highPriorityTaskIds.isEmpty() || performedHighPriorityTaskIds.isEmpty()) {
            return CalendarColorLevel.ANY_ACTIVITY
        }

        if (performedHighPriorityTaskIds.size == highPriorityTaskIds.size) {
            return CalendarColorLevel.ALL_HIGH_PRIORITY
        }

        val halfThreshold = highPriorityTaskIds.size / 2
        if (halfThreshold > 0 && performedHighPriorityTaskIds.size >= halfThreshold) {
            return CalendarColorLevel.HALF_HIGH_PRIORITY
        }

        return CalendarColorLevel.HIGH_PRIORITY_ACTIVITY
    }
    
    suspend fun getPieceOrTechniqueById(id: Long): PieceOrTechnique? {
        return pieceOrTechniqueDao.getById(id)
    }
    
    // Statistics-based query methods for improved performance
    fun getPiecesWithPracticeHistory(): Flow<List<PieceOrTechnique>> = 
        pieceOrTechniqueDao.getPiecesWithPracticeHistory()
    
    fun getPiecesWithPerformanceHistory(): Flow<List<PieceOrTechnique>> = 
        pieceOrTechniqueDao.getPiecesWithPerformanceHistory()
    
    fun getRecentlyPracticedPieces(limit: Int = 10): Flow<List<PieceOrTechnique>> = 
        pieceOrTechniqueDao.getRecentlyPracticedPieces(limit)
    
    fun getRecentlyPerformedPieces(limit: Int = 10): Flow<List<PieceOrTechnique>> = 
        pieceOrTechniqueDao.getRecentlyPerformedPieces(limit)
    
    fun getPiecesWithSatisfactoryPractice(): Flow<List<PieceOrTechnique>> = 
        pieceOrTechniqueDao.getPiecesWithSatisfactoryPractice()
    
    suspend fun exportToCsv(writer: Writer) {
        Log.d("ExportDebug", "Repository.exportToCsv called")
        try {
            Log.d("ExportDebug", "Getting activities from database...")
            val activities = getAllActivities().first().sortedBy { it.timestamp }
            Log.d("ExportDebug", "Got ${activities.size} activities")
            
            Log.d("ExportDebug", "Getting pieces from database...")
            val pieces = getAllPiecesAndTechniques().first().associateBy { it.id }
            Log.d("ExportDebug", "Got ${pieces.size} pieces")
            
            Log.d("ExportDebug", "Calling CsvHandler.exportActivitiesToCsv")
            CsvHandler.exportActivitiesToCsv(writer, activities, pieces)
            Log.d("ExportDebug", "CsvHandler.exportActivitiesToCsv completed")
        } catch (e: Exception) {
            Log.e("ExportDebug", "Exception in Repository.exportToCsv: ${e.javaClass.simpleName} - ${e.message}", e)
            throw e
        }
    }
    
    suspend fun importFromCsv(reader: Reader): CsvHandler.ImportResult {
        val result = CsvHandler.importActivitiesFromCsv(reader)
        
        // Save current favorites before clearing data (do this regardless of errors)
        val existingPieces = getAllPiecesAndTechniques().first()
        val favoritesByName = existingPieces
            .filter { it.isFavorite }
            .associate { it.name to it.isFavorite }
        
        // Always clear existing data to prevent duplicates, even if there are errors
        deleteAllActivities()
        deleteAllPiecesAndTechniques()
        deleteAllAchievements()
        deleteAllCalendarStates()
        
        // Only proceed with import if we have activities to import
        if (result.activities.isNotEmpty()) {
            // Create piece/technique map
            val pieceMap = mutableMapOf<String, Long>()
            
            // Insert unique pieces/techniques
            Log.d("ImportRepo", "Creating pieces from ${result.uniquePieceNames.size} unique names: ${result.uniquePieceNames}")
            result.uniquePieceNames.forEach { pieceName ->
                // First check if any imported activity provides explicit type info for this piece
                val explicitType = result.activities
                    .firstOrNull { it.pieceName == pieceName && it.pieceType != null }
                    ?.pieceType
                
                // Determine if it's a piece or technique - use explicit type if available, otherwise use heuristic
                val itemType = explicitType ?: when {
                    pieceName.contains("Scale", ignoreCase = true) ||
                    pieceName.contains("Arpeggio", ignoreCase = true) ||
                    pieceName.contains("Exercise", ignoreCase = true) ||
                    pieceName.contains("Technique", ignoreCase = true) -> ItemType.TECHNIQUE
                    else -> ItemType.PIECE
                }
                
                // Preserve favorite status if this piece was previously a favorite
                val isFavorite = favoritesByName[pieceName] ?: false
                
                val piece = PieceOrTechnique(
                    name = pieceName,
                    type = itemType,
                    isFavorite = isFavorite
                )
                
                val id = insertPieceOrTechnique(piece)
                pieceMap[pieceName] = id
                Log.d("ImportRepo", "Created piece: '$pieceName' with ID $id")
            }
            
            // Insert activities
            result.activities.forEach { importedActivity ->
                val pieceId = pieceMap[importedActivity.pieceName]
                if (pieceId != null) {
                    val activity = Activity(
                        timestamp = importedActivity.timestamp,
                        pieceOrTechniqueId = pieceId,
                        activityType = importedActivity.activityType,
                        level = importedActivity.level,
                        performanceType = importedActivity.performanceType,
                        minutes = importedActivity.minutes,
                        notes = importedActivity.notes
                    )
                    insertActivity(activity)
                }
            }
        }
        
        return result
    }
    
    suspend fun exportToJson(writer: Writer) {
        Log.d("JsonExport", "Repository.exportToJson called")
        try {
            Log.d("JsonExport", "Getting pieces, activities, and achievements from database...")
            val pieces = getAllPiecesAndTechniques().first()
            val activities = getAllActivities().first().sortedBy { it.timestamp }
            val achievements = achievementDao.getAllAchievementsList()
            val lifetimeCount = configurationManager.getLifetimeActivityCount()
            
            Log.d("JsonExport", "Got ${pieces.size} pieces, ${activities.size} activities, ${achievements.size} achievements, lifetime count: $lifetimeCount")
            Log.d("JsonExport", "Calling JsonExporter.exportToJson")
            
            JsonExporter.exportToJson(writer, pieces, activities, achievements, lifetimeCount)
            
            Log.d("JsonExport", "JsonExporter.exportToJson completed")
        } catch (e: Exception) {
            Log.e("JsonExport", "Exception in Repository.exportToJson: ${e.javaClass.simpleName} - ${e.message}", e)
            throw e
        }
    }
    
    suspend fun validateJsonForImport(reader: java.io.Reader, pieceLimit: Int, activityLimit: Int, achievementLimit: Int = 20): JsonValidationResult {
        return JsonImporter.validateJson(reader, pieceLimit, activityLimit, achievementLimit)
    }
    
    suspend fun importFromJson(reader: java.io.Reader): JsonImportResult {
        Log.d("JsonImport", "Repository.importFromJson called")
        
        try {
            // First validate and parse the JSON
            val importResult = JsonImporter.importFromJson(reader)
            
            if (!importResult.success) {
                return importResult
            }
            
            // Get the imported data
            val importedPieces = JsonImporter.getLastImportedPieces()
            val importedActivities = JsonImporter.getLastImportedActivities()
            
            Log.d("JsonImport", "Importing ${importedPieces.size} pieces and ${importedActivities.size} activities")
            
            // Clear existing data
            deleteAllActivities()
            deleteAllPiecesAndTechniques()
            deleteAllAchievements()
            deleteAllCalendarStates()
            
            // Create piece name to new ID mapping
            val pieceNameToIdMap = mutableMapOf<String, Long>()
            
            // Insert pieces and build mapping
            importedPieces.forEach { piece ->
                val newId = insertPieceOrTechnique(piece)
                pieceNameToIdMap[piece.name] = newId
                Log.d("JsonImport", "Inserted piece '${piece.name}' with new ID $newId")
            }
            
            // Create mapping from original piece IDs to new piece IDs
            val originalPieceIdToNewIdMap = mutableMapOf<Long, Long>()
            val originalPieceIds = JsonImporter.getLastOriginalPieceIds()
            
            originalPieceIds.forEach { (pieceName, originalId) ->
                val newId = pieceNameToIdMap[pieceName]
                if (newId != null) {
                    originalPieceIdToNewIdMap[originalId] = newId
                }
            }
            
            // Insert activities with correct piece IDs
            importedActivities.forEach { activity ->
                val newPieceId = originalPieceIdToNewIdMap[activity.pieceOrTechniqueId]
                if (newPieceId != null) {
                    val activityWithCorrectId = activity.copy(taskId = newPieceId)
                    insertActivity(activityWithCorrectId)
                } else {
                    Log.w("JsonImport", "Could not find new piece ID for original piece ID ${activity.pieceOrTechniqueId}")
                }
            }
            
            // Update lifetime counter based on imported data
            val importedLifetimeCount = JsonImporter.getLastImportedLifetimeCount()
            if (importedLifetimeCount != null) {
                // Use the lifetime count from the JSON file
                configurationManager.setLifetimeActivityCount(importedLifetimeCount)
                Log.d("JsonImport", "Set lifetime count from JSON: $importedLifetimeCount")
            } else {
                // Fallback to using the count of imported activities as lifetime count
                configurationManager.setLifetimeActivityCount(importedActivities.size)
                Log.d("JsonImport", "Set lifetime count from activity count: ${importedActivities.size}")
            }
            
            Log.d("JsonImport", "JSON import completed successfully")
            
            return JsonImportResult(
                success = true,
                piecesImported = importedPieces.size,
                activitiesImported = importedActivities.size,
                achievementsImported = 0, // TODO: Implement achievements import in repository
                errors = emptyList(),
                warnings = emptyList()
            )
            
        } catch (e: Exception) {
            Log.e("JsonImport", "Exception in Repository.importFromJson: ${e.javaClass.simpleName} - ${e.message}", e)
            return JsonImportResult(
                success = false,
                piecesImported = 0,
                activitiesImported = 0,
                achievementsImported = 0,
                errors = listOf("Import failed: ${e.message}"),
                warnings = emptyList()
            )
        }
    }
    
    private suspend fun updatePieceStatistics(pieceId: Long) {
        val piece = pieceOrTechniqueDao.getById(pieceId) ?: return
        pieceOrTechniqueDao.update(piece.copy(lastUpdated = System.currentTimeMillis()))
    }

    private fun requireNotFuture(activity: Activity) {
        require(activity.timestamp <= System.currentTimeMillis()) {
            "Activities cannot be dated in the future."
        }
    }

    private fun getTodayRange(): Pair<Long, Long> {
        val startTime = startOfDay(System.currentTimeMillis())
        return startTime to startTime + DAY_MILLIS
    }

    private fun getRollingSevenDayRange(): Pair<Long, Long> {
        val todayStart = startOfDay(System.currentTimeMillis())
        return (todayStart - (6 * DAY_MILLIS)) to (todayStart + DAY_MILLIS)
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

    private fun generateDayStarts(startTime: Long, endTime: Long): List<Long> {
        val starts = mutableListOf<Long>()
        var current = startOfDay(startTime)
        while (current < endTime) {
            starts.add(current)
            current += DAY_MILLIS
        }
        return starts
    }

    private companion object {
        const val DAY_MILLIS = 24L * 60L * 60L * 1000L
    }
    
    // Data pruning methods for Phase 3
    suspend fun getTotalActivityCount(): Int {
        return activityDao.getTotalActivityCount()
    }
    
    suspend fun getOldestActivities(count: Int): List<Activity> {
        return activityDao.getOldestActivities(count)
    }
    
    suspend fun deleteActivitiesWithoutStatsUpdate(activityIds: List<Long>): Int {
        // Delete activities directly without triggering piece statistics updates
        return activityDao.deleteActivitiesByIds(activityIds)
    }
    
}
