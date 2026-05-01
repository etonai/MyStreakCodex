package com.pseddev.mystreak.utils

import android.text.format.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

object DateFormatter {

    /**
     * Formats a timestamp to a user-friendly relative or absolute date string
     */
    fun formatDate(timestamp: Long?): String {
        if (timestamp == null) return "Never"
        if (timestamp == 0L) return "Never"

        val now = System.currentTimeMillis()
        val diff = now - timestamp

        // For dates within the last week, show relative time
        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                "${minutes}m ago"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "${hours}h ago"
            }
            diff < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                "${days}d ago"
            }
            else -> {
                // For older dates, show absolute date
                DateFormat.format("MMM d, yyyy", Date(timestamp)).toString()
            }
        }
    }

    /**
     * Formats a timestamp to just the date (MMM d, yyyy format)
     */
    fun formatDateOnly(timestamp: Long?): String {
        if (timestamp == null) return "Never"
        if (timestamp == 0L) return "Never"

        return DateFormat.format("MMM d, yyyy", Date(timestamp)).toString()
    }

    /**
     * Formats a timestamp to include both date and time
     */
    fun formatDateTime(timestamp: Long?): String {
        if (timestamp == null) return "Never"
        if (timestamp == 0L) return "Never"

        return DateFormat.format("MMM d, yyyy 'at' h:mm a", Date(timestamp)).toString()
    }

    /**
     * Formats a date with fallback text for null/zero values
     */
    fun formatDateWithFallback(timestamp: Long?, fallbackText: String = "N/A"): String {
        if (timestamp == null || timestamp == 0L) return fallbackText
        return formatDate(timestamp)
    }

    /**
     * Formats time duration between two timestamps
     */
    fun formatTimeSince(timestamp: Long?): String {
        if (timestamp == null || timestamp == 0L) return "Never"

        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Less than a minute ago"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                "$minutes minute${if (minutes == 1L) "" else "s"} ago"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "$hours hour${if (hours == 1L) "" else "s"} ago"
            }
            diff < TimeUnit.DAYS.toMillis(30) -> {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                "$days day${if (days == 1L) "" else "s"} ago"
            }
            diff < TimeUnit.DAYS.toMillis(365) -> {
                val months = TimeUnit.MILLISECONDS.toDays(diff) / 30
                "$months month${if (months == 1L) "" else "s"} ago"
            }
            else -> {
                val years = TimeUnit.MILLISECONDS.toDays(diff) / 365
                "$years year${if (years == 1L) "" else "s"} ago"
            }
        }
    }
}