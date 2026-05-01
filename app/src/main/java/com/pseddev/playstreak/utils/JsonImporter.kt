package com.pseddev.mystreak.utils

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.pseddev.mystreak.data.entities.Activity
import com.pseddev.mystreak.data.entities.CalendarColorLevel
import com.pseddev.mystreak.data.entities.DailyCalendarState
import com.pseddev.mystreak.data.entities.PieceOrTechnique
import com.pseddev.mystreak.data.entities.SuccessLevel
import com.pseddev.mystreak.data.entities.TaskPriority
import com.pseddev.mystreak.data.models.JsonImportResult
import com.pseddev.mystreak.data.models.JsonValidationResult
import com.pseddev.mystreak.data.models.MyStreakExportData
import java.io.Reader

object JsonImporter {
    private val gson = Gson()

    data class ParsedImport(
        val exportData: MyStreakExportData,
        val errors: List<String>,
        val warnings: List<String>
    )

    fun validateJson(reader: Reader): JsonValidationResult {
        val parsed = parseAndValidate(reader.readText())
        val data = parsed.exportData
        return JsonValidationResult(
            isValid = parsed.errors.isEmpty(),
            pieceCount = data.tasks.size,
            activityCount = data.activities.size,
            achievementCount = 0,
            calendarStateCount = data.frozenCalendarStates.size,
            errors = parsed.errors,
            formatVersion = data.schema.version.toString()
        )
    }

    fun parseImport(reader: Reader): ParsedImport {
        return parseAndValidate(reader.readText())
    }

    fun importResultForParsed(parsed: ParsedImport): JsonImportResult {
        return JsonImportResult(
            success = parsed.errors.isEmpty(),
            piecesImported = parsed.exportData.tasks.size,
            activitiesImported = parsed.exportData.activities.size,
            achievementsImported = 0,
            calendarStatesImported = parsed.exportData.frozenCalendarStates.size,
            errors = parsed.errors,
            warnings = parsed.warnings
        )
    }

    private fun parseAndValidate(content: String): ParsedImport {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        val root = try {
            JsonParser.parseString(content).asJsonObject
        } catch (e: Exception) {
            return emptyParsed("Invalid JSON format: ${e.message}")
        }

        if (root.has("exportInfo") || root.has("pieces") || root.has("achievements")) {
            return emptyParsed(
                "This appears to be a legacy PlayStreak export. MyStreak imports require the MyStreak v1 JSON schema."
            )
        }

        val missingFields = listOf("schema", "tasks", "activities", "frozenCalendarStates")
            .filterNot { root.has(it) }
        if (missingFields.isNotEmpty()) {
            return emptyParsed("Missing required MyStreak field(s): ${missingFields.joinToString()}.")
        }
        val nullFields = listOf("schema", "tasks", "activities", "frozenCalendarStates")
            .filter { root.get(it).isJsonNull }
        if (nullFields.isNotEmpty()) {
            return emptyParsed("MyStreak JSON contains null required field(s): ${nullFields.joinToString()}.")
        }

        val exportData = try {
            gson.fromJson(root, MyStreakExportData::class.java)
        } catch (e: JsonSyntaxException) {
            return emptyParsed("Invalid MyStreak JSON format: ${e.message}")
        } catch (e: Exception) {
            return emptyParsed("Unable to read MyStreak JSON: ${e.message}")
        }

        if (exportData.schema.name != JsonExporter.SCHEMA_NAME) {
            errors.add("Unsupported schema name '${exportData.schema.name}'. Expected '${JsonExporter.SCHEMA_NAME}'.")
        }

        if (exportData.schema.version != JsonExporter.SCHEMA_VERSION) {
            errors.add("Unsupported schema version ${exportData.schema.version}. Expected ${JsonExporter.SCHEMA_VERSION}.")
        }

        validateTasks(exportData, errors)
        validateActivities(exportData, errors)
        validateCalendarStates(exportData, errors)

        return ParsedImport(exportData, errors, warnings)
    }

