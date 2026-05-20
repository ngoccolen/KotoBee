package com.example.kotobee.ui.community

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kotobee.data.repository.CommunityRepository
import com.example.kotobee.data.service.CloudinaryService
import com.example.kotobee.model.CommunityPost
import com.example.kotobee.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class CreatePostViewModel(
    private val repository: CommunityRepository,
    private val cloudinaryService: CloudinaryService
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    var content by mutableStateOf("")
    var selectedImageUri by mutableStateOf<Uri?>(null)

    var isUploading by mutableStateOf(false)
        private set

    // ĐÃ THÊM: Trạng thái kiểm tra đăng bài thành công
    var isSuccess by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    val currentUserName: String
        get() = auth.currentUser?.displayName
            ?.takeIf { it.isNotBlank() }
            ?: "Người học"

    fun uploadPost() {
        if (isUploading) return

        val currentUser = auth.currentUser
        if (currentUser == null) {
            errorMessage = "Bạn cần đăng nhập để đăng bài"
            return
        }

        val trimmedContent = content.trim()
        val imageUri = selectedImageUri
        if (trimmedContent.isBlank() && imageUri == null) {
            errorMessage = "Hãy nhập nội dung hoặc chọn ảnh"
            return
        }

        viewModelScope.launch {
            isUploading = true
            errorMessage = null

            try {
                val imageUrls = if (imageUri != null) {
                    val uploadedUrl = cloudinaryService.uploadImage(imageUri)
                    if (uploadedUrl == null) {
                        errorMessage = "Không tải ảnh lên được, vui lòng thử lại"
                        return@launch
                    }
                    listOf(uploadedUrl)
                } else {
                    emptyList()
                }

                val newPost = CommunityPost(
                    content = trimmedContent,
                    author = UserProfile(
                        uid = currentUser.uid,
                        username = currentUser.displayName?.takeIf { it.isNotBlank() } ?: "Người học",
                        avatarUrl = currentUser.photoUrl?.toString() ?: "",
                        jlptLevel = "N5",
                        streak = 1
                    ),
                    imageUrls = imageUrls,
                    timestamp = System.currentTimeMillis()
                )

                val result = repository.createPost(newPost)
                if (result.isSuccess) {
                    content = ""
                    selectedImageUri = null
                    isSuccess = true // Đánh dấu thành công để hiện UI Mới
                } else {
                    errorMessage = result.exceptionOrNull()?.message ?: "Không đăng bài được, vui lòng thử lại"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Không đăng bài được, vui lòng thử lại"
            } finally {
                isUploading = false
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }

    fun resetSuccessState() {
        isSuccess = false
    }

    class Factory(
        private val repository: CommunityRepository,
        private val cloudinaryService: CloudinaryService
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CreatePostViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CreatePostViewModel(repository, cloudinaryService) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}