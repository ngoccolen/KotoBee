package com.example.kotobee.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kotobee.R

@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = viewModel()
) {
    var step by remember { mutableStateOf(1) }
    val levels = listOf("N5", "N4", "N3", "N2", "N1")

    // Tone màu Đỏ Nhật Bản đồng bộ với Auth
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFFDFD), Color(0xFFFCE8E8))
    )
    val primaryButtonGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFE53935), Color(0xFFB71C1C))
    )
    val primaryTextColor = Color(0xFFB71C1C)
    val primaryAccentColor = Color(0xFFD32F2F)

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    // Giao diện phong cách Light Visual Novel
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        // 1. Ảnh nhân vật (Sprite)
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { -it / 3 },
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-80).dp) // Nhích logo lên một chút để chừa chỗ cho các nút phía dưới
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "KotoBee Mascot",
                modifier = Modifier.size(250.dp)
            )
        }

        // Phân vùng dưới cùng chứa hộp thoại và lựa chọn
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // 2. Các nút lựa chọn (Hiện ra ở bước 2)
            if (step == 2) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    levels.forEach { level ->
                        Button(
                            onClick = {
                                viewModel.saveInitialLevel(level) {
                                    // Chuyển về Home và xoá lịch sử điều hướng
                                    navController.navigate("home") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(54.dp)
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(20.dp),
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
                                Text(
                                    text = "Trình độ $level",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 3. Khung hội thoại (Dialogue Box)
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(1000)) + slideInVertically(tween(1000)) { it / 3 }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(2.dp, primaryAccentColor, RoundedCornerShape(16.dp))
                        .clickable { if (step == 1) step = 2 } // Bấm vào hộp thoại để chuyển bước
                        .padding(20.dp)
                ) {
                    Column {
                        // Tên nhân vật
                        Text(
                            text = "KotoBee",
                            color = primaryTextColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        // Nội dung thoại
                        Text(
                            text = if (step == 1)
                                "Chào mừng bạn đến với KotoBee! \nNhấn vào đây để tiếp tục nhé..."
                            else
                                "Trình độ tiếng Nhật hiện tại của bạn là gì? \nHãy chọn ở phía trên nhé!",
                            color = Color(0xFF333333),
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}