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

    fun saveInitialLevel(selectedLevel: String, onSuccess: () -> Unit) {
        val email = auth.currentUser?.email ?: return
        _onboardingState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                val snapshot = db.collection("users").whereEqualTo("email", email).get().await()
                if (!snapshot.isEmpty) {
                    val docRef = snapshot.documents[0].reference

                    // Khởi tạo mảng completed_lessons rỗng luôn ở đây để dùng cho Phần 2
                    val updates = mapOf(
                        "jlpt_level" to selectedLevel,
                        "completed_lessons" to emptyList<String>()
                    )

                    docRef.update(updates).await()
                    _onboardingState.value = AuthState.Success()
                    onSuccess()
                }
            } catch (e: Exception) {
                _onboardingState.value = AuthState.Error(e.message ?: "Lỗi lưu dữ liệu")
            }
        }
    }
}