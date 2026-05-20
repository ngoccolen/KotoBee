package com.example.kotobee.data.model

data class SpeakingPairRoom(
    val code: String = "",
    val topicId: String = "",
    val topicTitle: String = "",
    val level: String = "N5",
    val scenario: String = "",
    val hostUserId: String = "",
    val status: String = "waiting",
    val currentTurnUserId: String = "",
    val turnIndex: Int = 0,
    val participantCount: Int = 0,
    val messageCount: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val expiresAt: Long = 0L,
    val endedReason: String = ""
)

data class SpeakingPairParticipant(
    val userId: String = "",
    val username: String = "",
    val avatarUrl: String = "",
    val joinedAt: Long = 0L,
    val isHost: Boolean = false,
    val isOnline: Boolean = true
)

data class SpeakingPairMessage(
    val id: String = "",
    val senderUserId: String = "",
    val senderName: String = "",
    val audioUrl: String = "",
    val transcriptJa: String = "",
    val turnIndex: Int = 0,
    val durationMs: Long = 0L,
    val createdAt: Long = 0L
)

data class SpeakingPairHistory(
    val roomCode: String = "",
    val topicTitle: String = "",
    val partnerName: String = "",
    val status: String = "waiting",
    val messageCount: Int = 0,
    val updatedAt: Long = 0L
)
