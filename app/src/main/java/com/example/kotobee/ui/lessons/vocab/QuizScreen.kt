package com.example.kotobee.ui.lessons.vocab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun QuizScreen(
    deckId: String,
    viewModel: VocabManagerViewModel,
    onNavigateBack: () -> Unit
) {
    val vocabs by viewModel.vocabs.collectAsState()
    val quizQuestions = remember(vocabs) { viewModel.generateQuiz(deckId) }

    var currentIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var isFinished by remember { mutableStateOf(false) }

    // Xử lý khi chọn đáp án
    LaunchedEffect(selectedAnswer) {
        if (selectedAnswer != null) {
            delay(800) // Đợi một chút để người dùng thấy màu đúng/sai
            if (selectedAnswer == quizQuestions[currentIndex].correctAnswer) {
                score++
            }

            if (currentIndex < quizQuestions.size - 1) {
                currentIndex++
                selectedAnswer = null
            } else {
                isFinished = true
            }
        }
    }

    if (isFinished) {
        QuizResultScreen(score, quizQuestions.size, onNavigateBack)
    } else if (quizQuestions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Cần ít nhất 4 từ để bắt đầu Quiz")
        }
    } else {
        val currentQuestion = quizQuestions[currentIndex]

        Scaffold(
            containerColor = AppBackground,
            topBar = {
                Row(Modifier.fillMaxWidth().padding(16.dp).statusBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.Close, null) }
                    LinearProgressIndicator(
                        progress = { (currentIndex + 1).toFloat() / quizQuestions.size },
                        modifier = Modifier.weight(1f).height(8.dp).padding(horizontal = 16.dp),
                        color = PrimaryBlue, trackColor = DividerColor
                    )
                }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {

                Text("Chọn nghĩa đúng của từ:", color = TextLight, fontSize = 16.sp)
                Spacer(Modifier.height(16.dp))

                // Thẻ câu hỏi
                Card(
                    Modifier.fillMaxWidth().height(180.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(currentQuestion.vocab.kanji.ifEmpty { currentQuestion.vocab.kana },
                            fontSize = 40.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                }

                Spacer(Modifier.height(40.dp))

                // Các lựa chọn đáp án
                currentQuestion.options.forEach { option ->
                    val isCorrect = option == currentQuestion.correctAnswer
                    val isSelected = selectedAnswer == option

                    val color = when {
                        selectedAnswer == null -> Color.White
                        isCorrect -> ButtonGreen
                        isSelected && !isCorrect -> ButtonRed
                        else -> Color.White
                    }

                    Button(
                        onClick = { if (selectedAnswer == null) selectedAnswer = option },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).height(60.dp)
                            .border(1.dp, if (selectedAnswer != null && isCorrect) TextGreen else Color.Transparent, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = TextDark)
                    ) {
                        Text(option, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun QuizResultScreen(score: Int, total: Int, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Kết quả", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("$score / $total", fontSize = 60.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlue)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
            Text("Quay về")
        }
    }
}