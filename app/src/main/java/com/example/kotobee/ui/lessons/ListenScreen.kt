package com.example.kotobee.ui.lessons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class WordDetail(
    val word: String,
    val reading: String,
    val meaning: String
)

data class TranscriptLine(
    val speaker: String,
    val jpText: String,
    val viText: String,
    val vocab: List<WordDetail>
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListeningPracticeScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedWord by remember { mutableStateOf<WordDetail?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var showViTranslation by remember { mutableStateOf(true) }

    val tabs = listOf("Phụ đề trực tiếp", "Kiểm tra nhanh")

    Scaffold(
        containerColor = Color(0xFFFAF7F2)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LessonHeader()

            AudioPlayerControls(
                isPlaying = isPlaying,
                onPlayPauseClick = { isPlaying = !isPlaying },
                showViTranslation = showViTranslation,
                onToggleTranslation = { showViTranslation = !showViTranslation }
            )

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color(0xFF8B5A2B)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            if (selectedTab == 0) {
                TranscriptSection(
                    showViTranslation = showViTranslation,
                    onWordClick = { word -> selectedWord = word }
                )
            } else {
                QuizSection()
            }
        }
    }

    selectedWord?.let { word ->
        ModalBottomSheet(
            onDismissRequest = { selectedWord = null },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(word.reading, fontSize = 16.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(word.word, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5A2B))
                Spacer(modifier = Modifier.height(16.dp))
                Text(word.meaning, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun LessonHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFD7CCC8))
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Surface(
                color = Color(0xFFFFC107),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "TRÌNH ĐỘ N3",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Đối thoại tại Quán Cà phê",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3E2723)
            )
        }
    }
}

@Composable
fun AudioPlayerControls(
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    showViTranslation: Boolean,
    onToggleTranslation: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            LinearProgressIndicator(
                progress = { 0.4f },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFB8860B)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("02:14", fontSize = 12.sp, color = Color.Gray)
                Text("04:50", fontSize = 12.sp, color = Color.Gray)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { }) {
                    Text("⏪ 10s", fontSize = 16.sp, color = Color(0xFF6B4226))
                }

                Button(
                    onClick = onPlayPauseClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B4226)),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(if (isPlaying) "⏸" else "▶", fontSize = 20.sp, modifier = Modifier.padding(8.dp))
                }

                TextButton(onClick = { }) {
                    Text("10s ⏩", fontSize = 16.sp, color = Color(0xFF6B4226))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = { }) {
                    Text("Tốc độ: 1.0x", color = Color(0xFF6B4226), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                TextButton(onClick = onToggleTranslation) {
                    Text(if (showViTranslation) "Ẩn Tiếng Việt" else "Hiện Tiếng Việt", color = Color(0xFF6B4226), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TranscriptSection(
    showViTranslation: Boolean,
    onWordClick: (WordDetail) -> Unit
) {
    val dialogues = listOf(
        TranscriptLine(
            "Customer",
            "すみません、メニューを見せていただけますか？",
            "Xin lỗi, bạn có thể cho tôi xem thực đơn được không?",
            listOf(
                WordDetail("メニュー", "めにゅー", "Thực đơn"),
                WordDetail("見せて", "みせて", "Cho xem")
            )
        ),
        TranscriptLine(
            "Staff",
            "はい、こちらがメニューでございます。ご注文はお決まりですか？",
            "Vâng, đây là thực đơn ạ. Quý khách đã chọn được món chưa?",
            listOf(
                WordDetail("注文", "ちゅうもん", "Gọi món / Đặt hàng"),
                WordDetail("決まり", "きまり", "Quyết định")
            )
        )
    )

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(dialogues) { dialogue ->
            val isStaff = dialogue.speaker == "Staff"
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isStaff) Color.White else Color(0xFFF0EBE1)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isStaff) 2.dp else 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = dialogue.speaker,
                        color = if (isStaff) Color(0xFFB8860B) else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    val annotatedString = buildAnnotatedString {
                        val text = dialogue.jpText
                        append(text)
                        dialogue.vocab.forEach { wordDetail ->
                            val startIndex = text.indexOf(wordDetail.word)
                            if (startIndex >= 0) {
                                val endIndex = startIndex + wordDetail.word.length
                                addStyle(
                                    style = SpanStyle(
                                        color = Color(0xFFD84315),
                                        fontWeight = FontWeight.ExtraBold,
                                        background = Color(0xFFFFCCBC)
                                    ),
                                    start = startIndex,
                                    end = endIndex
                                )
                                addStringAnnotation(
                                    tag = "VOCAB",
                                    annotation = wordDetail.word,
                                    start = startIndex,
                                    end = endIndex
                                )
                            }
                        }
                    }

                    ClickableText(
                        text = annotatedString,
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.Black),
                        onClick = { offset ->
                            annotatedString.getStringAnnotations("VOCAB", offset, offset)
                                .firstOrNull()?.let { annotation ->
                                    val clickedWord = dialogue.vocab.find { it.word == annotation.item }
                                    if (clickedWord != null) onWordClick(clickedWord)
                                }
                        }
                    )

                    if (showViTranslation) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = dialogue.viText, fontSize = 15.sp, color = Color.DarkGray)
                    }
                }
            }
        }
    }
}

@Composable
fun QuizSection() {
    val questions = listOf(
        QuizQuestion(
            question = "Khách hàng đã gọi món uống gì?",
            options = listOf("A. Black Coffee", "B. Cafe Latte", "C. Matcha"),
            correctAnswerIndex = 1
        )
    )

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(questions) { quiz ->
            var selectedOption by remember { mutableStateOf<Int?>(null) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE4C4)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(quiz.question, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF3E2723))
                    Spacer(modifier = Modifier.height(16.dp))

                    quiz.options.forEachIndexed { index, option ->
                        val isSelected = selectedOption == index
                        val backgroundColor = if (isSelected) Color(0xFFB8860B) else Color.White
                        val textColor = if (isSelected) Color.White else Color.Black

                        Button(
                            onClick = { selectedOption = index },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(option, color = textColor, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }
}