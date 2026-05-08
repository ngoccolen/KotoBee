package com.example.kotobee.ui.lessons

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kotobee.R
import com.example.kotobee.ui.home.HomeViewModel
import com.example.kotobee.ui.home.TextDark
import com.example.kotobee.ui.home.TextGray

// Tái sử dụng lại mã màu từ HomeScreen để đảm bảo tính đồng bộ
val ThemeBackground = Color(0xFFFFFDFD)
val CardBorderColor = Color(0xFFFFCDD2)
val ProgressPrimary = Color(0xFFE53935)
val ProgressTrack = Color(0xFFFFEBEE)

@Composable
fun LearningScreen(
    navController: NavController,
    viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()

    // Danh sách các kỹ năng cốt lõi (Có thể lấy từ skills_progress của UserProfile)
    val skills = listOf(
        Pair("Từ vựng", R.drawable.jp_vocabulary),
        Pair("Ngữ pháp", R.drawable.jp_grammar),
        Pair("Nghe hiểu", R.drawable.jp_listening), // Đảm bảo bạn có các ảnh này trong res/drawable
        Pair("Đọc hiểu", R.drawable.jp_reading),
        Pair("Hán tự", R.drawable.jp_writing),
        Pair("Luyện thi", R.drawable.jp_grammar) // Icon tạm cho mục Luyện thi
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .background(ThemeBackground)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp)
    ) {
        // Hero Section: Nằm ở trên cùng, chiếm toàn bộ 2 cột
        item(span = { GridItemSpan(2) }) {
            Column {
                Text(
                    text = "Lộ trình rèn luyện",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(20.dp))
                HeroResumeCard()
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = "Danh mục kỹ năng",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Lưới các thẻ kỹ năng
        items(skills) { skill ->
            val progress = userProfile.skills_progress[skill.first] ?: 0f
            SkillGridItem(
                skillName = skill.first,
                imageRes = skill.second,
                progress = progress,
                onClick = {
                    // Điều hướng tương ứng với kỹ năng
                    when (skill.first) {
                        "Từ vựng" -> navController.navigate("deck_list") // Gọi vào màn hình đã có sẵn của bạn
                        // Thêm các route khác ở đây: "Ngữ pháp" -> navController.navigate("grammar_list")...
                        "Đọc hiểu" -> navController.navigate("news_list")
                        "Nghe hiểu" -> navController.navigate("listening_list") // Đúng tên đã khai báo trong NavGraph
                        "Hán tự" -> navController.navigate("kanji_list")

                    }
                }
            )
        }
    }
}

@Composable
fun HeroResumeCard() {
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFF8A80), Color(0xFFE53935))
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(gradientBrush)
            .clickable { /* TODO: Chuyển thẳng vào bài học cuối cùng */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Đang học dở", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Từ vựng N5 - Bài 4", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                // Nút học tiếp giả lập phong cách Retro/RPG
                Box(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tiếp tục ngay", color = ProgressPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Image(
                painter = painterResource(id = R.drawable.jp_vocabulary), // Thay bằng icon nhân vật nếu có
                contentDescription = "Resume",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(80.dp)
            )
        }
    }
}

@Composable
fun SkillGridItem(skillName: String, imageRes: Int, progress: Float, onClick: () -> Unit) {
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(1000))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.9f) // Tỷ lệ khung hình cho card vuông vắn
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = skillName,
                modifier = Modifier.size(56.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = skillName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Thanh tiến trình nhỏ gọn bên dưới mỗi thẻ
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(6.dp)
                    .clip(CircleShape),
                color = ProgressPrimary,
                trackColor = ProgressTrack,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 11.sp,
                color = TextGray,
                fontWeight = FontWeight.Bold
            )
        }
    }
}