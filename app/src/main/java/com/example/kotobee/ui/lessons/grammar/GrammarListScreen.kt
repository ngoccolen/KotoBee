package com.example.kotobee.ui.lessons.grammar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController

@Composable
fun GrammarListScreen(
    navController: NavController,
    viewModel: GrammarViewModel,
    level: String
) {
    val lessons by viewModel.lessons.collectAsState()
    val lessonAccess by viewModel.lessonAccess.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    val rows = lessonAccess.ifEmpty {
        lessons.map { grammar ->
            GrammarLessonAccess(grammar = grammar, unlocked = true, completed = false)
        }
    }
    val completedCount = rows.count { it.completed }

    LaunchedEffect(level) {
        viewModel.loadLessonsByLevel(level)
    }

    DisposableEffect(lifecycleOwner, level) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadLessonsByLevel(level)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrammarSurface)
    ) {
        GrammarListHeader(
            level = level,
            lessonCount = lessons.size,
            completedCount = completedCount,
            onBack = { navController.popBackStack() }
        )

        if (isLoading && lessons.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GrammarCoral)
            }
        } else if (rows.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có bài học nào.", color = GrammarInk, fontWeight = FontWeight.Bold)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(rows, key = { _, row -> row.grammar.id }) { index, row ->
                    GrammarLessonRow(
                        index = index,
                        access = row,
                        onLessonClick = { navController.navigate("grammar_detail/${row.grammar.id}") }
                    )
                }
            }
        }
    }
}

@Composable
private fun GrammarListHeader(
    level: String,
    lessonCount: Int,
    completedCount: Int,
    onBack: () -> Unit
) {
    val levelColor = when (level) {
        "N5" -> GrammarMint
        "N4" -> JapaneseIndigo
        "N3" -> GrammarCoral
        "N2" -> GrammarAmber
        else -> Color(0xFF6D5BD0)
    }

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = GrammarInk)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text("Ngữ pháp $level", fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, color = GrammarInk)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = levelColor.copy(alpha = 0.12f)),
            border = BorderStroke(1.dp, levelColor.copy(alpha = 0.28f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(levelColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(level, color = Color.White, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("$completedCount/$lessonCount bài đã hoàn thành", color = GrammarInk, fontWeight = FontWeight.Bold)
                        Text("Học từng mẫu câu kèm cấu trúc, lưu ý và ví dụ.", color = GrammarMuted, fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { if (lessonCount == 0) 0f else completedCount.toFloat() / lessonCount.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(CircleShape),
                    color = levelColor,
                    trackColor = Color.White
                )
            }
        }
    }
}

@Composable
private fun GrammarLessonRow(
    index: Int,
    access: GrammarLessonAccess,
    onLessonClick: () -> Unit
) {
    val grammar = access.grammar
    val levelColor = grammarLevelColor(grammar.level)
    val enabled = access.unlocked
    val formattedIndex = String.format("%02d", index + 1)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onLessonClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (access.unlocked) GrammarLine else Color(0xFFE6E8EE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        if (access.unlocked) levelColor.copy(alpha = 0.1f) else Color(0xFFF5F5F5),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formattedIndex,
                    color = if (access.unlocked) levelColor else Color.Gray,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Bài ${index + 1}: ${grammar.title}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (access.unlocked) GrammarInk else Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (access.unlocked) grammar.meaning else "Hoàn thành bài trước để mở",
                    fontSize = 13.sp,
                    color = GrammarMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = when {
                    access.completed -> Icons.Default.CheckCircle
                    !access.unlocked -> Icons.Default.Lock
                    else -> Icons.Default.ChevronRight
                },
                contentDescription = null,
                tint = when {
                    access.completed -> GrammarMint
                    !access.unlocked -> Color.Gray
                    else -> levelColor
                }
            )
        }
    }
}

private fun grammarLevelColor(level: String): Color {
    return when (level) {
        "N5" -> GrammarMint
        "N4" -> JapaneseIndigo
        "N3" -> GrammarCoral
        "N2" -> GrammarAmber
        else -> Color(0xFF6D5BD0)
    }
}
