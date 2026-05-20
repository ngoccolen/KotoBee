package com.example.kotobee.data.repository

import android.util.Log
import com.example.kotobee.data.model.ShadowingLesson
import com.example.kotobee.data.model.ShadowingResponse
import com.example.kotobee.data.service.ShadowingApiService
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ShadowingRepository(
    private val apiService: ShadowingApiService,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    companion object {
        private const val TAG = "ShadowingRepository"
        private const val MAX_RETRIES = 2
        private const val SHADOWING_COLLECTION = "shadowing_lessons"
    }

    suspend fun getShadowingLessons(): List<ShadowingLesson> = withContext(Dispatchers.IO) {
        firestore.collection(SHADOWING_COLLECTION)
            .get()
            .await()
            .documents
            .filter { it.isPublished() }
            .mapNotNull { it.toShadowingLesson() }
            .sortedWith(compareBy<ShadowingLesson> { levelRank(it.level) }.thenBy { it.sortOrder }.thenBy { it.title })
    }

    suspend fun analyzeShadowing(
        audioFile: File,
        expectedText: String,
        expectedUnitsJson: String,
        level: String,
        checkGrammar: Boolean
    ): Result<ShadowingResponse> = withContext(Dispatchers.IO) {
        var lastException: Exception? = null

        for (attempt in 0..MAX_RETRIES) {
            try {
                if (attempt > 0) {
                    Log.d(TAG, "Retry attempt $attempt/$MAX_RETRIES")
                    delay(2000L * attempt) // Exponential backoff
                }

                val audioRequestBody = audioFile.asRequestBody("audio/mp4".toMediaTypeOrNull())
                val audioPart = MultipartBody.Part.createFormData("audio", audioFile.name, audioRequestBody)

                val textBody = expectedText.toRequestBody("text/plain".toMediaTypeOrNull())
                val unitsBody = expectedUnitsJson.toRequestBody("text/plain".toMediaTypeOrNull())
                val levelBody = level.toRequestBody("text/plain".toMediaTypeOrNull())
                val grammarBody = checkGrammar.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                val response = apiService.analyzeShadowing(
                    audio = audioPart,
                    expectedText = textBody,
                    expectedUnits = unitsBody,
                    level = levelBody,
                    checkGrammar = grammarBody
                )
                return@withContext Result.success(response)

            } catch (e: SocketTimeoutException) {
                Log.w(TAG, "Timeout on attempt $attempt: ${e.message}")
                lastException = e
                // Retry on timeout (cold start)

            } catch (e: UnknownHostException) {
                Log.e(TAG, "No internet connection: ${e.message}")
                return@withContext Result.failure(
                    Exception("Không có kết nối mạng. Vui lòng kiểm tra WiFi/4G.")
                )

            } catch (e: java.net.ConnectException) {
                Log.e(TAG, "Cannot connect to server: ${e.message}")
                return@withContext Result.failure(
                    Exception("Không thể kết nối đến server. Server có thể đang khởi động, vui lòng thử lại sau 30 giây.")
                )

            } catch (e: retrofit2.HttpException) {
                val code = e.code()
                val errorBody = e.response()?.errorBody()?.string()
                Log.e(TAG, "HTTP $code: $errorBody")

                return@withContext when (code) {
                    400 -> Result.failure(Exception("Dữ liệu gửi lên không hợp lệ: $errorBody"))
                    502 -> Result.failure(Exception("Lỗi xử lý từ server. Vui lòng thử lại."))
                    503 -> {
                        lastException = e
                        continue // Retry on 503 (service unavailable / cold start)
                    }
                    else -> Result.failure(Exception("Lỗi server ($code). Vui lòng thử lại."))
                }

            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error: ${e.message}", e)
                lastException = e
                if (attempt == MAX_RETRIES) {
                    break
                }
            }
        }

        Result.failure(
            lastException ?: Exception("Lỗi không xác định. Vui lòng thử lại.")
        )
    }

    private fun DocumentSnapshot.toShadowingLesson(): ShadowingLesson? {
        val id = firstString("shadowing_id", "id").ifBlank { this.id }
        val title = firstString("title")
        val japanese = firstString("japanese", "expected_text", "text")
        if (id.isBlank() || title.isBlank() || japanese.isBlank()) return null

        return ShadowingLesson(
            id = id,
            level = firstString("level").ifBlank { "N5" },
            title = title,
            japanese = japanese,
            furigana = firstString("furigana", "reading"),
            expectedUnits = stringList("expected_units", "expectedUnits"),
            sortOrder = numberValue("sort_order") ?: Int.MAX_VALUE
        )
    }

    private fun DocumentSnapshot.isPublished(): Boolean {
        val status = firstString("status")
        return status.isBlank() || status == "published"
    }

    private fun DocumentSnapshot.firstString(vararg fields: String): String {
        return fields.firstNotNullOfOrNull { field ->
            getString(field)?.trim()?.takeIf { it.isNotBlank() }
        }.orEmpty()
    }

    private fun DocumentSnapshot.stringList(vararg fields: String): List<String> {
        fields.forEach { field ->
            val value = get(field)
            val list = when (value) {
                is List<*> -> value.mapNotNull { it?.toString()?.trim()?.takeIf(String::isNotBlank) }
                is String -> value
                    .split(Regex("[\\r\\n,\\u3001]+"))
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                else -> emptyList()
            }
            if (list.isNotEmpty()) return list
        }
        return emptyList()
    }

    private fun DocumentSnapshot.numberValue(vararg fields: String): Int? {
        fields.forEach { field ->
            when (val value = get(field)) {
                is Number -> return value.toInt()
                is String -> value.toIntOrNull()?.let { return it }
            }
        }
        return null
    }

    private fun levelRank(level: String): Int {
        return when (level.uppercase()) {
            "N5" -> 0
            "N4" -> 1
            "N3" -> 2
            "N2" -> 3
            "N1" -> 4
            else -> 5
        }
    }
}
