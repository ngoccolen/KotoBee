package com.example.kotobee.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotobee.ui.auth.AuthState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class LoginViewModel : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun login(username: String, pass: String) {
        if (username.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Vui lòng nhập Tên đăng nhập và Mật khẩu!")
            return
        }

        _authState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                // tìm email dựa vào username
                val userDoc = db.collection("users").document(username).get().await()
                val emailOfUser = userDoc.getString("email")

                // Nếu không tìm thấy email -> Tên đăng nhập sai
                if (emailOfUser == null) {
                    _authState.value = AuthState.Error("Tên đăng nhập không tồn tại!")
                    return@launch
                }

                // đăng ký email rồi thì đăng nhập
                auth.signInWithEmailAndPassword(emailOfUser, pass).await()
                _authState.value = AuthState.Success(isNewUser = false)

            } catch (e: Exception) {
                _authState.value = AuthState.Error("Sai mật khẩu hoặc mất kết nối mạng!")
            }
        }
    }
    fun loginWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                // 1. Tạo credential từ Google ID Token
                val credential = GoogleAuthProvider.getCredential(idToken, null)

                // 2. Đăng nhập vào Firebase Auth
                val authResult = auth.signInWithCredential(credential).await()
                val user = authResult.user

                // 3. Đồng bộ dữ liệu lên Firestore (để giữ cấu trúc "users" của bạn không bị lỗi)
                if (user != null && user.email != null) {
                    // Tạo username tạm từ email (VD: abc@gmail.com -> abc)
                    val generatedUsername = user.email!!.substringBefore("@")

                    val userDocRef = db.collection("users").document(generatedUsername)
                    val snapshot = userDocRef.get().await()

                    // Nếu là người dùng mới, lưu thông tin vào Firestore
                    if (!snapshot.exists()) {
                        val userData = hashMapOf(
                            "username" to generatedUsername, // Bắt buộc phải có để HomeViewModel đọc được
                            "email" to user.email,
                            "displayName" to (user.displayName ?: generatedUsername),
                            "authProvider" to "google",
                            "jlpt_level" to "N5",            // Thêm các thông số mặc định giống UserProfile
                            "placement_level" to "",
                            "learning_goal" to "",
                            "focus_skills" to emptyList<String>(),
                            "onboarding_completed" to true,
                            "learned_vocab" to 0,
                            "streak" to 0,
                            "role" to "user"
                        )
                        userDocRef.set(userData).await()
                    }
                }
                _authState.value = AuthState.Success(isNewUser = false)

            } catch (e: Exception) {
                _authState.value = AuthState.Error("Đăng nhập Google thất bại: ${e.localizedMessage}")
            }
        }
    }

    // Hàm hỗ trợ để set trạng thái lỗi từ UI nếu Google Sign-in bị huỷ
    fun setErrorMessage(message: String) {
        _authState.value = AuthState.Error(message)
    }
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    // THAY IP BẰNG IPv4 CỦA MÁY TÍNH BẠN TÌM ĐƯỢC Ở BƯỚC 1
    private val baseUrl = "https://kotobeebe-production.up.railway.app/api"

    fun requestOtp(email: String, onResult: (Boolean, String) -> Unit) {
        if (email.isBlank()) {
            onResult(false, "Vui lòng nhập Email!")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = JSONObject().apply { put("email", email) }
                val body = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder().url("$baseUrl/request-otp").post(body).build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody ?: "{}")
                    val isSuccess = jsonResponse.optBoolean("success", false)
                    val msg = if (isSuccess) "Đã gửi mã OTP. Vui lòng kiểm tra email!" else jsonResponse.optString("error", "Lỗi gửi mail")

                    withContext(Dispatchers.Main) { onResult(isSuccess, msg) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onResult(false, "Không thể kết nối máy chủ: ${e.message}") }
            }
        }
    }

    fun verifyAndReset(email: String, otp: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        if (otp.isBlank() || newPass.isBlank()) {
            onResult(false, "Vui lòng nhập đủ OTP và Mật khẩu mới!")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("email", email)
                    put("otp", otp)
                    put("newPassword", newPass)
                }
                val body = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder().url("$baseUrl/reset-password").post(body).build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody ?: "{}")
                    val isSuccess = jsonResponse.optBoolean("success", false)
                    val msg = if (isSuccess) "Đổi mật khẩu thành công!" else jsonResponse.optString("error", "OTP không đúng hoặc đã hết hạn")

                    withContext(Dispatchers.Main) { onResult(isSuccess, msg) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onResult(false, "Không thể kết nối máy chủ: ${e.message}") }
            }
        }
    }
}
