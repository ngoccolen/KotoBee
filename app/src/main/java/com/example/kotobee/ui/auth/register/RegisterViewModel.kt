package com.example.kotobee.ui.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotobee.ui.auth.AuthState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterViewModel : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun register(username: String, email: String, pass: String, checkPass: String) {
        if (username.isBlank() || email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Vui lòng điền đầy đủ thông tin!")
            return
        }
        if (pass != checkPass) {
            _authState.value = AuthState.Error("Mật khẩu xác nhận không khớp!")
            return
        }
        if (pass.length < 6) {
            _authState.value = AuthState.Error("Mật khẩu phải có ít nhất 6 ký tự!")
            return
        }

        _authState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document(username).get().await()
                if (userDoc.exists()) {
                    _authState.value = AuthState.Error("Tên đăng nhập này đã có người sử dụng!")
                    return@launch
                }

                auth.createUserWithEmailAndPassword(email, pass).await()

                val userProfile = hashMapOf(
                    "email" to email,
                    "username" to username,
                    "created_at" to System.currentTimeMillis(),
                    "jlpt_level" to "N5",
                    "learned_vocab" to 0,
                    "role" to "user"
                )

                db.collection("users").document(username).set(userProfile).await()
                _authState.value = AuthState.Success(isNewUser = true)

            } catch (e: Exception) {
                // ĐÃ THÊM LOGIC CHECK TRÙNG EMAIL Ở ĐÂY
                if (e is FirebaseAuthUserCollisionException) {
                    _authState.value = AuthState.Error("Email này đã được sử dụng để đăng ký!")
                } else {
                    _authState.value = AuthState.Error(e.localizedMessage ?: "Đã xảy ra lỗi hệ thống!")
                }
            }
        }
    }
}