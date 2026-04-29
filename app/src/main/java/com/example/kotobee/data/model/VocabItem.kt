package com.example.kotobee.data.model

data class VocabItem(
    val id: String = "",
    val deckId: String = "",
    val kanji: String = "",
    val kana: String = "",
    val meaning: String = "",
    val example: String = "",
    val exampleMeaning: String = "",
    val nextReviewTime: Long = 0L,
    val level: Int = 0
)
