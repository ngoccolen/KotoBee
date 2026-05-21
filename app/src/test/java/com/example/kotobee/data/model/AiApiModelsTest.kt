package com.example.kotobee.data.model

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiApiModelsTest {
    private val gson = Gson()

    @Test
    fun speakingPairTurnAnalysisResponse_deserializesSnakeCaseFields() {
        val json = """
            {
              "corrected_sentence_ja": "昨日、学校へ行きました。",
              "natural_sentence_ja": "昨日、学校に行きました。",
              "grammar_feedback_vi": "Câu đã đúng hơn khi dùng thì quá khứ.",
              "pronunciation_feedback_vi": "Âm trường độ nghe hơi ngắn.",
              "summary_vi": "Tốt, cần chú ý trợ từ."
            }
        """.trimIndent()

        val result = gson.fromJson(json, SpeakingPairTurnAnalysisResponse::class.java)

        assertEquals("昨日、学校へ行きました。", result.correctedSentenceJa)
        assertEquals("昨日、学校に行きました。", result.naturalSentenceJa)
        assertEquals("Câu đã đúng hơn khi dùng thì quá khứ.", result.grammarFeedbackVi)
        assertEquals("Âm trường độ nghe hơi ngắn.", result.pronunciationFeedbackVi)
        assertEquals("Tốt, cần chú ý trợ từ.", result.summaryVi)
    }

    @Test
    fun jaViTranslationRequest_serializesContextFields() {
        val request = JaViTranslationRequest(
            text = "新しい制度が始まります。",
            articleTitle = "制度のニュース",
            articleContext = "政府は新しい制度を発表しました。"
        )

        val json = gson.toJson(request)

        assertTrue(json.contains("\"text\""))
        assertTrue(json.contains("\"article_title\""))
        assertTrue(json.contains("\"article_context\""))
    }

    @Test
    fun jaViTranslationResponse_deserializesSnakeCaseTranslation() {
        val result = gson.fromJson(
            """{"translation_vi":"Chế độ mới sẽ bắt đầu.","source":"gemini"}""",
            JaViTranslationResponse::class.java
        )

        assertEquals("Chế độ mới sẽ bắt đầu.", result.translationVi)
        assertEquals("gemini", result.source)
    }
}
