
package com.example.kotobee.ui.auth

// sealed class: một danh sách các kịch bản có thể xảy ra
sealed class AuthState {
    object Idle : AuthState() // Chưa làm gì cả
    object Loading : AuthState() // Đang kết nối máy chủ
    data class Success(val isNewUser: Boolean = false) : AuthState()
    data class Error(val message: String) : AuthState()
}