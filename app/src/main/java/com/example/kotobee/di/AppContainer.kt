package com.example.kotobee.di

import android.content.Context
import android.util.Log
import com.example.kotobee.data.repository.CommunityRepository
import com.example.kotobee.data.repository.CommunityRepositoryImpl
import com.example.kotobee.data.service.CloudinaryService
import com.example.kotobee.data.repository.ShadowingRepository
import com.example.kotobee.data.repository.SpeakingConversationRepository
import com.example.kotobee.data.repository.SpeakingPairRepository
import com.example.kotobee.data.service.ShadowingApiService
import com.example.kotobee.data.service.SpeakingApiService
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {

    companion object {
        private const val SHADOWING_API_BASE_URL = "https://kotobee-be.onrender.com/"
    }

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    val cloudinaryService: CloudinaryService by lazy {
        CloudinaryService(context.applicationContext)
    }

    val communityRepository: CommunityRepository by lazy {
        CommunityRepositoryImpl(firestore)
    }

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor { message ->
            Log.d("ShadowingAPI", message)
        }.apply {
            // Đừng để BODY vì nó sẽ log cả file audio ra Logcat, rất lag và rối
            level = HttpLoggingInterceptor.Level.BASIC
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(90, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(SHADOWING_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val shadowingApiService: ShadowingApiService by lazy {
        retrofit.create(ShadowingApiService::class.java)
    }

    val speakingApiService: SpeakingApiService by lazy {
        retrofit.create(SpeakingApiService::class.java)
    }

    val shadowingRepository: ShadowingRepository by lazy {
        ShadowingRepository(shadowingApiService, firestore)
    }

    val speakingConversationRepository: SpeakingConversationRepository by lazy {
        SpeakingConversationRepository(speakingApiService, firestore)
    }

    val speakingPairRepository: SpeakingPairRepository by lazy {
        SpeakingPairRepository(firestore = firestore, apiService = speakingApiService)
    }
}
