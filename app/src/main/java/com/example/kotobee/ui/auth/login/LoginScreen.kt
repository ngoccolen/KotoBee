package com.example.kotobee.ui.auth.login

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, googleSignInOptions)
    }

    // Launcher để mở màn hình chọn tài khoản Google
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    viewModel.loginWithGoogle(idToken) // Gọi ViewModel xử lý
                } else {
                    viewModel.setErrorMessage("Không lấy được Token từ Google")
                }
            } catch (e: ApiException) {
                viewModel.setErrorMessage("Đăng nhập bị huỷ hoặc lỗi mạng")
            }
        }
    }
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFFDFBF7), Color(0xFFF4E1E6))
    )
    val primaryButtonGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFB56A7A), Color(0xFFD68A9A))
    )
    // Các biến cho Hộp thoại OTP
    var showForgotDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var resetMessage by remember { mutableStateOf("") }
    var isSuccessMessage by remember { mutableStateOf(false) }
    var forgotStep by remember { mutableStateOf(1) } // 1: Nhập Email, 2: Nhập OTP


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center //căn giữa theo chiều dọc
        ) {
            //Tiêu đề app
            Text(
                text = "KotoBee",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF8A3A4A),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp)
            )
            //Ảnh logo app
            Image(
                painter = painterResource(id = R.drawable.bee),
                contentDescription = "bee",
                modifier = Modifier
                    .size(90.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(32.dp))

            //Tạo card nhập lieu
            Card(
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
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
                    //Tạo khoảng cách
                    Spacer(modifier = Modifier.height(24.dp))

                    // Lắng nghe trạng thái từ ViewModel
                    val authState by viewModel.authState.collectAsState()

                    //tạo biến lưu input
                    var username by remember { mutableStateOf("") }
                    //mutableStateOf: giá trị có thể thay đổi
                    //remember → lưu state khi UI re-render
                    //username: chứa dữ liệu người dùng nhập

                    //Ô nhập username
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Tên đăng nhập") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedBorderColor = Color(0xFFB56A7A),
                            focusedLabelColor = Color(0xFFB56A7A)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    var password by remember { mutableStateOf("") }
                    var passwordVisible by remember { mutableStateOf(false) }

                    //Ô nhập password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Mật khẩu") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
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
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedBorderColor = Color(0xFFB56A7A),
                            focusedLabelColor = Color(0xFFB56A7A)
                        )
                    )

                    Text(
                        text = "Quên mật khẩu?",
                        color = Color(0xFFB56A7A),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 12.dp, bottom = 24.dp)
                            .clickable {
                                // Gán lệnh mở hộp thoại vào đây!
                                showForgotDialog = true
                                forgotStep = 1
                                resetEmail = ""
                                otpCode = ""
                                newPass = ""
                                resetMessage = ""
                            }
                    )

                    //Tự động chuyển màn hình nếu trạng thái là Success
                    LaunchedEffect(authState) {
                        if (authState is AuthState.Success) {
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

                    //Nút đăng nhập
                    Button(
                        onClick = { viewModel.login(username, password) },
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
                            if (authState == AuthState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Đăng nhập", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    //Đăng nhập bằng google
                    OutlinedButton(
                        onClick = {
                            // Ép Google quên tài khoản cũ đi trước khi mở hộp thoại
                            googleSignInClient.signOut().addOnCompleteListener {
                                // Quên xong rồi thì mới mở bảng chọn lên
                                launcher.launch(googleSignInClient.signInIntent)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(20.dp),
                        enabled = authState != AuthState.Loading
                    ) {
                        Text(
                            text = "Đăng nhập bằng Google",
                            fontSize = 16.sp,
                            color = Color(0xFF555555),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Chưa có tài khoản? Đăng ký ngay",
                        color = Color(0xFFB56A7A),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                navController.navigate("register")
                            }
                            .padding(8.dp)
                    )
                }
            }
        }
    }

    // Xử lý sự kiện bấm nút "Quên mật khẩu?" (Cập nhật lại phần này trong form đăng nhập của bạn)
    // ... modifier.clickable { showForgotDialog = true; forgotStep = 1; resetEmail = ""; otpCode = ""; newPass = ""; resetMessage = "" } ...

    if (showForgotDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = { Text(text = if (forgotStep == 1) "Khôi phục mật khẩu" else "Xác nhận OTP", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    if (forgotStep == 1) {
                        Text("Nhập Email của bạn. Chúng tôi sẽ gửi mã OTP gồm 6 chữ số.", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))
                        OutlinedTextField(
                            value = resetEmail, onValueChange = { resetEmail = it },
                            label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                        )
                    } else {
                        Text("Mã OTP đã được gửi đến $resetEmail", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))
                        OutlinedTextField(
                            value = otpCode, onValueChange = { otpCode = it },
                            label = { Text("Mã OTP 6 số") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = newPass, onValueChange = { newPass = it },
                            label = { Text("Mật khẩu mới") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                            // Thêm logic ẩn hiện và con mắt vào đây
                            visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                val image = if (newPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                    Icon(imageVector = image, contentDescription = null)
                                }
                            }
                        )
                    }
                    if (resetMessage.isNotEmpty()) {
                        Text(text = resetMessage, color = if (isSuccessMessage) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
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
                                if (success) {
                                    // Thành công thì đóng hộp thoại
                                    showForgotDialog = false
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB56A7A))
                ) {
                    Text(if (forgotStep == 1) "Gửi OTP" else "Đổi mật khẩu", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showForgotDialog = false }) { Text("Hủy", color = Color(0xFFB56A7A)) }
            }
        )
    }
}