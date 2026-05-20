package com.example.kotobee.ui.profile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ProfileActivityWindowTest {
    @Test
    fun buildCurrentWeekActivity_returnsMondayToSunday() {
        val activity = buildCurrentWeekActivity(today = calendar(2026, 5, 20))

        assertEquals(listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN"), activity.map { it.day })
        assertEquals("2026-05-18", activity.first().dateKey)
        assertEquals("2026-05-24", activity.last().dateKey)
    }

    @Test
    fun buildCurrentWeekActivity_mapsValuesByDateAndZerosFutureDays() {
        val activity = buildCurrentWeekActivity(
            valuesByDate = mapOf(
                "2026-05-18" to 10,
                "2026-05-20" to 30,
                "2026-05-21" to 50
            ),
            today = calendar(2026, 5, 20)
        )

        assertEquals(10, activity[0].value)
        assertEquals(30, activity[2].value)
        assertEquals(0, activity[3].value)
        assertTrue(activity[2].isToday)
    }

    private fun calendar(year: Int, month: Int, day: Int): Calendar {
        return Calendar.getInstance().apply {
            set(year, month - 1, day, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
}
