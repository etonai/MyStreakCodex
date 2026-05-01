package com.pseddev.mystreak.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class PieceOrTechnique(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val name: String,
    val color: String = "#66B2FF",
    val priority: TaskPriority = TaskPriority.LOW,
    val minimumSuccess: String = "Minimum",
    val mediumSuccess: String = "Medium",
    val highSuccess: String = "High",
    val isActive: Boolean = true,
    val dateCreated: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis()
) {
    @Ignore
    constructor(
        id: Long = 0,
        name: String,
        type: ItemType = ItemType.PIECE,
        isFavorite: Boolean = false,
        dateCreated: Long = System.currentTimeMillis(),
        practiceCount: Int = 0,
        performanceCount: Int = 0,
        lastPracticeDate: Long? = null,
        secondLastPracticeDate: Long? = null,
        thirdLastPracticeDate: Long? = null,
        lastPerformanceDate: Long? = null,
        secondLastPerformanceDate: Long? = null,
        thirdLastPerformanceDate: Long? = null,
        lastSatisfactoryPractice: Long? = null,
        lastSatisfactoryPerformance: Long? = null,
        lastUpdated: Long = System.currentTimeMillis()
    ) : this(
        id = id,
        name = name,
        priority = if (isFavorite) TaskPriority.HIGH else TaskPriority.LOW,
        dateCreated = dateCreated,
        lastUpdated = lastUpdated
    )

    val type: ItemType
        get() = ItemType.PIECE

    val isFavorite: Boolean
        get() = priority == TaskPriority.HIGH

    val practiceCount: Int
        get() = 0

    val performanceCount: Int
        get() = 0

    val lastPracticeDate: Long?
        get() = null

    val secondLastPracticeDate: Long?
        get() = null

    val thirdLastPracticeDate: Long?
        get() = null

    val lastPerformanceDate: Long?
        get() = null

    val secondLastPerformanceDate: Long?
        get() = null

    val thirdLastPerformanceDate: Long?
        get() = null

    val lastSatisfactoryPractice: Long?
        get() = null

    val lastSatisfactoryPerformance: Long?
        get() = null
}

typealias Task = PieceOrTechnique

enum class TaskPriority {
    HIGH,
    LOW
}

enum class ItemType {
    PIECE,
    TECHNIQUE
}
