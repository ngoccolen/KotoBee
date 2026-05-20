package com.example.kotobee.data.model

import com.google.gson.annotations.SerializedName

data class ShadowingResponse(
    @SerializedName("expected_text") val expectedText: String,
    @SerializedName("recognized_text") val recognizedText: String,
    @SerializedName("score") val score: Int,
    @SerializedName("summary") val summary: String,
    @SerializedName("tokens") val tokens: List<TokenFeedback>,
    @SerializedName("missing_parts") val missingParts: List<String>,
    @SerializedName("extra_parts") val extraParts: List<String>,
    @SerializedName("grammar_feedback") val grammarFeedback: GrammarFeedback?
)

data class TokenFeedback(
    @SerializedName("text") val text: String,
    @SerializedName("status") val status: String, // "correct", "wrong", "missing", "extra"
    @SerializedName("message") val message: String
)

data class GrammarFeedback(
    @SerializedName("corrected_sentence") val correctedSentence: String,
    @SerializedName("natural_sentence") val naturalSentence: String,
    @SerializedName("issues") val issues: List<GrammarIssue>,
    @SerializedName("short_comment_vi") val shortCommentVi: String
)

data class GrammarIssue(
    @SerializedName("wrong") val wrong: String,
    @SerializedName("correct") val correct: String,
    @SerializedName("reason_vi") val reasonVi: String
)

data class ShadowingLesson(
    val id: String = "",
    val level: String = "N5",
    val title: String = "",
    val japanese: String = "",
    val furigana: String = "",
    val expectedUnits: List<String> = emptyList(),
    val sortOrder: Int = Int.MAX_VALUE
)
