package com.example.kotobee.ui.practice// Import các thư viện cần thiết cho Jetpack Compose
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Định nghĩa tông màu chính cho thiết kế
val MainSkyBlue = Color(0xFFC0E8F8)
val PracticeStartGreen = Color(0xFF7DD4AF)
val PracticeOrange = Color(0xFFFFB74D)
val PracticePurple = Color(0xFFC5B4E3)

@Composable
fun PracticeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // --- Phần Header & Tiến độ (Header & Progress) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MainSkyBlue, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Luyện Tập Hàng Ngày",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProgressCircle(complete = 2, total = 4)
                StreakCounter(days = 7)
            }
        }

        // --- Phần Lưới bài tập (Grid of Practice Cards) ---
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .weight(1f) // Chiếm phần diện tích còn lại
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Danh sách các bài tập (Dữ liệu mẫu)
            item { PracticeCard("Luyện Nghe", "Chủ đề: Giao tiếp cơ bản", Icons.Default.Headphones, Color(0xFFC6E8F6), "Bắt đầu") }
            item { PracticeCard("Ngữ Pháp", "Bài học: Chia động từ", Icons.Default.Book, Color(0xFFD3E7DE), "Bắt đầu") }
            item { PracticeCard("Luyện Đọc", "Văn bản: Câu chuyện ngắn N5", Icons.Default.Book, Color(0xFFFDE1C7), "Bắt đầu") }
            item { PracticeCard("Từ Vựng", "Flashcards: 10 từ mới", Icons.Default.Translate, Color(0xFFDCD4F1), "Bắt đầu") }
        }

        // --- Phần Gợi ý (Suggestions Section) ---
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Gợi ý cho bạn", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                SmallSuggestionCard("Review\ntừ cũ", Color(0xFF42A5F5))
                Spacer(modifier = Modifier.width(12.dp))
                SmallSuggestionCard("Thử thách\nphát âm", Color(0xFFFF8A65))
            }
        }
    }
}

// Composable cho Vòng tròn tiến độ (2/4)
@Composable
fun ProgressCircle(complete: Int, total: Int) {
    Surface(
        modifier = Modifier.size(70.dp),
        shape = CircleShape,
        color = Color.White,
        border = BorderStroke(4.dp, MainSkyBlue.copy(alpha = 0.4f))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(text = "$complete/$total", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "Bài tập", fontSize = 11.sp, color = Color.Gray)
        }
    }
}

// Composable cho Số ngày Streak (7 Ngày)
@Composable
fun StreakCounter(days: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$days Ngày", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(text = "streak", fontSize = 11.sp, color = Color.Gray)
    }
}

// Composable cho thẻ Bài tập chính (Luyện nghe, Ngữ pháp, ...)
@Composable
fun PracticeCard(title: String, subtitle: String, icon: ImageVector, iconColor: Color, buttonText: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7)), // Màu nền nhẹ của thẻ
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(modifier = Modifier.size(56.dp), shape = RoundedCornerShape(12.dp), color = iconColor) {
                Icon(imageVector = icon, contentDescription = title, tint = Color.Black, modifier = Modifier.padding(12.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center, minLines = 2)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { /* Bắt đầu bài tập */ },
                colors = ButtonDefaults.buttonColors(containerColor = PracticeStartGreen), // Màu nút Bắt đầu
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(text = buttonText, fontSize = 14.sp, color = Color.White)
            }
        }
    }
}

// Composable cho thẻ Gợi ý nhỏ (Review, Phát âm)
@Composable
fun RowScope.SmallSuggestionCard(text: String, accentColor: Color) {
    Card(
        modifier = Modifier.weight(1f).height(60.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Thanh màu nhấn nhỏ ở bên trái
            Box(modifier = Modifier.width(6.dp).height(30.dp).background(accentColor, shape = CircleShape))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF333333), lineHeight = 18.sp)
        }
    }
}