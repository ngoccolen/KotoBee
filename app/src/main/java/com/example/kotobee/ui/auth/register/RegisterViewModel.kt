package com.example.kotobee.ui.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotobee.ui.auth.AuthState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterViewModel : ViewModel() {

    // 1. Tạo biến lưu trữ trạng thái hiện tại.
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // 2. Gọi công cụ của Firebase
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // 3. Hàm thực thi khi người dùng bấm nút Đăng ký
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

        // viewModelScope.launch giúp chạy ngầm các tác vụ mạng mà không làm đơ ứng dụng
        viewModelScope.launch {
            try {
                // kiểm tra trùng tn
                // Lệnh .await() bắt code phải dừng lại chờ máy chủ trả lời rồi mới chạy dòng tiếp theo
                val userDoc = db.collection("users").document(username).get().await()
                if (userDoc.exists()) {
                    _authState.value = AuthState.Error("Tên đăng nhập này đã có người sử dụng!")
                    return@launch
                }

                // tạo tài khoàn
                auth.createUserWithEmailAndPassword(email, pass).await()

                // lưu hồ sơ
                val userProfile = hashMapOf(
                    "email" to email,
                    "username" to username,
                    "created_at" to System.currentTimeMillis(),
                    "jlpt_level" to "N5", // Trình độ tiếng Nhật mặc định khi mới vào
                    "learned_vocab" to 0,  // Số từ vựng đã học
                    "role" to "user"
                )

                db.collection("users").document(username).set(userProfile).await()

                _authState.value = AuthState.Success

            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Đã xảy ra lỗi hệ thống!")
            }
        }
    }
}