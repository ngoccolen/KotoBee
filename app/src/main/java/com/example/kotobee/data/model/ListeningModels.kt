package com.example.kotobee.data.model

data class WordDetail(
    val word: String = "",
    val reading: String = "",
    val meaning: String = "",
    val isGrammar: Boolean = false
)

data class TranscriptLine(
    val speaker: String = "",
    val jpText: String = "",
    val viText: String = "",
    val vocab: List<WordDetail> = emptyList()
)

data class QuizQuestion(
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctAnswerIndex: Int = 0
)

data class ListeningUiState(
    val isLoading: Boolean = true,
    val lessonTitle: String = "",
    val level: String = "",
    val audioUrl: String = "",
    val youtubeId: String = "", // ĐÃ THÊM: Biến này để nhận ID video từ Firebase
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val totalDuration: Long = 0L,
    val showFurigana: Boolean = true,
    val showTranslation: Boolean = true,
    val playbackSpeed: Float = 1.0f,
    val transcript: List<TranscriptLine> = emptyList(),
    val quizzes: List<QuizQuestion> = emptyList()
)