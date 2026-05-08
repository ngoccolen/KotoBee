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

    suspend fun downloadModelIfNeeded(): Boolean {
        return try {
            val conditions = DownloadConditions.Builder()
                .requireWifi()
                .build()
            translator.downloadModelIfNeeded(conditions).await()
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
            "Translation Error: ${e.message}"
        }
    }

    fun close() {
        translator.close()
    }
}
