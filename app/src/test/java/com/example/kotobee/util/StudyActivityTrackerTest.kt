package com.example.kotobee.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class StudyActivityTrackerTest {
    @Test
    fun emptyActivity_hasZeroStreak() {
        assertEquals(0, StudyActivityTracker.calculateCurrentStreak(emptySet(), calendar(2026, 5, 15)))
    }

    @Test
    fun activityToday_countsFromToday() {
        val streak = StudyActivityTracker.calculateCurrentStreak(
            setOf("2026-05-15", "2026-05-14", "2026-05-13"),
            calendar(2026, 5, 15)
        )

        assertEquals(3, streak)
    }

    @Test
    fun activityYesterday_keepsCurrentStreak() {
        val streak = StudyActivityTracker.calculateCurrentStreak(
            setOf("2026-05-14", "2026-05-13"),
            calendar(2026, 5, 15)
        )

        assertEquals(2, streak)
    }

    @Test
    fun gapLongerThanOneDay_resetsStreak() {
        val streak = StudyActivityTracker.calculateCurrentStreak(
            setOf("2026-05-13", "2026-05-12"),
            calendar(2026, 5, 15)
        )

        assertEquals(0, streak)
    }

    @Test
    fun invalidDateKeys_areIgnored() {
        val streak = StudyActivityTracker.calculateCurrentStreak(
            setOf("not-a-date", "2026-02-31", "2026-05-15"),
            calendar(2026, 5, 15)
        )

        assertEquals(1, streak)
        assertTrue(StudyActivityTracker.isValidDateKey("2026-05-15"))
        assertFalse(StudyActivityTracker.isValidDateKey("2026-02-31"))
    }

    private fun calendar(year: Int, month: Int, day: Int): Calendar {
        return Calendar.getInstance().apply {
            set(year, month - 1, day, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
}
