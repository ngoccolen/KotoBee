package com.example.kotobee.ui.lessons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kotobee.R
import com.example.kotobee.ui.home.CardBorderColor
import com.example.kotobee.ui.home.ProgressPrimary
import com.example.kotobee.ui.home.TextDark
import com.example.kotobee.ui.home.TextGray
import com.example.kotobee.ui.home.ThemeBackground

private data class SkillDestination(
    val name: String,
    val subtitle: String,
    val imageRes: Int,
    val icon: ImageVector,
    val route: String?
)

@Composable
fun LearningScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    onNavigateToLesson: (String) -> Unit = { route -> navController.navigate(route) }
) {
    val skills = listOf(
        SkillDestination("Từ vựng", "Flashcard, quiz và lặp lại ngắt quãng", R.drawable.jp_vocabulary, Icons.Default.Bookmarks, "deck_list"),
        SkillDestination("Ngữ pháp", "Mẫu câu, ví dụ và bài tập theo JLPT", R.drawable.jp_grammar, Icons.Default.Translate, "grammar_dashboard"),
        SkillDestination("Đọc hiểu", "Bài đọc, từ khóa và luyện đọc theo chủ đề", R.drawable.jp_reading, Icons.Default.MenuBook, "news_list"),
        SkillDestination("Hán tự", "Nét chữ, âm đọc và luyện viết", R.drawable.jp_writing, Icons.Default.Edit, "kanji_list"),
        SkillDestination("Luyện nói", "Shadowing và hội thoại AI theo chủ đề", R.drawable.jp_speaking, Icons.Default.Mic, "speaking_hub")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ThemeBackground),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            LearningHero()
        }

        item {
            Column(modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)) {
                Text("Kỹ năng", color = TextDark, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            }
        }

        items(skills) { skill ->
            SkillHorizontalCard(
                skill = skill,
                onClick = {
                    skill.route?.let(onNavigateToLesson)
                }
            )
        }
    }
}

@Composable
private fun LearningHero() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(listOf(Color(0xFFB71C1C), ProgressPrimary, Color(0xFFE53935))))
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Học tập", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    "Cùng nhau rèn luyện các kỹ năng nhé!",
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
            Image(
                painter = painterResource(id = R.drawable.logo_7),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(96.dp)
            )
        }
    }
}

@Composable
private fun SkillHorizontalCard(
    skill: SkillDestination,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF), // Ép màu trắng tuyệt đối
            contentColor = TextDark
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Để 0.dp và dùng viền cho sạch sẽ
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 112.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = skill.imageRes),
                contentDescription = skill.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(60.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(1.dp, CardBorderColor),
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(skill.icon, contentDescription = null, tint = ProgressPrimary, modifier = Modifier.padding(6.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        skill.name,
                        color = TextDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    skill.subtitle,
                    color = TextGray,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextGray)
        }
    }
}
