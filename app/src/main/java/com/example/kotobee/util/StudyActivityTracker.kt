package com.example.kotobee.util

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object StudyActivityTracker {
    private const val DATE_PATTERN = "yyyy-MM-dd"

    fun todayDateKey(calendar: Calendar = Calendar.getInstance()): String {
        return dateFormatter().format(calendar.time)
    }

    fun currentWeekDateKeys(today: Calendar = Calendar.getInstance()): Set<String> {
        val start = (today.clone() as Calendar).apply {
            clearTime()
            val daysFromMonday = (get(Calendar.DAY_OF_WEEK) + 5) % 7
            add(Calendar.DAY_OF_YEAR, -daysFromMonday)
        }
        return dateKeysBetween(start, today)
    }

    fun currentMonthDateKeys(today: Calendar = Calendar.getInstance()): Set<String> {
        val start = (today.clone() as Calendar).apply {
            clearTime()
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return dateKeysBetween(start, today)
    }

    fun isValidDateKey(value: String): Boolean {
        val position = ParsePosition(0)
        val parsed = dateFormatter().parse(value, position)
        return parsed != null && position.index == value.length
    }

    fun calculateCurrentStreak(
        activeDateKeys: Set<String>,
        today: Calendar = Calendar.getInstance()
    ): Int {
        val activeDates = activeDateKeys.filter(::isValidDateKey).toSet()
        if (activeDates.isEmpty()) return 0

        val cursor = (today.clone() as Calendar).apply { clearTime() }
        if (!activeDates.contains(todayDateKey(cursor))) {
            cursor.add(Calendar.DAY_OF_YEAR, -1)
            if (!activeDates.contains(todayDateKey(cursor))) return 0
        }

        var streak = 0
        while (activeDates.contains(todayDateKey(cursor))) {
            streak += 1
            cursor.add(Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }

    suspend fun recordStudyActivity(
        userRef: DocumentReference,
        points: Long,
        source: String
    ) {
        val safePoints = points.coerceAtLeast(0L)
        if (safePoints == 0L) return

        val dateKey = todayDateKey()
        userRef.collection("study_activity")
            .document(dateKey)
            .set(
                mapOf(
                    "date" to dateKey,
                    "value" to FieldValue.increment(safePoints),
                    "source" to source,
                    "sourcePoints" to mapOf(source to FieldValue.increment(safePoints)),
                    "sources" to FieldValue.arrayUnion(source),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            .await()
    }

    private fun dateKeysBetween(start: Calendar, end: Calendar): Set<String> {
        val cursor = (start.clone() as Calendar).apply { clearTime() }
        val normalizedEnd = (end.clone() as Calendar).apply { clearTime() }
        val keys = mutableSetOf<String>()

        while (!cursor.after(normalizedEnd)) {
            keys += todayDateKey(cursor)
            cursor.add(Calendar.DAY_OF_YEAR, 1)
        }

        return keys
    }

    private fun dateFormatter(): SimpleDateFormat {
        return SimpleDateFormat(DATE_PATTERN, Locale.US).apply {
            isLenient = false
        }
    }
}

fun Calendar.clearTime() {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}
