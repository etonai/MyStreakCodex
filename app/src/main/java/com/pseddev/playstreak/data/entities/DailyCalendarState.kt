package com.pseddev.playstreak.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_calendar_states")
data class DailyCalendarState(
    @PrimaryKey
    val dayStartMillis: Long,
    val colorLevel: CalendarColorLevel,
    val frozenAtMillis: Long = System.currentTimeMillis()
)

enum class CalendarColorLevel {
    NONE,
    ANY_ACTIVITY,
    HIGH_PRIORITY_ACTIVITY,
    HALF_HIGH_PRIORITY,
    ALL_HIGH_PRIORITY
}
