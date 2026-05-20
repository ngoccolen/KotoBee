package com.example.kotobee.ui.lessons.grammar

import com.example.kotobee.data.model.Grammar
import com.example.kotobee.data.model.GrammarProgress
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GrammarUnlockTest {
    @Test
    fun firstLesson_isUnlocked_whenNoProgressExists() {
        val access = buildGrammarLessonAccess(
            lessons = listOf(grammar("g1"), grammar("g2")),
            progress = emptyMap()
        )

        assertTrue(access[0].unlocked)
        assertFalse(access[1].unlocked)
    }

    @Test
    fun nextLesson_unlocksOnlyAfterPreviousLessonCompleted() {
        val access = buildGrammarLessonAccess(
            lessons = listOf(grammar("g1"), grammar("g2"), grammar("g3")),
            progress = mapOf("g1" to GrammarProgress(grammarId = "g1", completed = true, bestScore = 80))
        )

        assertTrue(access[0].completed)
        assertTrue(access[1].unlocked)
        assertFalse(access[2].unlocked)
    }

    private fun grammar(id: String): Grammar {
        return Grammar(id = id, title = id, level = "N5")
    }
}
