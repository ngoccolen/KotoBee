package com.example.kotobee.ui.lessons.shadowing

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.kotobee.data.model.ShadowingLesson
import com.example.kotobee.ui.home.CardBorderColor
import com.example.kotobee.ui.home.ProgressPrimary
import com.example.kotobee.ui.home.TextDark
import com.example.kotobee.ui.home.TextGray
import com.example.kotobee.ui.home.ThemeBackground
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShadowingPracticeScreen(
    lessonId: String,
    viewModel: ShadowingViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val lessons by viewModel.lessons.collectAsState()
    val isLoadingLessons by viewModel.lessonsLoading.collectAsState()
    val lessonsError by viewModel.lessonsError.collectAsState()
    val lesson = lessons.find { it.id == lessonId }
    val uiState by viewModel.uiState.collectAsState()

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasAudioPermission = isGranted }
    )

    LaunchedEffect(Unit) {
        viewModel.loadLessons()
    }

    if (lesson == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when {
                isLoadingLessons -> CircularProgressIndicator(color = ProgressPrimary)
                lessonsError != null -> Text(lessonsError.orEmpty(), color = Color.Red, modifier = Modifier.padding(24.dp))
                else -> Text("Không tìm thấy bài học shadowing", color = TextGray)
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Luyện phát âm", fontWeight = FontWeight.Bold, color = TextDark) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ThemeBackground)
            )
        },
        containerColor = ThemeBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hiển thị câu tiếng Nhật
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, CardBorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                var showFurigana by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxWidth()) {
                    // Nút bật/tắt furigana
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (showFurigana) ProgressPrimary else Color.LightGray.copy(alpha = 0.3f))
                            .clickable { showFurigana = !showFurigana }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "あ",
                            color = if (showFurigana) Color.White else TextGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (showFurigana) {
                            Text(
                                text = lesson.furigana,
                                color = TextGray,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        } else {
                            // Giữ khoảng trống để UI không bị giật khi bật tắt
                            Spacer(modifier = Modifier.height(28.dp))
                        }
                        Text(
                            text = lesson.japanese,
                            color = TextDark,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Nút Ghi Âm
            if (!hasAudioPermission) {
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    colors = ButtonDefaults.buttonColors(containerColor = ProgressPrimary)
                ) {
                    Text("Cấp quyền Ghi âm")
                }
            } else {
                when (uiState) {
                    is ShadowingUiState.Idle, is ShadowingUiState.Result, is ShadowingUiState.Error -> {
                        Button(
                            onClick = {
                                val file = File(context.cacheDir, "shadowing_audio_${System.currentTimeMillis()}.m4a")
                                viewModel.startRecording(file)
                            },
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = ProgressPrimary),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Bắt đầu", modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Bấm để ghi âm", color = TextGray)
                    }
                    is ShadowingUiState.Recording -> {
                        Button(
                            onClick = { viewModel.stopRecordingAndAnalyze(lesson) },
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Dừng", modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Đang ghi âm...", color = ProgressPrimary, fontWeight = FontWeight.Bold)
                    }
                    is ShadowingUiState.Analyzing -> {
                        CircularProgressIndicator(color = ProgressPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Đang chấm điểm...", color = TextGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Hiển thị Lỗi hoặc Kết quả
            if (uiState is ShadowingUiState.Error) {
                val error = (uiState as ShadowingUiState.Error).message
                Text(text = error, color = Color.Red, modifier = Modifier.padding(16.dp))
            } else if (uiState is ShadowingUiState.Result) {
                val result = (uiState as ShadowingUiState.Result).response
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, CardBorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Điểm số:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "${result.score}/100",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                color = if (result.score >= 80) Color(0xFF2E7D32) else ProgressPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = result.summary, color = TextDark)
                        
                        if (result.grammarFeedback != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Ngữ pháp:", fontWeight = FontWeight.Bold, color = TextDark)
                            Text(text = result.grammarFeedback.shortCommentVi, color = TextGray)
                            
                            if (result.grammarFeedback.issues.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                result.grammarFeedback.issues.forEach { issue ->
                                    Box(modifier = Modifier.padding(vertical = 4.dp).background(Color.White, RoundedCornerShape(8.dp)).padding(8.dp)) {
                                        Column {
                                            Text("Sửa: ${issue.wrong} -> ${issue.correct}", color = ProgressPrimary, fontWeight = FontWeight.Bold)
                                            Text(issue.reasonVi, color = TextGray, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