    private fun validateTasks(exportData: MyStreakExportData, errors: MutableList<String>) {
        val seenIds = mutableSetOf<Long>()
        val seenNames = mutableSetOf<String>()

        exportData.tasks.forEach { task ->
            if (task.id <= 0) {
                errors.add("Task '${task.name}' has invalid id ${task.id}.")
            }
            if (!seenIds.add(task.id)) {
                errors.add("Duplicate Task id ${task.id}.")
            }
            if (task.name.isBlank()) {
                errors.add("Task ${task.id} has a blank name.")
            } else if (!seenNames.add(task.name.trim().lowercase())) {
                errors.add("Duplicate Task name '${task.name}'.")
            }
            if (!task.color.matches(Regex("^#[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?$"))) {
                errors.add("Task '${task.name}' has invalid color '${task.color}'.")
            }
            if (task.minimumSuccess.isBlank()) {
                errors.add("Task '${task.name}' has a blank minimum success description.")
            }
            if (task.mediumSuccess.isBlank()) {
                errors.add("Task '${task.name}' has a blank medium success description.")
            }
            if (task.highSuccess.isBlank()) {
                errors.add("Task '${task.name}' has a blank high success description.")
            }
        }
    }

    private fun validateActivities(exportData: MyStreakExportData, errors: MutableList<String>) {
        val taskIds = exportData.tasks.map { it.id }.toSet()
        val seenIds = mutableSetOf<Long>()
        val now = System.currentTimeMillis()

        exportData.activities.forEach { activity ->
            if (activity.id <= 0) {
                errors.add("Activity has invalid id ${activity.id}.")
            }
            if (!seenIds.add(activity.id)) {
                errors.add("Duplicate Activity id ${activity.id}.")
            }
            if (activity.taskId !in taskIds) {
                errors.add("Activity ${activity.id} references missing Task ${activity.taskId}.")
            }
            if (activity.timestamp <= 0) {
                errors.add("Activity ${activity.id} has invalid timestamp ${activity.timestamp}.")
            }
            if (activity.timestamp > now) {
                errors.add("Activity ${activity.id} is dated in the future.")
            }
        }
    }

    private fun validateCalendarStates(exportData: MyStreakExportData, errors: MutableList<String>) {
        val seenDays = mutableSetOf<Long>()
        exportData.frozenCalendarStates.forEach { state ->
            if (state.dayStartMillis <= 0) {
                errors.add("Frozen calendar state has invalid day ${state.dayStartMillis}.")
            }
            if (!seenDays.add(state.dayStartMillis)) {
                errors.add("Duplicate frozen calendar state for day ${state.dayStartMillis}.")
            }
            if (state.frozenAtMillis <= 0) {
                errors.add("Frozen calendar state ${state.dayStartMillis} has invalid frozen timestamp.")
            }
        }
    }

    fun toTask(task: com.pseddev.mystreak.data.models.MyStreakExportTask): PieceOrTechnique {
        return PieceOrTechnique(
            name = task.name.trim(),
            color = task.color,
            priority = task.priority,
            minimumSuccess = task.minimumSuccess,
            mediumSuccess = task.mediumSuccess,
            highSuccess = task.highSuccess,
            isActive = task.isActive,
            dateCreated = task.dateCreated,
            lastUpdated = task.lastUpdated
        )
    }

    fun toActivity(
        activity: com.pseddev.mystreak.data.models.MyStreakExportActivity,
        newTaskId: Long
    ): Activity {
        return Activity(
            timestamp = activity.timestamp,
            taskId = newTaskId,
            successLevel = activity.successLevel
        )
    }

    fun toCalendarState(state: com.pseddev.mystreak.data.models.MyStreakExportCalendarState): DailyCalendarState {
        return DailyCalendarState(
            dayStartMillis = state.dayStartMillis,
            colorLevel = state.colorLevel,
            frozenAtMillis = state.frozenAtMillis
        )
    }

    private fun emptyParsed(error: String): ParsedImport {
        return ParsedImport(
            exportData = MyStreakExportData(
                schema = com.pseddev.mystreak.data.models.MyStreakExportInfo(
                    name = "",
                    version = 0,
                    exportedAtMillis = 0,
                    appVersion = ""
                ),
                tasks = emptyList(),
                activities = emptyList(),
                frozenCalendarStates = emptyList()
            ),
            errors = listOf(error),
            warnings = emptyList()
        )
    }
}
