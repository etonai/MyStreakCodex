package com.pseddev.mystreak

import com.pseddev.mystreak.data.entities.Activity
import com.pseddev.mystreak.data.entities.PieceOrTechnique
import com.pseddev.mystreak.data.entities.TaskKind
import com.pseddev.mystreak.data.entities.TaskPriority
import com.pseddev.mystreak.utils.StreakCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class StreakCalculatorTest {

    private val calculator = StreakCalculator()

    @Test
    fun threeHighPriorityTasksRequireTwoDistinctHighPriorityActivities() {
        val dayStart = startOfDay(0)
        val tasks = listOf(
            task(1, TaskPriority.HIGH),
            task(2, TaskPriority.HIGH),
            task(3, TaskPriority.HIGH)
        )

        assertFalse(
            calculator.qualifiesForStreakDay(
                activities = listOf(activity(1, dayStart)),
                tasks = tasks,
                dayStart = dayStart
            )
        )

        assertTrue(
            calculator.qualifiesForStreakDay(
                activities = listOf(activity(1, dayStart), activity(2, dayStart)),
                tasks = tasks,
                dayStart = dayStart
            )
        )
    }

    @Test
    fun duplicateActivitiesForOneHighPriorityTaskCountOnce() {
        val dayStart = startOfDay(0)
        val tasks = listOf(
            task(1, TaskPriority.HIGH),
            task(2, TaskPriority.HIGH),
            task(3, TaskPriority.HIGH)
        )

        val duplicateActivities = listOf(
            activity(1, dayStart),
            activity(1, dayStart + 60_000)
        )

        assertFalse(calculator.qualifiesForStreakDay(duplicateActivities, tasks, dayStart))
    }

    @Test
    fun lowPriorityAndRoutineActivitiesDoNotQualifyDay() {
        val dayStart = startOfDay(0)
        val tasks = listOf(
            task(1, TaskPriority.HIGH),
            task(2, TaskPriority.LOW),
            task(3, TaskPriority.HIGH, TaskKind.ROUTINE)
        )

        val activities = listOf(
            activity(2, dayStart),
            activity(3, dayStart)
        )

        assertFalse(calculator.qualifiesForStreakDay(activities, tasks, dayStart))
    }

    @Test
    fun currentStreakUsesOnlyQualifiedRecentDays() {
        val today = startOfDay(0)
        val yesterday = startOfDay(-1)
        val twoDaysAgo = startOfDay(-2)
        val tasks = listOf(
            task(1, TaskPriority.HIGH),
            task(2, TaskPriority.HIGH)
        )

        val activities = listOf(
            activity(1, today),
            activity(2, yesterday),
            activity(1, twoDaysAgo + 60_000)
        )

        assertEquals(3, calculator.calculateCurrentStreak(activities, tasks))
    }

    @Test
    fun lowPriorityGapYesterdayBreaksStreakEvenWhenTodayAndDayBeforeQualify() {
        val today = startOfDay(0)
        val yesterday = startOfDay(-1)
        val twoDaysAgo = startOfDay(-2)
        val tasks = listOf(
            task(1, TaskPriority.HIGH),
            task(2, TaskPriority.HIGH),
            task(3, TaskPriority.LOW)
        )

        val activities = listOf(
            activity(1, today),
            activity(3, yesterday),
            activity(2, twoDaysAgo)
        )

        assertEquals(1, calculator.calculateCurrentStreak(activities, tasks))
    }

    @Test
    fun noActiveHighPriorityStandardTasksMeansNoStreak() {
        val dayStart = startOfDay(0)
        val tasks = listOf(
            task(1, TaskPriority.LOW),
            task(2, TaskPriority.HIGH, TaskKind.ROUTINE)
        )

        assertEquals(
            0,
            calculator.calculateCurrentStreak(
                activities = listOf(activity(1, dayStart), activity(2, dayStart)),
                tasks = tasks
            )
        )
    }

    private fun task(
        id: Long,
        priority: TaskPriority,
        taskKind: TaskKind = TaskKind.STANDARD
    ): PieceOrTechnique {
        return PieceOrTechnique(
            id = id,
            name = "Task $id",
            priority = priority,
            taskKind = taskKind
        )
    }

    private fun activity(taskId: Long, timestamp: Long): Activity {
        return Activity(
            id = 0,
            timestamp = timestamp,
            taskId = taskId
        )
    }

    private fun startOfDay(dayOffset: Int): Long {
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, dayOffset)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
