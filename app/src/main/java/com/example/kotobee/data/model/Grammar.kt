package com.example.kotobee.data.model

// Model cho chi tiết bài học ngữ pháp
data class Grammar(
    val id: String = "",
    val level: String = "N4",
    val title: String = "",
    val meaning: String = "",
    val formation: String = "",
    val usageNote: String = "",
    val examples: List<Example> = emptyList()
)

data class Example(
    val jp: String = "",
    val vi: String = ""
)

// Model cho câu hỏi trắc nghiệm/sắp xếp
data class GrammarQuestion(
    val id: String = "",
    val lessonId: String = "", // Khớp với id của Grammar
    val type: String = "SORTING", // MULTIPLE_CHOICE, SORTING, FILL_BLANK
    val content: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val hint: String = ""
)