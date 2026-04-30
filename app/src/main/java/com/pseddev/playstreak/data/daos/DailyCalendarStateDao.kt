package com.pseddev.playstreak.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pseddev.playstreak.data.entities.DailyCalendarState
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyCalendarStateDao {
    @Query("SELECT * FROM daily_calendar_states WHERE dayStartMillis = :dayStartMillis LIMIT 1")
    suspend fun getStateForDay(dayStartMillis: Long): DailyCalendarState?

    @Query("SELECT * FROM daily_calendar_states WHERE dayStartMillis >= :startMillis AND dayStartMillis < :endMillis")
    fun getStatesForDateRange(startMillis: Long, endMillis: Long): Flow<List<DailyCalendarState>>

    @Query("SELECT * FROM daily_calendar_states WHERE dayStartMillis >= :startMillis AND dayStartMillis < :endMillis")
    suspend fun getStatesForDateRangeOnce(startMillis: Long, endMillis: Long): List<DailyCalendarState>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(state: DailyCalendarState)

    @Query("DELETE FROM daily_calendar_states")
    suspend fun deleteAll()
}
