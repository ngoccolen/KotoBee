package com.example.kotobee.data.service

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class CloudinaryService(context: Context) {
    init {
        // Khởi tạo một lần duy nhất. Thay bằng thông tin của bạn từ Cloudinary Dashboard
        val config = mapOf(
            "cloud_name" to "dp7tq1zns",
            "api_key" to "863381775223727",
            "api_secret" to "T90fojFRpR_9rxC99cG4_vk2C3I"
        )
        try {
            MediaManager.init(context, config)
        } catch (e: Exception) { /* Đã khởi tạo */ }
    }

    suspend fun uploadImage(uri: Uri): String? = suspendCancellableCoroutine { continuation ->
        MediaManager.get().upload(uri)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String
                    continuation.resume(url)
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    continuation.resume(null)
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }
}