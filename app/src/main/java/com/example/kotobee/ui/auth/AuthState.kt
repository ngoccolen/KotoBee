
package com.example.kotobee.ui.auth

// sealed class: một danh sách các kịch bản có thể xảy ra
sealed class AuthState {
    object Idle : AuthState() // Chưa làm gì cả
    object Loading : AuthState() // Đang kết nối máy chủ
    object Success : AuthState() // Thành công! -> chuyển màn hình)
    data class Error(val message: String) : AuthState() // Thất bại
}