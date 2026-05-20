package com.example.kotobee.data.service

import com.example.kotobee.data.model.ConversationRequest
import com.example.kotobee.data.model.ConversationResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface SpeakingApiService {
    @POST("api/conversation/respond")
    suspend fun respondToConversation(
        @Body request: ConversationRequest
    ): ConversationResponse
}
