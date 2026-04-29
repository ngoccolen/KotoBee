package com.example.kotobee.ui.lessons.reading

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kotobee.data.model.ReadingLesson
import com.example.kotobee.data.model.VocabDetail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingPracticeScreen(viewModel: ReadingViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedVocab by viewModel.selectedVocab.collectAsState()
    val saveStatus by viewModel.saveStatus.collectAsState()

    val context = LocalContext.current
    var showTranslation by remember { mutableStateOf(false) }
    var isAudioPlaying by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // State lưu đoạn text người dùng đang bôi đen
    var selectedTextByCursor by remember { mutableStateOf("") }

    val bgColor = Color(0xFFF8F9FA)
    val surfaceColor = Color(0xFFFFFFFF)
    val textColor = Color(0xFF2C3E50)
    val primaryBlue = Color(0xFF1976D2)
    val vocabOrange = Color(0xFFE65100)

    // Khởi tạo TTS và Load bài đọc
    LaunchedEffect(Unit) {
        viewModel.initTTS(context)
        viewModel.loadLesson("lesson_n3_001", highlightColor = vocabOrange, grammarColor = primaryBlue)
    }

    // Hiển thị Toast khi lưu từ vựng
    LaunchedEffect(saveStatus) {
        saveStatus?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearSaveStatus()
        }
    }

    Scaffold(
        containerColor = bgColor,
        topBar = { TopNavigationBar(progress = 0.65f) },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                // NÚT DỊCH AI NỔI: Chỉ hiện khi có chữ được bôi đen
                AnimatedVisibility(visible = selectedTextByCursor.isNotEmpty()) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            viewModel.translateSelectedText(selectedTextByCursor)
                            selectedTextByCursor = "" // Reset sau khi bấm dịch
                        },
                        icon = { Icon(Icons.Default.Translate, contentDescription = "Dịch AI") },
                        text = {
                            val shortText = if (selectedTextByCursor.length > 10) "${selectedTextByCursor.take(10)}..." else selectedTextByCursor
                            Text("Dịch: $shortText")
                        },
                        containerColor = primaryBlue,
                        contentColor = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                FloatingAudioPlayer(
                    isPlaying = isAudioPlaying,
                    onClick = { isAudioPlaying = !isAudioPlaying }
                )
            }
        }
    ) { paddingValues ->
        when (uiState) {
            is ReadingUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryBlue)
                }
            }
            is ReadingUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text(text = (uiState as ReadingUiState.Error).message, color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
            is ReadingUiState.Success -> {
                val data = uiState as ReadingUiState.Success
                val lesson = data.lesson

                // Trạng thái quản lý văn bản cho tính năng Bôi Đen
                var textValue by remember(data.annotatedContent) {
                    mutableStateOf(TextFieldValue(annotatedString = data.annotatedContent))
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SuggestionChip(
                            onClick = { },
                            label = { Text("${lesson.level} READING", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = primaryBlue.copy(alpha = 0.1f)),
                            border = null
                        )

                        IconButton(onClick = { showTranslation = !showTranslation }) {
                            Icon(
                                imageVector = Icons.Outlined.Translate,
                                contentDescription = "Dịch",
                                tint = if (showTranslation) primaryBlue else Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = lesson.title.ifEmpty { "Đang tải tiêu đề..." },
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor,
                        lineHeight = 1.3.em
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // BỘ CHỨA VĂN BẢN THÔNG MINH (Xử lý Bôi đen & Click)
                    Surface(
                        color = surfaceColor,
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            BasicTextField(
                                value = textValue,
                                onValueChange = { newValue ->
                                    val prevSelection = textValue.selection
                                    textValue = newValue

                                    if (!newValue.selection.collapsed) {
                                        // 1. Khi người dùng đang bôi đen
                                        val start = newValue.selection.min
                                        val end = newValue.selection.max
                                        if (start != end) {
                                            selectedTextByCursor = newValue.annotatedString.text.substring(start, end)
                                        }
                                    } else {
                                        // 2. Khi người dùng chạm (click) vào màn hình
                                        selectedTextByCursor = ""

                                        // Kiểm tra xem vị trí chạm có phải là từ vựng được highlight không
                                        if (prevSelection != newValue.selection) {
                                            val offset = newValue.selection.start
                                            data.annotatedContent.getStringAnnotations("vocab", offset, offset)
                                                .firstOrNull()?.let { annotation ->
                                                    viewModel.onWordClicked(annotation.item)
                                                }
                                        }
                                    }
                                },
                                readOnly = true, // Không hiện bàn phím, chỉ cho đọc và bôi đen
                                textStyle = TextStyle(lineHeight = 2.2.em, fontSize = 18.sp, color = textColor),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    AnimatedVisibility(visible = showTranslation) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                color = primaryBlue.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, primaryBlue.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = lesson.fullTranslation.ifEmpty { "Chưa có bản dịch cho bài này." },
                                    fontSize = 15.sp,
                                    lineHeight = 1.6.em,
                                    color = textColor,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    QuizSection(
                        surfaceColor = surfaceColor,
                        textColor = textColor,
                        viewModel = viewModel,
                        lesson = lesson
                    )

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }

        // POPUP CHI TIẾT TỪ VỰNG (Hiển thị cho cả Click bình thường và Dịch AI)
        if (selectedVocab != null) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.clearSelectedVocab() },
                sheetState = sheetState,
                containerColor = surfaceColor,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                VocabDetailSheet(
                    vocab = selectedVocab!!,
                    textColor = textColor,
                    onSaveClick = {
                        val testUserId = "user_001"
                        viewModel.saveVocab(testUserId, selectedVocab!!)
                    },
                    onSpeakClick = {
                        viewModel.speak(selectedVocab!!.word) // Gọi Text-To-Speech phát âm
                    }
                )
            }
        }
    }
}

// ... (Các component TopNavigationBar, FloatingAudioPlayer giữ nguyên như cũ)

@Composable
fun TopNavigationBar(progress: Float) {
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    Column(modifier = Modifier.background(Color.Transparent)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { }) {
                Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Back", modifier = Modifier.size(20.dp))
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${(progress * 100).toInt()}% Hoàn thành",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = Color(0xFF009688),
                    trackColor = Color.LightGray.copy(alpha = 0.5f),
                )
            }
            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
fun FloatingAudioPlayer(isPlaying: Boolean, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(50),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
        modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onClick() }
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.VolumeUp,
                contentDescription = "Play/Pause Audio",
                tint = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isPlaying) "Đang phát..." else "Nghe Audio",
                color = Color(0xFF1976D2),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun VocabDetailSheet(vocab: VocabDetail, textColor: Color, onSaveClick: () -> Unit, onSpeakClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 40.dp, top = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(vocab.furigana, fontSize = 14.sp, color = Color.Gray)
                Text(vocab.word, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
                if (vocab.hanViet.isNotEmpty()) {
                    Text(
                        vocab.hanViet,
                        fontSize = 13.sp,
                        color = Color(0xFF009688),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            IconButton(
                onClick = onSpeakClick, // ĐÃ GẮN SỰ KIỆN PHÁT ÂM VÀO ĐÂY
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF1976D2), CircleShape)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Nghe", tint = Color.White)
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))

        Text("Ý nghĩa:", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        Text(
            vocab.meaning,
            fontSize = 18.sp,
            color = textColor,
            lineHeight = 1.4.em,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        if (vocab.example.isNotEmpty()) {
            Text("Ví dụ:", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Text(
                vocab.example,
                fontSize = 16.sp,
                color = textColor,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = { /* Mở chi tiết Kanji */ },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Chi tiết Kanji", color = textColor)
            }

            Button(
                onClick = { onSaveClick() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Outlined.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lưu từ")
            }
        }
    }
}

@Composable
fun QuizSection(
    surfaceColor: Color,
    textColor: Color,
    viewModel: ReadingViewModel,
    lesson: ReadingLesson
) {
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    val quizResult by viewModel.quizResult.collectAsState()

    val quiz = lesson.quiz.firstOrNull()
    val questionText = quiz?.question ?: "Bài đọc nói về điều gì là chủ yếu?"
    val options = quiz?.options ?: listOf(
        "Các loại hoa quả mùa hè ở Nhật Bản.",
        "Lễ hội truyền thống và sự kiện theo mùa.",
        "Cách làm pháo hoa ở Nhật."
    )
    val correctIndex = quiz?.correctIndex ?: 1
    val lessonId = lesson.lessonId.ifEmpty { "lesson_n3_001" }
    val testUserId = "user_001"

    Card(
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = Color(0xFFFFB300))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kiểm tra nhanh", fontWeight = FontWeight.Bold, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = questionText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            options.forEachIndexed { index, text ->
                val isSelected = selectedOption == index

                val bgColor = when {
                    quizResult == true && index == correctIndex -> Color(0xFFE8F5E9)
                    quizResult == false && isSelected -> Color(0xFFFFEBEE)
                    isSelected -> Color(0xFFE3F2FD)
                    else -> Color.Transparent
                }

                val borderColor = when {
                    quizResult == true && index == correctIndex -> Color(0xFF4CAF50)
                    quizResult == false && isSelected -> Color(0xFFF44336)
                    isSelected -> Color(0xFF1976D2)
                    else -> Color.LightGray
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                        .background(bgColor, RoundedCornerShape(12.dp))
                        .clickable(enabled = quizResult == null) {
                            selectedOption = index
                            viewModel.resetQuiz()
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = if (quizResult == true) Color(0xFF4CAF50) else if (quizResult == false) Color(0xFFF44336) else Color(0xFF1976D2)
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = text, color = textColor, fontSize = 16.sp)
                }
            }

            AnimatedVisibility(visible = selectedOption != null && quizResult == null) {
                Button(
                    onClick = {
                        viewModel.checkQuizAnswer(testUserId, lessonId, selectedOption!!, correctIndex)
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688))
                ) {
                    Text("Kiểm tra đáp án", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            AnimatedVisibility(visible = quizResult != null) {
                Text(
                    text = if (quizResult == true) "🎉 Chính xác! Bạn đã hoàn thành bài học." else "❌ Sai rồi, thử lại nhé!",
                    color = if (quizResult == true) Color(0xFF4CAF50) else Color(0xFFF44336),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
