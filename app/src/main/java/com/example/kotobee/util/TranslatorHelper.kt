package com.example.kotobee.util

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

class TranslatorHelper {
    private val options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.JAPANESE)
        .setTargetLanguage(TranslateLanguage.VIETNAMESE)
        .build()

    private val translator: Translator = Translation.getClient(options)
    // Đánh dấu đã tải model xong, tránh tải lại mỗi lần dịch
    private var isModelReady = false

    suspend fun downloadModelIfNeeded(): Boolean {
        if (isModelReady) return true
        return try {
            // Bỏ requireWifi() để hoạt động cả trên mobile data
            val conditions = DownloadConditions.Builder().build()
            translator.downloadModelIfNeeded(conditions).await()
            isModelReady = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun translateText(text: String): String {
        return try {
            translator.translate(text).await()
        } catch (e: Exception) {
            "Lỗi dịch: ${e.message}"
        }
    }

    fun close() {
        translator.close()
    }
}
