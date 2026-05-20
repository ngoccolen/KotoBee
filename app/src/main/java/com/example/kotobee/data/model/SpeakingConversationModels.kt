package com.example.kotobee.data.model

import com.google.gson.annotations.SerializedName

data class ConversationTopic(
    val id: String = "",
    val title: String = "",
    val level: String = "N5",
    val description: String = "",
    val scenario: String = "",
    val aiRole: String = "",
    val starterMessageJa: String = "",
    val starterHintVi: String = "",
    val sortOrder: Int = Int.MAX_VALUE,
    val tags: List<String> = emptyList()
)

data class SpeakingConversation(
    val id: String = "",
    val topicId: String = "",
    val topicTitle: String = "",
    val level: String = "N5",
    val messageCount: Int = 0,
    val lastMessageJa: String = ""
)

data class SpeakingMessage(
    val id: String = "",
    val role: String = "user",
    val textJa: String = "",
    val translationVi: String = "",
    val feedbackVi: String = "",
    val correctionJa: String = "",
    val turnIndex: Int = 0,
    val source: String = "conversation"
)

data class ConversationApiMessage(
    @SerializedName("role") val role: String,
    @SerializedName("text_ja") val textJa: String
)

data class ConversationRequest(
    @SerializedName("topic_id") val topicId: String,
    @SerializedName("level") val level: String,
    @SerializedName("topic_title") val topicTitle: String,
    @SerializedName("scenario") val scenario: String,
    @SerializedName("ai_role") val aiRole: String,
    @SerializedName("user_text") val userText: String,
    @SerializedName("recent_messages") val recentMessages: List<ConversationApiMessage>
)

data class ConversationResponse(
    @SerializedName("reply_text_ja") val replyTextJa: String,
    @SerializedName("translation_vi") val translationVi: String? = null,
    @SerializedName("feedback_vi") val feedbackVi: String? = null,
    @SerializedName("correction_ja") val correctionJa: String? = null
)
