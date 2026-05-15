package com.pseddev.mystreak.utils

import com.pseddev.mystreak.data.entities.Activity
import com.pseddev.mystreak.data.entities.PieceOrTechnique
import com.pseddev.mystreak.data.entities.TaskKind
import com.pseddev.mystreak.data.entities.TaskPriority
import java.util.Calendar

class StreakCalculator {

    fun calculateCurrentStreak(
        activities: List<Activity>,
        tasks: List<PieceOrTechnique>
    ): Int {
        val highPriorityTaskIds = activeHighPriorityStandardTaskIds(tasks)
        if (activities.isEmpty() || highPriorityTaskIds.isEmpty()) return 0

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Check if there was activity today
        val todayStart = today.timeInMillis
        val todayEndCalendar = today.clone() as Calendar
        todayEndCalendar.add(Calendar.DAY_OF_YEAR, 1)
        val todayEnd = todayEndCalendar.timeInMillis

        val qualifiesToday = qualifiesForStreak(activities, highPriorityTaskIds, todayStart, todayEnd)

        // Start from today if it qualifies, otherwise start from yesterday.
        val startDate = if (qualifiesToday) {
            today.clone() as Calendar
        } else {
            val yesterday = today.clone() as Calendar
            yesterday.add(Calendar.DAY_OF_YEAR, -1)
            yesterday
        }

        var streak = 0
        val currentDate = startDate.clone() as Calendar

        while (true) {
            val dayStart = currentDate.timeInMillis
            val dayEndCalendar = currentDate.clone() as Calendar
            dayEndCalendar.add(Calendar.DAY_OF_YEAR, 1)
            val dayEnd = dayEndCalendar.timeInMillis

            if (qualifiesForStreak(activities, highPriorityTaskIds, dayStart, dayEnd)) {
                streak++
                currentDate.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        return streak
    }

    /**
     * Find the date when a specific streak milestone was first achieved
     * Returns null if the milestone was never reached
     */
    fun findStreakMilestoneDate(
        activities: List<Activity>,
        tasks: List<PieceOrTechnique>,
        milestone: Int
    ): Long? {
        if (activities.isEmpty() || milestone <= 0) return null

        val qualifiedDates = qualifiedDayStarts(activities, tasks).sorted()
        if (qualifiedDates.size < milestone) return null

        // Find all consecutive streak periods and check for milestone
        var currentStreakStart = 0

        for (i in 1 until qualifiedDates.size) {
            val currentDate = qualifiedDates[i]
            val previousDate = qualifiedDates[i - 1]

            // Check if dates are consecutive (difference of exactly 1 day)
            val daysDifference = (currentDate - previousDate) / DAY_MILLIS

            if (daysDifference == 1L) {
                // Still in a streak
                val currentStreakLength = i - currentStreakStart + 1

                // Check if we've reached the milestone
                if (currentStreakLength >= milestone) {
                    // Return the date when the milestone was completed
                    // (milestone days from start of this streak)
                    val milestoneDate = qualifiedDates[currentStreakStart + milestone - 1]
                    return milestoneDate
                }
            } else {
                // Streak broken, start new streak from current position
                currentStreakStart = i
            }
        }

        // Check if the final streak (including just the first date) meets the milestone
        val finalStreakLength = qualifiedDates.size - currentStreakStart
        if (finalStreakLength >= milestone) {
            val milestoneDate = qualifiedDates[currentStreakStart + milestone - 1]
            return milestoneDate
        }

        return null
    }

    /**
     * Get all streak periods from historical data for debugging/analysis
     */
    fun getAllStreakPeriods(
        activities: List<Activity>,
        tasks: List<PieceOrTechnique>
    ): List<Pair<Int, Long>> {
        val practiceDates = qualifiedDayStarts(activities, tasks).sorted()
        if (practiceDates.isEmpty()) return emptyList()

        val streakPeriods = mutableListOf<Pair<Int, Long>>()

        var currentStreakStart = 0

        for (i in 1 until practiceDates.size) {
            val currentDate = practiceDates[i]
            val previousDate = practiceDates[i - 1]
            val daysDifference = (currentDate - previousDate) / DAY_MILLIS

            if (daysDifference != 1L) {
                // End of streak period
                val streakLength = i - currentStreakStart
                if (streakLength > 1) {
                    streakPeriods.add(Pair(streakLength, practiceDates[currentStreakStart + streakLength - 1]))
                }
                currentStreakStart = i
            }
        }

        // Handle final streak
        val finalStreakLength = practiceDates.size - currentStreakStart
        if (finalStreakLength > 1) {
            streakPeriods.add(Pair(finalStreakLength, practiceDates.last()))
        }

        return streakPeriods
    }

    fun qualifiesForStreakDay(
        activities: List<Activity>,
        tasks: List<PieceOrTechnique>,
        dayStart: Long
    ): Boolean {
        val highPriorityTaskIds = activeHighPriorityStandardTaskIds(tasks)
        if (highPriorityTaskIds.isEmpty()) return false
        return qualifiesForStreak(activities, highPriorityTaskIds, dayStart, dayStart + DAY_MILLIS)
    }

    private fun qualifiedDayStarts(
        activities: List<Activity>,
        tasks: List<PieceOrTechnique>
    ): Set<Long> {
        val highPriorityTaskIds = activeHighPriorityStandardTaskIds(tasks)
        if (activities.isEmpty() || highPriorityTaskIds.isEmpty()) return emptySet()

        return activities
            .map { startOfDay(it.timestamp) }
            .toSet()
            .filter { dayStart ->
                qualifiesForStreak(activities, highPriorityTaskIds, dayStart, dayStart + DAY_MILLIS)
            }
            .toSet()
    }

    private fun activeHighPriorityStandardTaskIds(tasks: List<PieceOrTechnique>): Set<Long> {
        return tasks
            .filter {
                it.isActive &&
                    it.taskKind == TaskKind.STANDARD &&
                    it.priority == TaskPriority.HIGH
            }
            .map { it.id }
            .toSet()
    }

    private fun qualifiesForStreak(
        activities: List<Activity>,
        highPriorityTaskIds: Set<Long>,
        dayStart: Long,
        dayEnd: Long
    ): Boolean {
        val completedHighPriorityTaskIds = activities
            .asSequence()
            .filter { it.timestamp >= dayStart && it.timestamp < dayEnd }
            .map { it.taskId }
            .filter { it in highPriorityTaskIds }
            .toSet()

        val threshold = (highPriorityTaskIds.size + 1) / 2
        return completedHighPriorityTaskIds.size >= threshold
    }

    private fun startOfDay(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private companion object {
        const val DAY_MILLIS = 24L * 60L * 60L * 1000L
    }
}
