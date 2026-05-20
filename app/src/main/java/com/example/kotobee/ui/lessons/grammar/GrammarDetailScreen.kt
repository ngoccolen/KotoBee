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
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kotobee.data.model.Example
import com.example.kotobee.data.model.Grammar

@Composable
fun GrammarDetailScreen(
    navController: NavController,
    viewModel: GrammarViewModel
) {
    val grammar by viewModel.currentGrammar.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val lessonAccess by viewModel.currentLessonAccess.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    when {
        isLoading && grammar == null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GrammarSurface),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GrammarCoral)
            }
        }

        grammar == null -> GrammarNotFound(
            message = errorMessage ?: "Không tìm thấy bài học.",
            onBack = { navController.popBackStack() }
        )

        lessonAccess?.unlocked == false -> GrammarLockedDetail(
            lessonAccess = lessonAccess!!,
            onBack = { navController.popBackStack() }
        )

        else -> GrammarDetailContent(
            grammar = grammar!!,
            hasQuiz = questions.isNotEmpty(),
            onBack = { navController.popBackStack() },
            onPractice = { navController.navigate("grammar_practice/${grammar!!.id}") }
        )
    }
}

@Composable
private fun GrammarDetailContent(
    grammar: Grammar,
    hasQuiz: Boolean,
    onBack: () -> Unit,
    onPractice: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Scaffold(
        containerColor = GrammarSurface,
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = Color.White) {
                Button(
                    onClick = onPractice,
                    enabled = hasQuiz,
                    colors = ButtonDefaults.buttonColors(containerColor = GrammarCoral),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(54.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (hasQuiz) "Luyện tập mẫu này" else "Bài này chưa có quiz",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = GrammarInk)
                }
                Text("KotoBee Grammar", color = GrammarMuted, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                IconButton(onClick = { }) {
                    Icon(Icons.Default.BookmarkBorder, contentDescription = "Lưu bài", tint = GrammarCoral)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(JapaneseIndigo.copy(alpha = 0.12f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text("JLPT ${grammar.level}", color = JapaneseIndigo, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                grammar.tags.take(2).forEach { tag ->
                    GrammarTag(text = tag)
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(grammar.title, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = GrammarInk, lineHeight = 40.sp)
            if (grammar.romaji.isNotBlank()) {
                Text(grammar.romaji, color = GrammarMuted, fontSize = 15.sp, modifier = Modifier.padding(top = 4.dp))
            }
            Text(grammar.meaning, color = GrammarCoral, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
            if (grammar.summary.isNotBlank()) {
                Text(grammar.summary, color = GrammarMuted, fontSize = 15.sp, lineHeight = 22.sp, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(22.dp))
            GrammarInfoCard(title = "Cấu trúc", icon = Icons.Default.Edit, iconColor = GrammarMint) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F7FA), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = grammar.formation,
                        color = GrammarInk,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            GrammarInfoCard(title = "Lưu ý dùng", icon = Icons.Default.Info, iconColor = GrammarAmber) {
                Text(grammar.usageNote, color = GrammarMuted, fontSize = 15.sp, lineHeight = 22.sp)
            }

            Spacer(modifier = Modifier.height(22.dp))
            Text("Ví dụ dễ nhớ", color = GrammarInk, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(12.dp))
            grammar.examples.forEach { example ->
                GrammarExampleCard(example)
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (grammar.sourceName.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, GrammarLine)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Nguồn tham khảo", fontWeight = FontWeight.Bold, color = GrammarInk)
                        Text(grammar.sourceName, color = GrammarMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                        if (grammar.sourceUrl.isNotBlank()) {
                            TextButton(onClick = { uriHandler.openUri(grammar.sourceUrl) }) {
                                Text("Mở trang nguồn", color = JapaneseIndigo, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = JapaneseIndigo, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            if (!hasQuiz) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GrammarAmber.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, GrammarAmber.copy(alpha = 0.3f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = GrammarAmber)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Admin cần thêm quiz để bài này được tính hoàn thành và mở khóa bài sau.",
                            color = GrammarInk,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun GrammarLockedDetail(
    lessonAccess: GrammarLessonAccess,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrammarSurface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(GrammarLine.copy(alpha = 0.28f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = GrammarCoral, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Bài học đang khóa", color = GrammarInk, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        Text(
            "Hoàn thành \"${lessonAccess.previousTitle ?: "bài trước"}\" để mở ${lessonAccess.grammar.title}.",
            color = GrammarMuted,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
        Spacer(modifier = Modifier.height(18.dp))
        Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = GrammarCoral)) {
            Text("Quay lại danh sách")
        }
    }
}

@Composable
private fun GrammarInfoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, GrammarLine),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(iconColor.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(19.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(title, color = GrammarInk, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun GrammarExampleCard(example: Example) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, GrammarLine)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Translate, contentDescription = null, tint = GrammarCoral, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(example.jp, color = GrammarInk, fontSize = 17.sp, fontWeight = FontWeight.Bold, lineHeight = 25.sp)
                    if (example.romaji.isNotBlank()) {
                        Text(example.romaji, color = GrammarMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
                Icon(Icons.Default.VolumeUp, contentDescription = "Nghe ví dụ", tint = GrammarMuted, modifier = Modifier.size(21.dp))
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = GrammarLine)
            Text(example.vi, color = GrammarMint, fontSize = 15.sp, fontWeight = FontWeight.Medium, lineHeight = 21.sp)
        }
    }
}

@Composable
private fun GrammarNotFound(
    message: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrammarSurface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message, color = GrammarInk, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = GrammarCoral)) {
            Text("Quay lại")
        }
    }
}

@Composable
private fun GrammarTag(text: String, modifier: Modifier = Modifier, color: Color = GrammarMint) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(50))
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}
