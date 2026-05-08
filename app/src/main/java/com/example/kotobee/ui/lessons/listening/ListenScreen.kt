package com.example.kotobee.ui.lessons.listening

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kotobee.data.model.WordDetail
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListeningPracticeScreen(
    viewModel: ListeningViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var selectedWord by remember { mutableStateOf<WordDetail?>(null) }
    val tabs = listOf("Hội thoại", "Từ vựng & Mẫu câu")

    Scaffold(
        containerColor = Color(0xFFFAF7F2) // Màu nền kem đặc trưng
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // 1. Header bài học
            LessonHeader(
                title = uiState.lessonTitle,
                onBackClick = onBackClick
            )

            // ==========================================
            // 2. TRÌNH PHÁT VIDEO YOUTUBE (TỰ HIỆN NẾU CÓ DATA)
            // ==========================================
            if (!uiState.youtubeId.isNullOrEmpty()) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    YouTubeVideoPlayer(youtubeVideoId = uiState.youtubeId)
                }
            }

            // 3. Trình điều khiển Audio
            AudioPlayerControls(
                isPlaying = uiState.isPlaying,
                currentPos = uiState.currentPosition,
                totalDur = uiState.totalDuration,
                onPlayPause = { viewModel.togglePlayPause() },
                onForward = { viewModel.seekForward() },
                onBackward = { viewModel.seekBackward() },
                showTranslation = uiState.showTranslation,
                onToggleTranslation = { viewModel.toggleTranslation() }
            )

            // 4. Hệ thống Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color(0xFF8B5A2B),
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                    )
                }
            }

            // 5. Nội dung thay đổi theo Tab
            if (selectedTab == 0) {
                TranscriptSection(
                    transcript = uiState.transcript,
                    showTranslation = uiState.showTranslation,
                    onWordClick = { word -> selectedWord = word }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Tính năng đang phát triển", color = Color.Gray)
                }
            }
        }
    }

    // 6. Bottom Sheet hiển thị nghĩa khi click vào từ
    if (selectedWord != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedWord = null },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(selectedWord!!.reading, fontSize = 16.sp, color = Color.Gray)
                Text(selectedWord!!.word, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5A2B))
                Spacer(modifier = Modifier.height(16.dp))
                Text(selectedWord!!.meaning, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { /* Chức năng lưu sổ tay */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5A2B))
                ) {
                    Text("Thêm vào sổ tay")
                }
            }
        }
    }
}

// --- COMPONENT PHÁT VIDEO YOUTUBE ---
@Composable
fun YouTubeVideoPlayer(youtubeVideoId: String) {
    // Quản lý vòng đời (Lifecycle) để video tự dừng khi thoát màn hình
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f) // Tỷ lệ chuẩn của video YouTube 16:9
            .clip(RoundedCornerShape(12.dp)), // Bo góc đẹp mắt
        factory = { context ->
            YouTubePlayerView(context).apply {
                lifecycleOwner.lifecycle.addObserver(this)

                addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        // cueVideo tải sẵn Thumbnail. Nếu muốn tự động phát ngay, dùng loadVideo()
                        youTubePlayer.cueVideo(youtubeVideoId, 0f)
                    }
                })
            }
        }
    )
}

@Composable
fun LessonHeader(title: String, onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFD7CCC8))
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Quay lại",
                tint = Color(0xFF3E2723)
            )
        }

        Text(
            text = title,
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 48.dp, vertical = 16.dp),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF3E2723)
        )
    }
}

@Composable
fun AudioPlayerControls(
    isPlaying: Boolean,
    currentPos: Long,
    totalDur: Long,
    onPlayPause: () -> Unit,
    onForward: () -> Unit,
    onBackward: () -> Unit,
    showTranslation: Boolean,
    onToggleTranslation: () -> Unit
) {
    val progress = if (totalDur > 0) currentPos.toFloat() / totalDur else 0f

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50)),
                color = Color(0xFFB8860B),
                trackColor = Color(0xFFEEEEEE)
            )

            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackward) { Text("⏪", fontSize = 20.sp) }

                Button(
                    onClick = onPlayPause,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B4226)),
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(if (isPlaying) "⏸" else "▶", fontSize = 24.sp)
                }

                IconButton(onClick = onForward) { Text("⏩", fontSize = 20.sp) }
            }

            TextButton(
                onClick = onToggleTranslation,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(if (showTranslation) "Ẩn dịch tiếng Việt" else "Hiện dịch tiếng Việt", color = Color(0xFF6B4226), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TranscriptSection(
    transcript: List<com.example.kotobee.data.model.TranscriptLine>,
    showTranslation: Boolean,
    onWordClick: (WordDetail) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(transcript) { dialogue ->
            val isUser = dialogue.speaker == "Tam" // Highlight nhân vật chính
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isUser) Color(0xFFF0EBE1) else Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = dialogue.speaker, color = Color(0xFF8B5A2B), fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    val annotatedString = buildAnnotatedString {
                        val text = dialogue.jpText
                        append(text)
                        dialogue.vocab.forEach { vocab ->
                            val start = text.indexOf(vocab.word)
                            if (start >= 0) {
                                addStyle(style = SpanStyle(color = Color(0xFFD84315), fontWeight = FontWeight.ExtraBold), start = start, end = start + vocab.word.length)
                                addStringAnnotation(tag = "VOCAB", annotation = vocab.word, start = start, end = start + vocab.word.length)
                            }
                        }
                    }

                    ClickableText(
                        text = annotatedString,
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.Black),
                        onClick = { offset ->
                            annotatedString.getStringAnnotations("VOCAB", offset, offset).firstOrNull()?.let { annotation ->
                                val word = dialogue.vocab.find { it.word == annotation.item }
                                if (word != null) onWordClick(word)
                            }
                        }
                    )

                    if (showTranslation) {
                        Text(text = dialogue.viText, fontSize = 15.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }
}