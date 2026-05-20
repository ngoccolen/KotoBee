package com.example.kotobee.ui.lessons.reading

import org.junit.Assert.assertEquals
import org.junit.Test

class ReadingDifficultyTest {
    @Test
    fun normalizeReadingDifficulty_acceptsEnglishLabels() {
        assertEquals(ReadingDifficulty.EASY, normalizeReadingDifficulty("easy"))
        assertEquals(ReadingDifficulty.MEDIUM, normalizeReadingDifficulty("medium"))
        assertEquals(ReadingDifficulty.HARD, normalizeReadingDifficulty("hard"))
    }

    @Test
    fun normalizeReadingDifficulty_mapsJlptLevels() {
        assertEquals(ReadingDifficulty.EASY, normalizeReadingDifficulty("N5"))
        assertEquals(ReadingDifficulty.MEDIUM, normalizeReadingDifficulty("N4"))
        assertEquals(ReadingDifficulty.HARD, normalizeReadingDifficulty("N3"))
        assertEquals(ReadingDifficulty.HARD, normalizeReadingDifficulty("N2"))
        assertEquals(ReadingDifficulty.HARD, normalizeReadingDifficulty("N1"))
    }

    @Test
    fun normalizeReadingDifficulty_usesDifficultyBeforeLevelAndDefaultsToMedium() {
        assertEquals(ReadingDifficulty.HARD, normalizeReadingDifficulty("hard", "N5"))
        assertEquals(ReadingDifficulty.MEDIUM, normalizeReadingDifficulty(null, null))
        assertEquals(ReadingDifficulty.MEDIUM, normalizeReadingDifficulty(""))
    }
}
