package com.example.kotobee.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotobee.ui.auth.AuthState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class ProfileState(
    val username: String = "",
    val email: String = "",
    val avatarUrl: String = "", // Thêm trường lưu URL avatar
    val jlptLevel: String = "N5",
    val learnedVocab: Int = 0,
    val streak: Int = 0,
    val rankInfo: String = "Top 5%",
    val role: String = "USER"
)

data class ActivityDay(val day: String, val value: Int)

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance() // Thêm Firebase Storage

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState

    private val _activityData = MutableStateFlow<List<ActivityDay>>(emptyList())
    val activityData: StateFlow<List<ActivityDay>> = _activityData

    private val _updateState = MutableStateFlow<AuthState>(AuthState.Idle)
    val updateState: StateFlow<AuthState> = _updateState

    init {
        loadActivityData()
    }

    fun loadUserProfile() {
        val email = auth.currentUser?.email ?: return
        viewModelScope.launch {
            try {
                val snapshot = db.collection("users").whereEqualTo("email", email).get().await()
                if (!snapshot.isEmpty) {
                    val doc = snapshot.documents[0]
                    _profileState.value = ProfileState(
                        username = doc.getString("username") ?: "",
                        email = email,
                        avatarUrl = doc.getString("avatar_url") ?: "", // Load avatar URL
                        jlptLevel = doc.getString("jlpt_level") ?: "N5",
                        learnedVocab = doc.getLong("learned_vocab")?.toInt() ?: 0,
                        streak = doc.getLong("streak")?.toInt() ?: 0,
                        role = doc.getString("role") ?: "USER",
                        rankInfo = doc.getString("rank_info") ?: "Chưa xếp hạng"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Nâng cấp hàm update: Nhận thêm Uri ảnh từ Local
    fun updateProfile(newUsername: String, newJlptLevel: String, newAvatarUri: Uri?) {
        val email = auth.currentUser?.email ?: return

        viewModelScope.launch {
            _updateState.value = AuthState.Loading
            try {
                var downloadUrl = _profileState.value.avatarUrl

                // Nếu có ảnh mới, up lên Firebase Storage trước
                if (newAvatarUri != null) {
                    val fileName = "avatars/${email}_${UUID.randomUUID()}.jpg"
                    val storageRef = storage.reference.child(fileName)
                    storageRef.putFile(newAvatarUri).await()
                    downloadUrl = storageRef.downloadUrl.await().toString()
                }

                // Cập nhật Firestore
                val snapshot = db.collection("users").whereEqualTo("email", email).get().await()
                if (!snapshot.isEmpty) {
                    val docRef = snapshot.documents[0].reference
                    val updates = mapOf(
                        "username" to newUsername,
                        "jlpt_level" to newJlptLevel,
                        "avatar_url" to downloadUrl
                    )
                    docRef.update(updates).await()

                    // Cập nhật lại UI state cục bộ
                    _profileState.value = _profileState.value.copy(
                        username = newUsername,
                        jlptLevel = newJlptLevel,
                        avatarUrl = downloadUrl
                    )
                    _updateState.value = AuthState.Success()
                }
            } catch (e: Exception) {
                _updateState.value = AuthState.Error(e.message ?: "Lỗi khi cập nhật hồ sơ")
            }
        }
    }

    fun resetUpdateState() {
        _updateState.value = AuthState.Idle
    }

    private fun loadActivityData() {
        // Có thể thay bằng logic lấy từ Firestore sau này
        _activityData.value = listOf(
            ActivityDay("TH2", 30), ActivityDay("TH3", 60), ActivityDay("TH4", 90),
            ActivityDay("TH5", 50), ActivityDay("TH6", 75), ActivityDay("TH7", 20),
            ActivityDay("CN", 40)
        )
    }

    fun signOut(onSuccess: () -> Unit) {
        auth.signOut()
        onSuccess()
    }
}