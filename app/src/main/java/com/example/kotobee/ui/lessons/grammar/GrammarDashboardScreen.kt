package com.example.kotobee.ui.lessons.grammar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

val JapaneseIndigo = Color(0xFF3949AB)
val GrammarCoral = Color(0xFFE95454)
val GrammarMint = Color(0xFF12A88A)
val GrammarAmber = Color(0xFFF4A51C)
val GrammarInk = Color(0xFF25324B)
val GrammarMuted = Color(0xFF687386)
val GrammarSurface = Color(0xFFFFFFFF)
val GrammarLine = Color(0xFFE53935)

@Composable
fun GrammarDashboardScreen(
    navController: NavController,
    viewModel: GrammarViewModel = viewModel()
) {
    val allLessons by viewModel.allLessons.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val lessons = allLessons
    val levels = viewModel.buildLevelProgress()
    val completed = levels.sumOf { it.completed }

    LaunchedEffect(Unit) {
        viewModel.loadOverview()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrammarSurface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = GrammarInk)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("Ngữ pháp JLPT", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = GrammarInk)
                Text("Học theo cấp độ N5 đến N1", fontSize = 14.sp, color = GrammarMuted)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        GrammarHeroCard(
            totalLessons = lessons.size,
            completed = completed,
            onStartClick = {
                val firstLesson = lessons.firstOrNull { it.level == "N5" } ?: lessons.firstOrNull()
                firstLesson?.let { navController.navigate("grammar_detail/${it.id}") }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            GrammarStatCard(
                title = "Cấp độ",
                value = "5",
                iconColor = JapaneseIndigo,
                modifier = Modifier.weight(1f)
            )
            GrammarStatCard(
                title = "Bài học",
                value = lessons.size.toString(),
                iconColor = GrammarMint,
                modifier = Modifier.weight(1f)
            )
            GrammarStatCard(
                title = "Đã học",
                value = completed.toString(),
                iconColor = GrammarAmber,
                modifier = Modifier.weight(1f)
            )
        }

        if (isLoading && allLessons.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GrammarCoral)
            }
        }

        Spacer(modifier = Modifier.height(26.dp))
        Text("Chọn cấp độ", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = GrammarInk)
        Text("Mỗi cấp độ có giải thích, cấu trúc, ví dụ và luyện tập nhanh.", fontSize = 14.sp, color = GrammarMuted)
        Spacer(modifier = Modifier.height(14.dp))

        levels.forEach { level ->
            GrammarLevelCard(
                item = level,
                onClick = { navController.navigate("grammar_list/${level.level}") }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun GrammarHeroCard(
    totalLessons: Int,
    completed: Int,
    onStartClick: () -> Unit
) {
    val progress = if (totalLessons == 0) 0f else completed.toFloat() / totalLessons.toFloat()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFB71C1C), Color(0xFFD32F2F), Color(0xFFE53935))
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Lộ trình ngữ pháp", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Từ mẫu câu nền tảng đến diễn đạt N1", color = Color.White.copy(alpha = 0.86f), fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.32f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("$completed/$totalLessons bài đã hoàn thành", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onStartClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Bắt đầu từ N5", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFFD32F2F))
                }
            }
        }
    }
}

@Composable
private fun GrammarStatCard(
    title: String,
    value: String,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, GrammarLine)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.TrackChanges, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = GrammarInk)
            Text(title, fontSize = 11.sp, color = GrammarMuted)
        }
    }
}

@Composable
private fun GrammarLevelCard(
    item: GrammarLevelProgress,
    onClick: () -> Unit
) {
    val levelColor = when (item.level) {
        "N5" -> GrammarMint
        "N4" -> JapaneseIndigo
        "N3" -> GrammarCoral
        "N2" -> GrammarAmber
        else -> Color(0xFF6D5BD0)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, GrammarLine),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(levelColor.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(item.level, color = levelColor, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("${item.level} · ${item.title}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = GrammarInk)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(item.description, fontSize = 13.sp, color = GrammarMuted, lineHeight = 18.sp)
                }
                Icon(
                    imageVector = Icons.Default.AutoStories,
                    contentDescription = null,
                    tint = levelColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { item.progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(CircleShape),
                    color = levelColor,
                    trackColor = levelColor.copy(alpha = 0.12f)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text("${item.completed}/${item.lessons}", color = GrammarMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = levelColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Text("Xem bài học", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun GrammarSourceCard() {
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, GrammarLine)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GrammarAmber)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nguồn học liệu", fontWeight = FontWeight.ExtraBold, color = GrammarInk)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Bộ mẫu trong app được biên soạn lại ngắn gọn để học nhanh. Khi cần mở rộng dữ liệu, có thể import danh mục từ JLPT Sensei hoặc bộ nội dung Creative Commons của Hanabira.",
                color = GrammarMuted,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { uriHandler.openUri("https://jlptsensei.com") }) {
                    Text("JLPT Sensei", color = GrammarCoral, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.OpenInNew, contentDescription = null, tint = GrammarCoral, modifier = Modifier.size(16.dp))
                }
                TextButton(onClick = { uriHandler.openUri("https://hanabira.org") }) {
                    Text("Hanabira", color = JapaneseIndigo, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.OpenInNew, contentDescription = null, tint = JapaneseIndigo, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
