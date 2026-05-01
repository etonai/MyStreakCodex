package com.pseddev.mystreak.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pseddev.mystreak.data.entities.Achievement
import com.pseddev.mystreak.data.entities.AchievementType
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {

    @Query("SELECT * FROM achievements ORDER BY type ASC")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements ORDER BY type ASC")
    suspend fun getAllAchievementsList(): List<Achievement>

    @Query("SELECT * FROM achievements WHERE isUnlocked = 1 ORDER BY unlockedAt ASC")
    fun getUnlockedAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE isUnlocked = 0 ORDER BY type ASC")
    fun getLockedAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE type = :type LIMIT 1")
    suspend fun getAchievementByType(type: AchievementType): Achievement?

    @Query("SELECT COUNT(*) FROM achievements WHERE isUnlocked = 1")
    suspend fun getUnlockedCount(): Int

    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun getTotalCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: Achievement): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAchievements(achievements: List<Achievement>)

    @Update
    suspend fun updateAchievement(achievement: Achievement)

    @Query("UPDATE achievements SET isUnlocked = 1, unlockedAt = :unlockedAt WHERE type = :type")
    suspend fun unlockAchievement(type: AchievementType, unlockedAt: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM achievements WHERE type = :type AND isUnlocked = 1)")
    suspend fun isAchievementUnlocked(type: AchievementType): Boolean

    @Query("DELETE FROM achievements")
    suspend fun deleteAllAchievements()

    @Query("UPDATE achievements SET isUnlocked = 0, unlockedAt = NULL")
    suspend fun resetAllAchievements()
}