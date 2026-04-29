package com.example.kotobee.ui.lessons.grammar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

val JapaneseIndigo = Color(0xFF3F51B5)
val DailyPracticeRed = Color(0xFFC2185B)

@Composable
fun GrammarDashboardScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Chào buổi sáng,", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Quân!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text("Bạn đã hoàn thành 65% mục tiêu tuần này.", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))

        Spacer(modifier = Modifier.height(24.dp))

        // Card Tiến độ
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("TIẾN ĐỘ NGỮ PHÁP", color = JapaneseIndigo, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("42", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Text(" / 120 bài học", color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp, start = 4.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { 42f / 120f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF4CAF50),
                    trackColor = Color(0xFFE8F5E9)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card Luyện tập hàng ngày
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DailyPracticeRed),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Timer, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Luyện tập hàng ngày", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Dành 15 phút để duy trì trí nhớ dài hạn.", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /* Mở Quiz trộn */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Bắt đầu ngay", color = DailyPracticeRed, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Các cấp độ ngữ pháp", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // Danh sách Level
        LevelCard(level = "N5", title = "Elementary", desc = "Cơ bản về cấu trúc câu, trợ từ...", progress = 0.85f, isLocked = false, onClick = { navController.navigate("grammar_list/N5") })
        Spacer(modifier = Modifier.height(16.dp))
        LevelCard(level = "N4", title = "Intermediate", desc = "Liên từ phức tạp, thể bị động...", progress = 0.12f, isLocked = false, onClick = { navController.navigate("grammar_list/N4") })
        Spacer(modifier = Modifier.height(16.dp))
        LevelCard(level = "N3", title = "Advanced", desc = "Ngữ pháp trung cấp, sắc thái tinh tế...", progress = 0f, isLocked = true, onClick = { })
    }
}

@Composable
fun LevelCard(level: String, title: String, desc: String, progress: Float, isLocked: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = !isLocked) { onClick() },
        colors = CardDefaults.cardColors(containerColor = if (isLocked) Color(0xFFF3F4F6) else Color.White),
        shape = RoundedCornerShape(20.dp),
        border = if (!isLocked) BorderStroke(1.dp, JapaneseIndigo) else null
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(modifier = Modifier.size(40.dp).background(Color(0xFFE8EAF6), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Text(level, color = JapaneseIndigo, fontWeight = FontWeight.Bold)
                }
                if (isLocked) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Locked", tint = Color.Gray) // Thay bằng icon Lock
                } else {
                    Text("HOÀN THÀNH ${(progress * 100).toInt()}%", color = Color(0xFF4CAF50), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("$level $title", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (isLocked) Color.Gray else Color.Black)
            Text(desc, color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))

            if (!isLocked) {
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = JapaneseIndigo),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (progress > 0) "Tiếp tục học" else "Bắt đầu học")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}