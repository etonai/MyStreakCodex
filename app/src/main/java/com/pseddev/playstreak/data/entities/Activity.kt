package com.pseddev.mystreak.data.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class Activity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val taskId: Long,
    val successLevel: SuccessLevel = SuccessLevel.MINIMUM
) {
    @Ignore
    constructor(
        id: Long = 0,
        timestamp: Long,
        pieceOrTechniqueId: Long,
        activityType: ActivityType = ActivityType.PRACTICE,
        level: Int,
        performanceType: String = "activity",
        minutes: Int = -1,
        notes: String = ""
    ) : this(
        id = id,
        timestamp = timestamp,
        taskId = pieceOrTechniqueId,
        successLevel = when {
            level >= 3 -> SuccessLevel.HIGH
            level == 2 -> SuccessLevel.MEDIUM
            else -> SuccessLevel.MINIMUM
        }
    )

    val pieceOrTechniqueId: Long
        get() = taskId

    val activityType: ActivityType
        get() = ActivityType.PRACTICE

    val level: Int
        get() = when (successLevel) {
            SuccessLevel.MINIMUM -> 1
            SuccessLevel.MEDIUM -> 2
            SuccessLevel.HIGH -> 3
        }

    val performanceType: String
        get() = "activity"

    val minutes: Int
        get() = -1

    val notes: String
        get() = ""
}

enum class SuccessLevel {
    MINIMUM,
    MEDIUM,
    HIGH
}

enum class ActivityType {
    PRACTICE,
    PERFORMANCE
}
