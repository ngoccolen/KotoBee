package com.example.kotobee.data.service

import com.example.kotobee.data.model.ShadowingResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ShadowingApiService {
    @Multipart
    @POST("api/shadowing/analyze")
    suspend fun analyzeShadowing(
        @Part audio: MultipartBody.Part,
        @Part("expected_text") expectedText: RequestBody,
        @Part("expected_units") expectedUnits: RequestBody,
        @Part("level") level: RequestBody,
        @Part("check_grammar") checkGrammar: RequestBody
    ): ShadowingResponse
}
