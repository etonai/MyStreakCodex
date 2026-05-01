package com.pseddev.mystreak.utils

import com.pseddev.mystreak.data.entities.Activity
import java.util.Calendar

class StreakCalculator {

    fun calculateCurrentStreak(activities: List<Activity>): Int {
        if (activities.isEmpty()) return 0

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

        val hasActivityToday = activities.any {
            it.timestamp >= todayStart && it.timestamp < todayEnd
        }

        // Start from today if there's activity today, otherwise start from yesterday
        val startDate = if (hasActivityToday) {
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

            val hasActivity = activities.any {
                it.timestamp >= dayStart && it.timestamp < dayEnd
            }

            if (hasActivity) {
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
    fun findStreakMilestoneDate(activities: List<Activity>, milestone: Int): Long? {
        if (activities.isEmpty() || milestone <= 0) return null

        // Group activities by date (day level)
        val activitiesByDate = activities.groupBy { activity ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = activity.timestamp
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        }

        // Get sorted list of practice dates
        val practiceDates = activitiesByDate.keys.sorted()
        if (practiceDates.size < milestone) return null

        // Find all consecutive streak periods and check for milestone
        var currentStreakStart = 0

        for (i in 1 until practiceDates.size) {
            val currentDate = practiceDates[i]
            val previousDate = practiceDates[i - 1]

            // Check if dates are consecutive (difference of exactly 1 day)
            val millisecondsPerDay = 24 * 60 * 60 * 1000L
            val daysDifference = (currentDate - previousDate) / millisecondsPerDay

            if (daysDifference == 1L) {
                // Still in a streak
                val currentStreakLength = i - currentStreakStart + 1

                // Check if we've reached the milestone
                if (currentStreakLength >= milestone) {
                    // Return the date when the milestone was completed
                    // (milestone days from start of this streak)
                    val milestoneDate = practiceDates[currentStreakStart + milestone - 1]
                    return milestoneDate
                }
            } else {
                // Streak broken, start new streak from current position
                currentStreakStart = i
            }
        }

        // Check if the final streak (including just the first date) meets the milestone
        val finalStreakLength = practiceDates.size - currentStreakStart
        if (finalStreakLength >= milestone) {
            val milestoneDate = practiceDates[currentStreakStart + milestone - 1]
            return milestoneDate
        }

        return null
    }

    /**
     * Get all streak periods from historical data for debugging/analysis
     */
    fun getAllStreakPeriods(activities: List<Activity>): List<Pair<Int, Long>> {
        if (activities.isEmpty()) return emptyList()

        // Group activities by date
        val activitiesByDate = activities.groupBy { activity ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = activity.timestamp
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        }

        val practiceDates = activitiesByDate.keys.sorted()
        val streakPeriods = mutableListOf<Pair<Int, Long>>()

        var currentStreakStart = 0
        val millisecondsPerDay = 24 * 60 * 60 * 1000L

        for (i in 1 until practiceDates.size) {
            val currentDate = practiceDates[i]
            val previousDate = practiceDates[i - 1]
            val daysDifference = (currentDate - previousDate) / millisecondsPerDay

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
}