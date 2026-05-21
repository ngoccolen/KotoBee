package com.example.kotobee.data.service

import com.example.kotobee.data.model.ConversationRequest
import com.example.kotobee.data.model.ConversationResponse
import com.example.kotobee.data.model.JaViTranslationRequest
import com.example.kotobee.data.model.JaViTranslationResponse
import com.example.kotobee.data.model.SpeakingPairTurnAnalysisResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface SpeakingApiService {
    @POST("api/conversation/respond")
    suspend fun respondToConversation(
        @Body request: ConversationRequest
    ): ConversationResponse

    @Multipart
    @POST("api/speaking-pair/analyze-turn")
    suspend fun analyzeSpeakingPairTurn(
        @Part audio: MultipartBody.Part,
        @Part("transcript_ja") transcriptJa: RequestBody,
        @Part("room_code") roomCode: RequestBody,
        @Part("topic_title") topicTitle: RequestBody,
        @Part("level") level: RequestBody,
        @Part("scenario") scenario: RequestBody,
        @Part("turn_index") turnIndex: RequestBody,
        @Part("recent_messages") recentMessages: RequestBody
    ): SpeakingPairTurnAnalysisResponse

    @POST("api/translation/ja-vi")
    suspend fun translateJaVi(
        @Body request: JaViTranslationRequest
    ): JaViTranslationResponse
}
