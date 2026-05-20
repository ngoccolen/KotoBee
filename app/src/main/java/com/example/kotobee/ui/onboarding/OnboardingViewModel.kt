package com.example.kotobee.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotobee.ui.auth.AuthState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OnboardingViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _onboardingState = MutableStateFlow<AuthState>(AuthState.Idle)
    val onboardingState: StateFlow<AuthState> = _onboardingState

    fun saveInitialSurvey(
        selectedLevel: String,
        learningGoal: String,
        focusSkills: List<String>
    ) {
        val email = auth.currentUser?.email ?: return
        _onboardingState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                val snapshot = db.collection("users").whereEqualTo("email", email).get().await()
                if (snapshot.isEmpty) {
                    _onboardingState.value = AuthState.Error("Không tìm thấy hồ sơ người dùng")
                    return@launch
                }

                val normalizedSkills = focusSkills.ifEmpty { listOf("Từ vựng", "Ngữ pháp", "Hán tự") }
                val updates = mapOf(
                    "jlpt_level" to selectedLevel,
                    "placement_level" to selectedLevel,
                    "learning_goal" to learningGoal,
                    "focus_skills" to normalizedSkills,
                    "onboarding_completed" to true,
                    "completed_lessons" to emptyList<String>(),
                    "skills_progress" to normalizedSkills.associateWith { 0f },
                    "updated_at" to System.currentTimeMillis()
                )

                snapshot.documents.first().reference.update(updates).await()
                _onboardingState.value = AuthState.Success()
            } catch (e: Exception) {
                _onboardingState.value = AuthState.Error(e.message ?: "Lỗi lưu dữ liệu")
            }
        }
    }

    fun resetState() {
        _onboardingState.value = AuthState.Idle
    }
}
