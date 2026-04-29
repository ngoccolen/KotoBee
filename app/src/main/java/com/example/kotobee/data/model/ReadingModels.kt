package com.example.kotobee.data.model

data class ReadingLesson(
    val lessonId: String = "",
    val level: String = "",
    val title: String = "",
    val coverImage: String = "",
    val content: String = "",
    val fullTranslation: String = "",
    val audioUrl: String = "",
    val quiz: List<QuizItem> = emptyList()
)

data class QuizItem(
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctIndex: Int = 0
)

data class VocabDetail(
    val vocabId: String = "",
    val word: String = "",
    val furigana: String = "",
    val hanViet: String = "",
    val meaning: String = "",
    val example: String = "",
    val jlptLevel: String = "N3",
    val kanjiInfo: List<KanjiInfo> = emptyList()
)

data class KanjiInfo(
    val character: String = "",
    val onyomi: String = "",
    val kunyomi: String = "",
    val meaning: String = ""
)

data class GrammarDetail(
    val grammarId: String = "",
    val structure: String = "",
    val meaning: String = "",
    val usage: String = "",
    val jlptLevel: String = "N4"
)