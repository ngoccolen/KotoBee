package com.example.kotobee.ui.lessons.writing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.PathParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    state: KanjiPracticeState,
    onBackClick: () -> Unit // Thêm nút Back
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.data?.character ?: "Hán tự", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFFDFD))
            )
        },
        containerColor = Color(0xFFFFFDFD)
    ) { padding ->
        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFE53935))
                }
            }
            state.error != null -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(text = state.error, color = MaterialTheme.colorScheme.error)
                }
            }
            state.data != null -> {
                val kanjiDto = state.data
                val userPaths = remember { mutableStateListOf<Path>() }
                var currentPath by remember { mutableStateOf<Path?>(null) }
                var isHintVisible by remember { mutableStateOf(false) }

                val parsedStrokePaths = remember(kanjiDto.svgPaths) {
                    kanjiDto.svgPaths.mapNotNull { svgString ->
                        try {
                            PathParser.createPathFromPathData(svgString).asComposePath()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }
                }

                val parsedHintPath = remember(parsedStrokePaths) {
                    Path().apply {
                        parsedStrokePaths.forEach { path -> addPath(path) }
                    }
                }

                // Chi thớt: Cả cục này có thể cuộn được nếu màn hình nhỏ
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ==========================================
                    // PHẦN 1: BẢNG VẼ Ở TRÊN (Top Section)
                    // ==========================================
                    KanjiDrawingBoard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 16.dp),
                        paths = userPaths,
                        currentPath = currentPath,
                        hintPath = parsedHintPath,
                        showHint = isHintVisible,
                        onDragStart = { offset -> currentPath = Path().apply { moveTo(offset.x, offset.y) } },
                        onDrag = { position ->
                            currentPath?.lineTo(position.x, position.y)
                            val temp = currentPath
                            currentPath = null
                            currentPath = temp
                        },
                        onDragEnd = {
                            currentPath?.let { userPaths.add(it) }
                            currentPath = null
                        }
                    )

                    // Nút thao tác bảng vẽ
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        Button(
                            onClick = { userPaths.clear() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Xóa")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Xóa nét")
                        }

                        Button(
                            onClick = { isHintVisible = !isHintVisible },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26A69A))
                        ) {
                            Icon(
                                imageVector = if (isHintVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Gợi ý"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isHintVisible) "Ẩn gợi ý" else "Xem gợi ý")
                        }
                    }

                    Divider(color = Color(0xFFE53935).copy(alpha = 0.35f), thickness = 1.dp, modifier = Modifier.padding(horizontal = 24.dp))

                    // ==========================================
                    // PHẦN 2: THÔNG TIN CHI TIẾT (Bottom Section)
                    // ==========================================
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text("Phát âm", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("🔹 Kunyomi: ", color = Color.Gray, fontSize = 14.sp)
                        Text(kanjiDto.kunyomi.ifEmpty { "Không có" }, fontSize = 18.sp, color = Color(0xFFE53935), modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))

                        Text("🔹 Onyomi: ", color = Color.Gray, fontSize = 14.sp)
                        Text(kanjiDto.onyomi.ifEmpty { "Không có" }, fontSize = 18.sp, color = Color(0xFFE53935), modifier = Modifier.padding(start = 16.dp, bottom = 16.dp))

                        Text("Ý nghĩa", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                        Text(kanjiDto.meaning.uppercase(), fontSize = 16.sp, color = Color(0xFF333333), modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Số nét", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                                Text("${kanjiDto.strokeCount} nét", fontSize = 16.sp, color = Color.Gray)
                            }
                            Column {
                                Text("Bộ thủ", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                                Text(kanjiDto.radical, fontSize = 16.sp, color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // KHUNG DEMO VIẾT TỪ
                        Text("Mô phỏng nét chữ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(Color.White, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedKanjiStrokeOrder(
                                strokePaths = parsedStrokePaths,
                                modifier = Modifier.size(150.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}
