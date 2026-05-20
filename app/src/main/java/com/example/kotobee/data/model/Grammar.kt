package com.example.kotobee.data.model

data class Grammar(
    val id: String = "",
    val level: String = "N5",
    val title: String = "",
    val romaji: String = "",
    val meaning: String = "",
    val summary: String = "",
    val formation: String = "",
    val usageNote: String = "",
    val tags: List<String> = emptyList(),
    val sourceName: String = "",
    val sourceUrl: String = "",
    val examples: List<Example> = emptyList(),
    val sortOrder: Int = Int.MAX_VALUE,
    val questionCount: Int = 0
)

data class Example(
    val jp: String = "",
    val romaji: String = "",
    val vi: String = "",
    val en: String = ""
)

data class GrammarQuestion(
    val id: String = "",
    val lessonId: String = "",
    val type: String = "MULTIPLE_CHOICE",
    val content: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val hint: String = ""
)

data class GrammarProgress(
    val grammarId: String = "",
    val level: String = "N5",
    val completed: Boolean = false,
    val bestScore: Int = 0,
    val correctCount: Int = 0,
    val totalQuestions: Int = 0
)

data class GrammarQuizSaveResult(
    val passed: Boolean,
    val awardedPoints: Boolean,
    val bestScore: Int
)
