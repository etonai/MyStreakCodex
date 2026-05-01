package com.pseddev.mystreak.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey
    val type: AchievementType,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val dateCreated: Long = System.currentTimeMillis()
)

enum class AchievementType {
    FIRST_PIECE,
    FIRST_TECHNIQUE,
    FIRST_PRACTICE,
    FIRST_PERFORMANCE,
    FIRST_ONLINE_PERFORMANCE,
    FIRST_LIVE_PERFORMANCE,
    STREAK_3_DAYS,
    STREAK_5_DAYS,
    STREAK_8_DAYS,
    STREAK_14_DAYS,
    STREAK_30_DAYS,
    STREAK_61_DAYS,
    STREAK_100_DAYS
}