package com.pseddev.mystreak.utils

import android.util.Log
import com.pseddev.mystreak.BuildConfig
import com.pseddev.mystreak.data.entities.Activity
import com.pseddev.mystreak.data.entities.ActivityType
import com.pseddev.mystreak.data.entities.ItemType
import com.pseddev.mystreak.data.entities.PieceOrTechnique
import com.pseddev.mystreak.utils.TextNormalizer
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import java.io.Reader
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.*

class CsvHandler {

    companion object {
        // Enable detailed logging only when needed for debugging
        private const val ENABLE_DETAILED_LOGGING = false
        private const val DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
        private val datetimeFormatter = SimpleDateFormat(DATETIME_FORMAT, Locale.US)

        // CSV Headers
        private const val HEADER_DATE = "DateTime"
        private const val HEADER_LENGTH = "Length"
        private const val HEADER_ACTIVITY_TYPE = "ActivityType"
        private const val HEADER_PIECE = "Piece"
        private const val HEADER_PIECE_TYPE = "PieceType"
        private const val HEADER_LEVEL = "Level"
        private const val HEADER_PERFORMANCE_TYPE = "PerformanceType"
        private const val HEADER_NOTES = "Notes"

        fun exportActivitiesToCsv(
            writer: Writer,
            activities: List<Activity>,
            pieces: Map<Long, PieceOrTechnique>
        ) {
            if (BuildConfig.DEBUG && ENABLE_DETAILED_LOGGING) {
                Log.d("ExportDebug", "CsvHandler.exportActivitiesToCsv called with ${activities.size} activities")
            }

            try {
                // Write CSV manually to avoid OpenCSV issues
                val bufferedWriter = writer.buffered()

                // Write header
                val header = listOf(
                    HEADER_DATE,
                    HEADER_LENGTH,
                    HEADER_ACTIVITY_TYPE,
                    HEADER_PIECE,
                    HEADER_PIECE_TYPE,
                    HEADER_LEVEL,
                    HEADER_PERFORMANCE_TYPE,
                    HEADER_NOTES
                )
                bufferedWriter.write(header.joinToString(",") { escapeCSVField(it) })
                bufferedWriter.write("\n")

                // Write activities
                var rowCount = 0
                activities.forEach { activity ->
                    val piece = pieces[activity.pieceOrTechniqueId]
                    if (piece != null) {
                        val datetime = datetimeFormatter.format(Date(activity.timestamp))
                        val row = listOf(
                            datetime,
                            activity.minutes.toString(),
                            activity.activityType.toString(),
                            TextNormalizer.normalizePieceName(piece.name),
                            piece.type.toString(),
                            activity.level.toString(),
                            activity.performanceType,
                            TextNormalizer.normalizeUserInput(activity.notes)
                        )
                        bufferedWriter.write(row.joinToString(",") { escapeCSVField(it) })
                        bufferedWriter.write("\n")
                        rowCount++

                        if (BuildConfig.DEBUG && ENABLE_DETAILED_LOGGING && rowCount % 50 == 0) {
                            Log.d("ExportDebug", "Written $rowCount rows so far")
                        }
                    }
                }

                // Just flush, don't close
                bufferedWriter.flush()

                if (BuildConfig.DEBUG && ENABLE_DETAILED_LOGGING) {
                    Log.d("ExportDebug", "CsvHandler.exportActivitiesToCsv completed successfully with $rowCount rows")
                }

            } catch (e: Exception) {
                Log.e("ExportDebug", "Exception in CsvHandler.exportActivitiesToCsv: ${e.javaClass.simpleName} - ${e.message}", e)
                throw e
            }
        }

        private fun escapeCSVField(field: String): String {
            return if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
                "\"${field.replace("\"", "\"\"")}\""
            } else {
                field
            }
        }

        fun importActivitiesFromCsv(reader: Reader): ImportResult {
            val csvReader = CSVReader(reader)
            val importedActivities = mutableListOf<ImportedActivity>()
            val errors = mutableListOf<String>()
            val uniquePieces = mutableSetOf<String>()

            try {
                // Skip header
                val header = csvReader.readNext()
                if (header == null || !validateHeader(header)) {
                    errors.add("Invalid CSV header format")
                    return ImportResult(emptyList(), emptySet(), errors)
                }

                // Determine if this is the new format with PieceType
                val isNewFormat = header.size >= 8 && header[4] == HEADER_PIECE_TYPE

                var lineNumber = 2 // Start from 2 (after header)
                var line: Array<String>?

                while (csvReader.readNext().also { line = it } != null) {
                    try {
                        val row = line!!
                        if (row.size < 6) {
                            errors.add("Line $lineNumber: Invalid number of columns")
                            lineNumber++
                            continue
                        }

                        // Parse datetime
                        val timestamp = try {
                            datetimeFormatter.parse(row[0])?.time ?: throw Exception("Invalid datetime")
                        } catch (e: Exception) {
                            errors.add("Line $lineNumber: Invalid datetime format '${row[0]}' (expected: yyyy-MM-dd HH:mm:ss)")
                            lineNumber++
                            continue
                        }

                        // Parse minutes
                        val minutes = try {
                            row[1].toInt()
                        } catch (e: Exception) {
                            errors.add("Line $lineNumber: Invalid minutes value '${row[1]}'")
                            lineNumber++
                            continue
                        }

                        // Parse activity type
                        val activityType = try {
                            ActivityType.valueOf(row[2])
                        } catch (e: Exception) {
                            errors.add("Line $lineNumber: Invalid activity type '${row[2]}'")
                            lineNumber++
                            continue
                        }

                        // Parse piece name and normalize it
                        val originalName = row[3]
                        val pieceName = TextNormalizer.normalizePieceName(originalName)

                        if (BuildConfig.DEBUG && ENABLE_DETAILED_LOGGING) {
                            Log.d("CsvImport", "Line $lineNumber: Original='$originalName', Normalized='$pieceName'")
                        }

                        if (pieceName.isEmpty()) {
                            errors.add("Line $lineNumber: Empty piece name")
                            lineNumber++
                            continue
                        }

                        // Parse piece type (only in new format)
                        val pieceType = if (isNewFormat) {
                            try {
                                ItemType.valueOf(row[4])
                            } catch (e: Exception) {
                                // Default to PIECE if invalid or missing
                                ItemType.PIECE
                            }
                        } else {
                            null
                        }

                        // Parse level (column index depends on format)
                        val levelColumnIndex = if (isNewFormat) 5 else 4
                        val level = try {
                            row[levelColumnIndex].toInt()
                        } catch (e: Exception) {
                            errors.add("Line $lineNumber: Invalid level '${row[levelColumnIndex]}'")
                            lineNumber++
                            continue
                        }

                        // Validate level range
                        when (activityType) {
                            ActivityType.PRACTICE -> {
                                if (level !in 1..4) {
                                    errors.add("Line $lineNumber: Invalid practice level $level (must be 1-4)")
                                    lineNumber++
                                    continue
                                }
                            }
                            ActivityType.PERFORMANCE -> {
                                if (level !in 1..3) {
                                    errors.add("Line $lineNumber: Invalid performance level $level (must be 1-3)")
                                    lineNumber++
                                    continue
                                }
                            }
                        }

                        // Parse performance type and notes (column indices depend on format)
                        val performanceTypeIndex = if (isNewFormat) 6 else 5
                        val notesIndex = if (isNewFormat) 7 else 6

                        val performanceType = row[performanceTypeIndex]
                        val notes = if (row.size > notesIndex) TextNormalizer.normalizeUserInput(row[notesIndex]) else ""

                        // Explicitly check for duplicates to work around Set issues
                        val alreadyExists = uniquePieces.any { existing ->
                            existing == pieceName
                        }

                        if (!alreadyExists) {
                            uniquePieces.add(pieceName)
                        }

                        importedActivities.add(
                            ImportedActivity(
                                timestamp = timestamp,
                                pieceName = pieceName,
                                pieceType = pieceType,
                                activityType = activityType,
                                level = level,
                                performanceType = performanceType,
                                minutes = minutes,
                                notes = notes
                            )
                        )

                    } catch (e: Exception) {
                        errors.add("Line $lineNumber: ${e.message}")
                    }

                    lineNumber++
                }

            } finally {
                csvReader.close()
            }

            if (BuildConfig.DEBUG) {
                Log.d("CsvImport", "Import completed: ${importedActivities.size} activities, ${uniquePieces.size} unique pieces")
            }

            return ImportResult(importedActivities, uniquePieces, errors)
        }

        private fun validateHeader(header: Array<String>): Boolean {
            if (header.size < 6) return false

            // Support both old format (7 columns) and new format (8 columns with PieceType)
            val isOldFormat = header.size == 7 &&
                    header[0] == HEADER_DATE &&
                    header[1] == HEADER_LENGTH &&
                    header[2] == HEADER_ACTIVITY_TYPE &&
                    header[3] == HEADER_PIECE &&
                    header[4] == HEADER_LEVEL &&
                    header[5] == HEADER_PERFORMANCE_TYPE

            val isNewFormat = header.size >= 8 &&
                    header[0] == HEADER_DATE &&
                    header[1] == HEADER_LENGTH &&
                    header[2] == HEADER_ACTIVITY_TYPE &&
                    header[3] == HEADER_PIECE &&
                    header[4] == HEADER_PIECE_TYPE &&
                    header[5] == HEADER_LEVEL &&
                    header[6] == HEADER_PERFORMANCE_TYPE

            return isOldFormat || isNewFormat
        }

        /**
         * Validates a CSV file without importing it, checking piece and activity limits
         * @param reader Reader for the CSV file
         * @param maxPieces Maximum allowed pieces for this user type
         * @param maxActivities Maximum allowed activities for this user type
         * @return CsvValidationResult with counts and validation status
         */
        fun validateCsv(reader: Reader, maxPieces: Int, maxActivities: Int): CsvValidationResult {
            val csvReader = CSVReader(reader)
            val errors = mutableListOf<String>()
            val uniquePieces = mutableSetOf<String>()
            var activityCount = 0

            try {
                // Skip header
                val header = csvReader.readNext()
                if (header == null || !validateHeader(header)) {
                    return CsvValidationResult(
                        isValid = false,
                        activityCount = 0,
                        uniquePieceCount = 0,
                        errors = listOf("Invalid CSV header format")
                    )
                }

                // Count activities and unique pieces
                var row: Array<String>?
                while (csvReader.readNext().also { row = it } != null) {
                    row?.let { currentRow ->
                        if (currentRow.size >= 6) {
                            activityCount++

                            // Extract piece name (column 3 in both formats)
                            val pieceName = TextNormalizer.normalizePieceName(currentRow[3])
                            if (pieceName.isNotBlank()) {
                                uniquePieces.add(pieceName)
                            }
                        }
                    }
                }

                // Check limits
                if (uniquePieces.size > maxPieces) {
                    errors.add("This file contains ${uniquePieces.size} unique pieces, but the limit is $maxPieces pieces.")
                }

                if (activityCount > maxActivities) {
                    errors.add("This file contains $activityCount activities, but the limit is $maxActivities activities.")
                }

                return CsvValidationResult(
                    isValid = errors.isEmpty(),
                    activityCount = activityCount,
                    uniquePieceCount = uniquePieces.size,
                    errors = errors
                )

            } catch (e: Exception) {
                return CsvValidationResult(
                    isValid = false,
                    activityCount = activityCount,
                    uniquePieceCount = uniquePieces.size,
                    errors = listOf("Error reading CSV file: ${e.message}")
                )
            } finally {
                try {
                    csvReader.close()
                } catch (e: Exception) {
                    // Ignore close errors
                }
            }
        }
    }

    data class ImportedActivity(
        val timestamp: Long,
        val pieceName: String,
        val pieceType: ItemType?,
        val activityType: ActivityType,
        val level: Int,
        val performanceType: String,
        val minutes: Int,
        val notes: String
    )

    data class ImportResult(
        val activities: List<ImportedActivity>,
        val uniquePieceNames: Set<String>,
        val errors: List<String>
    )

    data class CsvValidationResult(
        val isValid: Boolean,
        val activityCount: Int,
        val uniquePieceCount: Int,
        val errors: List<String>
    )
}