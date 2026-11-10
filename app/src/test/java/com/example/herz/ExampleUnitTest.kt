package com.example.herz

import org.junit.Assert.*
import org.junit.Test

class DaysTest {
    @Test fun weekIncludesTodayAndCrossesYearBoundary() {
        assertEquals(listOf("2025-12-29", "2025-12-30", "2025-12-31", "2026-01-01", "2026-01-02", "2026-01-03", "2026-01-04"), Days.week("2026-01-04"))
    }
    @Test fun monthNavigationStartsAtFirstAndHandlesLeapYear() {
        assertEquals("2024-02-01", Days.month("2024-01-31", 1))
        assertEquals(29, Days.monthLength("2024-02-01"))
        assertEquals(28, Days.monthLength("2025-02-01"))
        assertEquals("2025-12-01", Days.month("2026-01-15", -1))
    }
    @Test fun calendarUsesMondayFirst() {
        assertEquals(0, Days.mondayOffset("2026-06-01"))
        assertEquals(6, Days.mondayOffset("2026-11-01"))
    }
    @Test fun weeklyStatsExcludeMissingOldAndFutureDays() {
        val entries = listOf(CheckIn("2026-11-03", 1, 1, "old"), CheckIn("2026-11-04", 3, 2, ""),
            CheckIn("2026-11-10", 5, 4, "today"), CheckIn("2026-11-11", 1, 1, "future"))
        val week = weeklyEntries(entries, "2026-11-10")
        assertEquals(2, week.size)
        assertEquals(3.0, week.map { it.energy }.average(), 0.001)
        assertTrue(weeklyEntries(emptyList(), "2026-11-10").isEmpty())
    }
}
