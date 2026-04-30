package com.pseddev.playstreak.data.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.pseddev.playstreak.data.entities.ItemType
import com.pseddev.playstreak.data.entities.PieceOrTechnique
import kotlinx.coroutines.flow.Flow

@Dao
interface PieceOrTechniqueDao {
    @Query("SELECT * FROM tasks ORDER BY name COLLATE NOCASE ASC")
    fun getAllPiecesAndTechniques(): Flow<List<PieceOrTechnique>>

    @Query("SELECT * FROM tasks WHERE priority = 'HIGH' ORDER BY name COLLATE NOCASE ASC")
    fun getFavorites(): Flow<List<PieceOrTechnique>>

    @Query("SELECT * FROM tasks WHERE :type IN ('PIECE', 'TECHNIQUE') ORDER BY name COLLATE NOCASE ASC")
    fun getByType(type: ItemType): Flow<List<PieceOrTechnique>>

    @Query("SELECT * FROM tasks WHERE isActive = 1 ORDER BY name COLLATE NOCASE ASC")
    fun getActiveTasks(): Flow<List<PieceOrTechnique>>

    @Query("SELECT * FROM tasks WHERE isActive = 0 ORDER BY name COLLATE NOCASE ASC")
    fun getInactiveTasks(): Flow<List<PieceOrTechnique>>

    @Query("SELECT * FROM tasks WHERE isActive = 1 AND priority = 'HIGH' ORDER BY name COLLATE NOCASE ASC")
    fun getActiveHighPriorityTasks(): Flow<List<PieceOrTechnique>>

    @Insert
    suspend fun insert(piece: PieceOrTechnique): Long

    @Update
    suspend fun update(piece: PieceOrTechnique)

    @Delete
    suspend fun delete(piece: PieceOrTechnique)

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): PieceOrTechnique?

    @Query("SELECT * FROM tasks ORDER BY name COLLATE NOCASE ASC")
    fun getAllPiecesAndTechniquesSync(): List<PieceOrTechnique>

    @Query("SELECT tasks.* FROM tasks INNER JOIN activities ON tasks.id = activities.taskId GROUP BY tasks.id ORDER BY MAX(activities.timestamp) DESC")
    fun getPiecesWithPracticeHistory(): Flow<List<PieceOrTechnique>>

    @Query("SELECT tasks.* FROM tasks INNER JOIN activities ON tasks.id = activities.taskId WHERE tasks.priority = 'HIGH' GROUP BY tasks.id ORDER BY MAX(activities.timestamp) DESC")
    fun getPiecesWithPerformanceHistory(): Flow<List<PieceOrTechnique>>

    @Query("SELECT tasks.* FROM tasks INNER JOIN activities ON tasks.id = activities.taskId GROUP BY tasks.id ORDER BY MAX(activities.timestamp) DESC LIMIT :limit")
    fun getRecentlyPracticedPieces(limit: Int = 10): Flow<List<PieceOrTechnique>>

    @Query("SELECT tasks.* FROM tasks INNER JOIN activities ON tasks.id = activities.taskId WHERE tasks.priority = 'HIGH' GROUP BY tasks.id ORDER BY MAX(activities.timestamp) DESC LIMIT :limit")
    fun getRecentlyPerformedPieces(limit: Int = 10): Flow<List<PieceOrTechnique>>

    @Query("SELECT tasks.* FROM tasks INNER JOIN activities ON tasks.id = activities.taskId WHERE activities.successLevel = 'HIGH' GROUP BY tasks.id ORDER BY MAX(activities.timestamp) DESC")
    fun getPiecesWithSatisfactoryPractice(): Flow<List<PieceOrTechnique>>

    @Query("SELECT COUNT(*) > 0 FROM tasks WHERE LOWER(name) = LOWER(:name)")
    suspend fun doesPieceNameExist(name: String): Boolean
}
