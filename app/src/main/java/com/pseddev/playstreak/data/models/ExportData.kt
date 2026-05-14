package com.pseddev.mystreak.data.models

import com.google.gson.annotations.SerializedName
import com.pseddev.mystreak.data.entities.CalendarColorLevel
import com.pseddev.mystreak.data.entities.SuccessLevel
import com.pseddev.mystreak.data.entities.TaskKind
import com.pseddev.mystreak.data.entities.TaskPriority

data class MyStreakExportData(
    @SerializedName("schema")
    val schema: MyStreakExportInfo,

    @SerializedName("tasks")
    val tasks: List<MyStreakExportTask>,

    @SerializedName("activities")
    val activities: List<MyStreakExportActivity>,

    @SerializedName("frozenCalendarStates")
    val frozenCalendarStates: List<MyStreakExportCalendarState>
)

data class MyStreakExportInfo(
    @SerializedName("name")
    val name: String,

    @SerializedName("version")
    val version: Int,

    @SerializedName("exportedAtMillis")
    val exportedAtMillis: Long,

    @SerializedName("appVersion")
    val appVersion: String
)

data class MyStreakExportTask(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("color")
    val color: String,

    @SerializedName("priority")
    val priority: TaskPriority,

    // Nullable for backward-compatible imports of schema v1/v2 files.
    @SerializedName("taskKind")
    val taskKind: TaskKind? = TaskKind.STANDARD,

    @SerializedName("minimumSuccess")
    val minimumSuccess: String,

    @SerializedName("mediumSuccess")
    val mediumSuccess: String,

    @SerializedName("highSuccess")
    val highSuccess: String,

    @SerializedName("isActive")
    val isActive: Boolean,

    @SerializedName("dateCreated")
    val dateCreated: Long,

    @SerializedName("lastUpdated")
    val lastUpdated: Long
)

data class MyStreakExportActivity(
    @SerializedName("id")
    val id: Long,

    @SerializedName("taskId")
    val taskId: Long,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("successLevel")
    val successLevel: SuccessLevel,

    // Nullable for backward-compatible imports of schema v1 files where notes are absent.
    @SerializedName("notes")
    val notes: String? = ""
)

data class MyStreakExportCalendarState(
    @SerializedName("dayStartMillis")
    val dayStartMillis: Long,

    @SerializedName("colorLevel")
    val colorLevel: CalendarColorLevel,

    @SerializedName("frozenAtMillis")
    val frozenAtMillis: Long
)

data class JsonImportResult(
    val success: Boolean,
    val piecesImported: Int,
    val activitiesImported: Int,
    val achievementsImported: Int,
    val calendarStatesImported: Int = 0,
    val errors: List<String>,
    val warnings: List<String>
)

data class JsonValidationResult(
    val isValid: Boolean,
    val pieceCount: Int,
    val activityCount: Int,
    val achievementCount: Int,
    val calendarStateCount: Int = 0,
    val errors: List<String>,
    val formatVersion: String?
)
