package com.pseddev.mystreak.data.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.pseddev.mystreak.data.entities.Activity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities ORDER BY timestamp DESC")
    fun getAllActivities(): Flow<List<Activity>>

    @Query("SELECT * FROM activities WHERE taskId = :pieceId ORDER BY timestamp DESC")
    fun getActivitiesForPiece(pieceId: Long): Flow<List<Activity>>

    @Query("SELECT * FROM activities WHERE timestamp >= :startTime AND timestamp < :endTime ORDER BY timestamp DESC")
    fun getActivitiesForDateRange(startTime: Long, endTime: Long): Flow<List<Activity>>

    @Query("SELECT taskId, COUNT(*) AS count FROM activities WHERE timestamp >= :startTime AND timestamp < :endTime GROUP BY taskId")
    suspend fun getTaskActivityCountsForDateRange(startTime: Long, endTime: Long): List<TaskActivityCount>

    @Insert
    suspend fun insert(activity: Activity)

    @Update
    suspend fun update(activity: Activity)

    @Delete
    suspend fun delete(activity: Activity)

    @Query("DELETE FROM activities")
    suspend fun deleteAll()

    @Query("SELECT COUNT(DISTINCT date(timestamp/1000, 'unixepoch', 'localtime')) as streak FROM activities WHERE timestamp >= :startTime")
    suspend fun getStreakCount(startTime: Long): Int

    @Query("SELECT COUNT(*) FROM activities")
    suspend fun getActivityCount(): Int

    @Query("DELETE FROM activities WHERE taskId = :pieceId")
    suspend fun deleteActivitiesForPiece(pieceId: Long)

    @Query("SELECT * FROM activities WHERE taskId = :pieceId ORDER BY timestamp DESC")
    fun getActivitiesForPieceSync(pieceId: Long): List<Activity>

    @Query("SELECT * FROM activities ORDER BY timestamp ASC LIMIT :count")
    suspend fun getOldestActivities(count: Int): List<Activity>

    @Query("DELETE FROM activities WHERE id IN (:activityIds)")
    suspend fun deleteActivitiesByIds(activityIds: List<Long>): Int

    @Query("SELECT COUNT(*) FROM activities")
    suspend fun getTotalActivityCount(): Int
}

data class TaskActivityCount(
    val taskId: Long,
    val count: Int
)
