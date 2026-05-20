package com.example.kotobee.ui.auth.register

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

private val RedPrimary = Color(0xFFE53935)
private val RedAccent = Color(0xFFD32F2F)

@Composable
fun RegisterScreen(navController: NavController, viewModel: RegisterViewModel = viewModel()) {
    val authState by viewModel.authState.collectAsState()

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color.White, Color.White)
    )
    val primaryButtonGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFE53935), Color(0xFFB71C1C))
    )

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    // Đăng ký xong vào thẳng app; người dùng có thể chỉnh N trong hồ sơ.
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            navController.navigate("home") {
                popUpTo("register") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(5.dp))

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { -it / 3 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "bee",
                        modifier = Modifier.size(200.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(1000)) + slideInVertically(tween(1000)) { it / 3 }
            ) {
                // Card nền trắng, viền đỏ
                Card(
                    shape = RoundedCornerShape(32.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, Color(0xFFB71C1C)),
                    modifier = Modifier.fillMaxWidth().offset(y = (-20).dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tạo tài khoản mới",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        var username by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Tên đăng nhập") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = RedAccent) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            colors = registerTextFieldColors()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        var email by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = RedAccent) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            colors = registerTextFieldColors()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        var password by remember { mutableStateOf("") }
                        var passwordVisible by remember { mutableStateOf(false) }
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Mật khẩu") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = RedAccent) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                            trailingIcon = {
                                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = image, contentDescription = null)
                                }
                            },
                            colors = registerTextFieldColors()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        var checkpassword by remember { mutableStateOf("") }
                        var checkPasswordVisible by remember { mutableStateOf(false) }
                        OutlinedTextField(
                            value = checkpassword,
                            onValueChange = { checkpassword = it },
                            label = { Text("Nhập lại mật khẩu", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = RedAccent) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            visualTransformation = if (checkPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            trailingIcon = {
                                val image = if (checkPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                IconButton(onClick = { checkPasswordVisible = !checkPasswordVisible }) {
                                    Icon(imageVector = image, contentDescription = null)
                                }
                            },
                            colors = registerTextFieldColors()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        if (authState is AuthState.Error) {
                            Surface(
                                color = RedPrimary.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                Text(
                                    text = (authState as AuthState.Error).message,
                                    color = RedAccent,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }

                        // Nút Đăng ký
                        Button(
                            onClick = { viewModel.register(username, email, password, checkpassword) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
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
                                    Text("Đăng ký tài khoản", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "Đã có tài khoản? ", color = Color.Gray, fontSize = 14.sp)
                            Text(
                                text = "Đăng nhập",
                                color = RedAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.clickable {
                                    navController.navigate("login")
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun registerTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    disabledContainerColor = Color.White,
    errorContainerColor = Color.White,
    focusedBorderColor = RedAccent,
    unfocusedBorderColor = RedAccent.copy(alpha = 0.65f),
    focusedLabelColor = RedAccent,
    unfocusedLabelColor = Color.Gray,
    cursorColor = RedAccent
)
