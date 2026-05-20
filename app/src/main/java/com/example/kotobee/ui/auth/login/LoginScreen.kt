package com.example.kotobee.ui.auth.login

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kotobee.R
import com.example.kotobee.ui.auth.AuthState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(navController: NavController, viewModel: LoginViewModel = viewModel()) {
    val context = LocalContext.current
    val webClientId = "375139692777-lr9mkpsrja1fg7tdr98j3g1iu7bneblt.apps.googleusercontent.com"
    val googleSignInOptions = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, googleSignInOptions) }

    // THÊM BIẾN NÀY: Dùng để phân biệt đang loading ở nút nào
    var loginType by remember { mutableStateOf("none") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    loginType = "google" // Set kiểu login là google khi có token
                    viewModel.loginWithGoogle(idToken)
                } else {
                    loginType = "none"
                    viewModel.setErrorMessage("Không lấy được Token từ Google")
                }
            } catch (e: ApiException) {
                loginType = "none"
                viewModel.setErrorMessage("Đăng nhập bị huỷ hoặc lỗi mạng")
            }
        } else {
            loginType = "none" // Reset nếu người dùng huỷ popup chọn tài khoản Google
        }
    }

    // Tone màu Đỏ Nhật Bản (Japanese Red)
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color.White, Color.White)
    )
    val primaryButtonGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFE53935), Color(0xFFB71C1C)) // Gradient đỏ tươi sang đỏ sậm
    )
    val primaryTextColor = Color(0xFFB71C1C) // Đỏ sậm
    val primaryAccentColor = Color(0xFFD32F2F) // Đỏ nhấn

    var showForgotDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var resetMessage by remember { mutableStateOf("") }
    var isSuccessMessage by remember { mutableStateOf(false) }
    var forgotStep by remember { mutableStateOf(1) }

    // Biến cho Animation
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .imePadding() // Tránh bị bàn phím che mất
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top, // Đổi sang Top để dễ nhích nội dung lên trên
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Khoảng cách từ đỉnh màn hình xuống ảnh (chỉnh số này để nhích ảnh lên/xuống)
            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { -it / 3 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "bee",
                        modifier = Modifier.size(230.dp)
                    )
                }
            }

            // Khoảng cách giữa ảnh và thẻ form (đã thu hẹp lại cho gần nhau)
            Spacer(modifier = Modifier.height(1.dp))

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(1000)) + slideInVertically(tween(1000)) { it / 3 }
            ) {
                Card(
                    shape = RoundedCornerShape(32.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFB71C1C)),
                    modifier = Modifier.fillMaxWidth().offset(y = (-20).dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Chào mừng trở lại",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        val authState by viewModel.authState.collectAsState()
                        var username by remember { mutableStateOf("") }

                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Tên đăng nhập") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = primaryAccentColor) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            colors = loginTextFieldColors(primaryAccentColor)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        var password by remember { mutableStateOf("") }
                        var passwordVisible by remember { mutableStateOf(false) }

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Mật khẩu") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = primaryAccentColor) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            trailingIcon = {
                                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = image, contentDescription = null)
                                }
                            },
                            colors = loginTextFieldColors(primaryAccentColor)
                        )

                        Text(
                            text = "Quên mật khẩu?",
                            color = primaryAccentColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 12.dp, bottom = 24.dp)
                                .clickable {
                                    showForgotDialog = true
                                    forgotStep = 1
                                    resetEmail = ""
                                    otpCode = ""
                                    newPass = ""
                                    resetMessage = ""
                                }
                        )

                        LaunchedEffect(authState) {
                            if (authState is AuthState.Success) {
                                loginType = "none" // Reset trạng thái
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        }

                        if (authState is AuthState.Error) {
                            Text(
                                text = (authState as AuthState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .padding(bottom = 12.dp)
                                    .align(Alignment.Start)
                            )
                        }

                        // --- NÚT ĐĂNG NHẬP THƯỜNG ---
                        Button(
                            onClick = {
                                loginType = "normal" // Cập nhật biến này
                                viewModel.login(username, password)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(20.dp),
                            enabled = authState != AuthState.Loading,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = primaryButtonGradient,
                                        shape = RoundedCornerShape(20.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                // CHỈ HIỂN THỊ LOADING NẾU ĐANG BẤM NÚT NORMAL
                                if (authState == AuthState.Loading && loginType == "normal") {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Text("Đăng nhập", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // --- NÚT ĐĂNG NHẬP GOOGLE ---
                        OutlinedButton(
                            onClick = {
                                loginType = "google" // Cập nhật biến này
                                googleSignInClient.signOut().addOnCompleteListener {
                                    launcher.launch(googleSignInClient.signInIntent)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(20.dp),
                            enabled = authState != AuthState.Loading
                        ) {
                            // CHỈ HIỂN THỊ LOADING NẾU ĐANG BẤM NÚT GOOGLE
                            if (authState == AuthState.Loading && loginType == "google") {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = primaryAccentColor, strokeWidth = 2.dp)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_google),
                                        contentDescription = "Google Icon",
                                        modifier = Modifier.size(24.dp),
                                        tint = Color.Unspecified
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Đăng nhập bằng Google",
                                    fontSize = 16.sp,
                                    color = Color(0xFF555555),
                                    fontWeight = FontWeight.Medium
                                )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "Chưa có tài khoản? ", color = Color.Gray, fontSize = 14.sp)
                            Text(
                                text = "Đăng ký ngay",
                                color = primaryAccentColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.clickable {
                                    navController.navigate("register")
                                }
                            )
                        }
                    }
                }
            }

            // Spacer dưới cùng để có thể cuộn đẹp hơn
            Spacer(modifier = Modifier.height(6.dp))
        }
    }

    // Dialog quên mật khẩu
    if (showForgotDialog) {
        androidx.compose.material3.AlertDialog(
            // ... (Phần Dialog giữ nguyên như cũ của bạn)
            onDismissRequest = { showForgotDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.border(1.5.dp, Color(0xFFB71C1C), RoundedCornerShape(28.dp)),
            title = { Text(text = if (forgotStep == 1) "Khôi phục mật khẩu" else "Xác nhận OTP", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    if (forgotStep == 1) {
                        Text("Nhập Email của bạn. Chúng tôi sẽ gửi mã OTP gồm 6 chữ số.", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))
                        OutlinedTextField(
                            value = resetEmail, onValueChange = { resetEmail = it },
                            label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                            colors = loginTextFieldColors(primaryAccentColor)
                        )
                    } else {
                        Text("Mã OTP đã được gửi đến $resetEmail", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))
                        OutlinedTextField(
                            value = otpCode, onValueChange = { otpCode = it },
                            label = { Text("Mã OTP 6 số") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = loginTextFieldColors(primaryAccentColor)
                        )
                        OutlinedTextField(
                            value = newPass, onValueChange = { newPass = it },
                            label = { Text("Mật khẩu mới") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                            visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                val image = if (newPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                    Icon(imageVector = image, contentDescription = null)
                                }
                            },
                            colors = loginTextFieldColors(primaryAccentColor)
                        )
                    }
                    if (resetMessage.isNotEmpty()) {
                        Text(text = resetMessage, color = if (isSuccessMessage) Color(0xFF16A34A) else MaterialTheme.colorScheme.error, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        resetMessage = "Đang xử lý..."
                        isSuccessMessage = true
                        if (forgotStep == 1) {
                            viewModel.requestOtp(resetEmail) { success, msg ->
                                isSuccessMessage = success
                                resetMessage = msg
                                if (success) forgotStep = 2
                            }
                        } else {
                            viewModel.verifyAndReset(resetEmail, otpCode, newPass) { success, msg ->
                                isSuccessMessage = success
                                resetMessage = msg
                                if (success) showForgotDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor)
                ) {
                    Text(if (forgotStep == 1) "Gửi OTP" else "Đổi mật khẩu", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showForgotDialog = false }) { Text("Hủy", color = primaryAccentColor) }
            }
        )
    }
}

@Composable
private fun loginTextFieldColors(accent: Color) = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    disabledContainerColor = Color.White,
    errorContainerColor = Color.White,
    focusedBorderColor = accent,
    unfocusedBorderColor = accent.copy(alpha = 0.65f),
    focusedLabelColor = accent,
    unfocusedLabelColor = Color.Gray,
    cursorColor = accent
)
