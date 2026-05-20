package com.example.kotobee.data.repository

import com.example.kotobee.data.model.GrammarQuestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GrammarQuestionSelectionTest {
    @Test
    fun mergeGrammarQuizQuestions_dedupesAndLimitsToTen() {
        val embedded = (1..6).map { index ->
            question(id = "embedded_$index", content = "Question $index")
        }
        val external = (1..8).map { index ->
            question(id = "external_$index", content = "External $index")
        } + question(id = "duplicate", content = "Question 1")

        val selected = mergeGrammarQuizQuestions(
            embeddedQuestions = embedded,
            externalQuestions = external,
            shuffle = false
        )

        assertEquals(10, selected.size)
        assertEquals(10, selected.map { it.content.lowercase() to it.correctAnswer.lowercase() }.toSet().size)
        assertTrue(selected.any { it.id == "external_1" })
        assertTrue(selected.none { it.id == "duplicate" })
    }

    private fun question(id: String, content: String): GrammarQuestion {
        return GrammarQuestion(
            id = id,
            lessonId = "lesson_1",
            content = content,
            options = listOf("A", "B", "C", "D"),
            correctAnswer = "A",
            hint = "hint"
        )
    }
}
