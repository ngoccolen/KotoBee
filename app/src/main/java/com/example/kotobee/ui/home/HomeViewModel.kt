package com.example.kotobee.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

// Model cho tiến độ kỹ năng (Lưu dạng Map trên Firebase cho linh hoạt)
data class UserProfile(
    val username: String = "",
    val email: String = "",
    val current_level: String = "N5",
    val next_level: String = "N4",
    val level_progress: Float = 0.3f, // Ví dụ: 30% chặng đường từ N5 lên N4
    val skills_progress: Map<String, Float> = mapOf(
        "Từ vựng" to 0.6f,
        "Ngữ pháp" to 0.4f,
        "Nghe hiểu" to 0.2f,
        "Đọc hiểu" to 0.1f
    ),
    val streak: Int = 0,
    val role: String = "USER"
)

// Model cho Task hằng ngày của người dùng
data class DailyTask(
    val id: String = "",
    val title: String = "",
    val current: Int = 0,
    val target: Int = 1
)

class HomeViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile

    private val _dailyTasks = MutableStateFlow<List<DailyTask>>(emptyList())
    val dailyTasks: StateFlow<List<DailyTask>> = _dailyTasks

    private var currentUserDocId: String? = null

    fun loadUserData() {
        val currentUser = auth.currentUser ?: return
        val email = currentUser.email ?: return

        viewModelScope.launch {
            try {
                // 1. Lấy thông tin User
                val snapshot = db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .await()

                if (!snapshot.isEmpty) {
                    val doc = snapshot.documents[0]
                    currentUserDocId = doc.id
                    val data = doc.toObject(UserProfile::class.java)
                    if (data != null) {
                        _userProfile.value = data
                    }

                    // 2. Lấy danh sách nhiệm vụ hằng ngày của user này
                    loadDailyTasks()
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Lỗi tải dữ liệu: ${e.message}")
            }
        }
    }

    private suspend fun loadDailyTasks() {
        currentUserDocId?.let { docId ->
            try {
                val tasksSnapshot = db.collection("users")
                    .document(docId)
                    .collection("daily_tasks")
                    .get()
                    .await()

                val tasks = tasksSnapshot.documents.mapNotNull { it.toObject(DailyTask::class.java) }
                _dailyTasks.value = tasks
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Lỗi tải Tasks: ${e.message}")
            }
        }
    }

    // Hàm để người dùng tự thêm nhiệm vụ mới lưu thẳng lên Firebase
    fun addNewDailyTask(title: String, target: Int) {
        val docId = currentUserDocId ?: return
        val taskId = UUID.randomUUID().toString()
        val newTask = DailyTask(id = taskId, title = title, current = 0, target = target)

        viewModelScope.launch {
            try {
                db.collection("users")
                    .document(docId)
                    .collection("daily_tasks")
                    .document(taskId)
                    .set(newTask)
                    .await()

                // Cập nhật lại UI sau khi thêm
                val currentList = _dailyTasks.value.toMutableList()
                currentList.add(newTask)
                _dailyTasks.value = currentList
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Lỗi thêm Task: ${e.message}")
            }
        }
    }

    // Tùy chọn: Hàm để tăng tiến độ task (VD: Nút tick hoàn thành)
    fun incrementTaskProgress(task: DailyTask) {
        val docId = currentUserDocId ?: return
        if (task.current >= task.target) return

        val newCurrent = task.current + 1

        viewModelScope.launch {
            try {
                db.collection("users")
                    .document(docId)
                    .collection("daily_tasks")
                    .document(task.id)
                    .update("current", newCurrent)
                    .await()

                loadDailyTasks() // Tải lại danh sách để cập nhật thanh progress
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Lỗi cập nhật Task: ${e.message}")
            }
        }
    }

    fun signOut(onSuccess: () -> Unit) {
        auth.signOut()
        onSuccess()
    }
}