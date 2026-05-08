package com.example.kotobee.ui.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LessonViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Hàm được gọi khi người dùng bấm nút "Hoàn thành bài học"
    fun completeLesson(lessonId: String, currentLevel: String) {
        val email = auth.currentUser?.email ?: return

        viewModelScope.launch {
            try {
                // 1. Tìm user document
                val snapshot = db.collection("users").whereEqualTo("email", email).get().await()
                if (snapshot.isEmpty) return@launch

                val docRef = snapshot.documents[0].reference

                // 2. Thêm lessonId vào mảng "completed_lessons" trên Firestore
                // Dùng FieldValue.arrayUnion để không bị trùng lặp bài học
                docRef.update("completed_lessons", FieldValue.arrayUnion(lessonId)).await()

                // 3. Kiểm tra logic Thăng Cấp (Auto Level-up)
                // Giả sử ID của bài test cuối cùng cấp N5 là "N5_FINAL_TEST"
                if (currentLevel == "N5" && lessonId == "N5_FINAL_TEST") {

                    // Cập nhật level mới lên N4
                    docRef.update("jlpt_level", "N4").await()

                    // THÊM: Trigger một event UI ở đây để hiển thị Dialog chúc mừng
                    // Ví dụ: _showLevelUpDialog.value = true
                }

                // Tương tự cho N4 lên N3...
                if (currentLevel == "N4" && lessonId == "N4_FINAL_TEST") {
                    docRef.update("jlpt_level", "N3").await()
                }

            } catch (e: Exception) {
                // Xử lý lỗi (ví dụ: in ra log)
            }
        }
    }
}