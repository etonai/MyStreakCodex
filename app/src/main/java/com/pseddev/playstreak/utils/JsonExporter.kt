package com.pseddev.mystreak.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.pseddev.mystreak.BuildConfig
import com.pseddev.mystreak.data.entities.Activity
import com.pseddev.mystreak.data.entities.DailyCalendarState
import com.pseddev.mystreak.data.entities.PieceOrTechnique
import com.pseddev.mystreak.data.models.MyStreakExportActivity
import com.pseddev.mystreak.data.models.MyStreakExportCalendarState
import com.pseddev.mystreak.data.models.MyStreakExportData
import com.pseddev.mystreak.data.models.MyStreakExportInfo
import com.pseddev.mystreak.data.models.MyStreakExportTask
import java.io.Writer

object JsonExporter {
    const val SCHEMA_NAME = "MyStreak"
    const val SCHEMA_VERSION = 2

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    fun exportToJson(
        writer: Writer,
        tasks: List<PieceOrTechnique>,
        activities: List<Activity>,
        frozenCalendarStates: List<DailyCalendarState>
    ) {
        val exportData = MyStreakExportData(
            schema = MyStreakExportInfo(
                name = SCHEMA_NAME,
                version = SCHEMA_VERSION,
                exportedAtMillis = System.currentTimeMillis(),
                appVersion = BuildConfig.VERSION_NAME
            ),
            tasks = tasks
                .sortedBy { it.id }
                .map { task ->
                    MyStreakExportTask(
                        id = task.id,
                        name = task.name,
                        color = task.color,
                        priority = task.priority,
                        minimumSuccess = task.minimumSuccess,
                        mediumSuccess = task.mediumSuccess,
                        highSuccess = task.highSuccess,
                        isActive = task.isActive,
                        dateCreated = task.dateCreated,
                        lastUpdated = task.lastUpdated
                    )
                },
            activities = activities
                .sortedWith(compareBy<Activity> { it.timestamp }.thenBy { it.id })
                .map { activity ->
                    MyStreakExportActivity(
                        id = activity.id,
                        taskId = activity.taskId,
                        timestamp = activity.timestamp,
                        successLevel = activity.successLevel,
                        notes = activity.notes
                    )
                },
            frozenCalendarStates = frozenCalendarStates
                .sortedBy { it.dayStartMillis }
                .map { state ->
                    MyStreakExportCalendarState(
                        dayStartMillis = state.dayStartMillis,
                        colorLevel = state.colorLevel,
                        frozenAtMillis = state.frozenAtMillis
                    )
                }
        )

        writer.write(gson.toJson(exportData))
        writer.flush()
    }
}
