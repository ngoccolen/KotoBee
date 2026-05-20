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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kotobee.data.model.Grammar
import com.example.kotobee.data.model.GrammarQuestion

@Composable
fun GrammarPracticeScreen(
    grammarId: String = "n5_te_kudasai",
    viewModel: GrammarViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val grammar by viewModel.currentGrammar.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val lessonAccess by viewModel.currentLessonAccess.collectAsState()
    val quizSaveState by viewModel.quizSaveState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var currentIndex by remember(grammarId, questions.size) { mutableIntStateOf(0) }
    var correctCount by remember(grammarId, questions.size) { mutableIntStateOf(0) }
    var selectedAnswer by remember(grammarId, currentIndex) { mutableStateOf<String?>(null) }
    var checked by remember(grammarId, currentIndex) { mutableStateOf(false) }
    var showHint by remember(grammarId, currentIndex) { mutableStateOf(false) }
    var finished by remember(grammarId, questions.size) { mutableStateOf(false) }
    var resultSaved by remember(grammarId, finished) { mutableStateOf(false) }

    LaunchedEffect(grammarId) {
        viewModel.loadGrammarDetail(grammarId)
    }

    LaunchedEffect(grammarId, finished) {
        if (!finished) viewModel.resetQuizSaveState()
    }

    if (isLoading && grammar == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GrammarSurface),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = GrammarCoral)
        }
        return
    }

    if (lessonAccess?.unlocked == false) {
        GrammarPracticeLocked(
            lessonAccess = lessonAccess!!,
            onBackClick = onBackClick
        )
        return
    }

    if (grammar == null || questions.isEmpty()) {
        GrammarPracticeEmpty(onBackClick = onBackClick)
        return
    }

    if (finished) {
        val totalQuestions = questions.size
        val score = ((correctCount.toFloat() / totalQuestions.toFloat()) * 100).toInt()

        LaunchedEffect(grammar!!.id, correctCount, totalQuestions) {
            if (!resultSaved) {
                resultSaved = true
                viewModel.saveGrammarQuizResult(correctCount, totalQuestions)
            }
        }

        GrammarQuizResultScreen(
            grammar = grammar!!,
            correctCount = correctCount,
            totalQuestions = totalQuestions,
            score = score,
            isSaving = quizSaveState.isSaving,
            saveError = quizSaveState.errorMessage,
            onRetry = {
                currentIndex = 0
                correctCount = 0
                selectedAnswer = null
                checked = false
                showHint = false
                finished = false
                resultSaved = false
                viewModel.resetQuizSaveState()
            },
            onBackClick = onBackClick
        )
        return
    }

    val question = questions[currentIndex.coerceIn(0, questions.lastIndex)]
    val isCorrect = selectedAnswer == question.correctAnswer
    val progress = ((currentIndex + if (checked) 1 else 0).toFloat() / questions.size.toFloat())
        .coerceIn(0.08f, 1f)

    Scaffold(
        containerColor = GrammarSurface,
        topBar = {
            GrammarPracticeTopBar(
                level = grammar!!.level,
                progress = progress,
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = Color.White) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            selectedAnswer = null
                            checked = false
                            showHint = false
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (!checked) {
                                if (isCorrect) correctCount += 1
                                checked = true
                            } else if (currentIndex == questions.lastIndex) {
                                finished = true
                            } else {
                                currentIndex += 1
                            }
                        },
                        enabled = selectedAnswer != null || checked,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GrammarMint,
                            disabledContainerColor = Color(0xFFD6DBE3)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text(
                            when {
                                !checked -> "KIỂM TRA"
                                currentIndex == questions.lastIndex -> "XEM KẾT QUẢ"
                                else -> "CÂU TIẾP"
                            },
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Text(
                "Câu ${currentIndex + 1}/${questions.size}",
                color = GrammarCoral,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            GrammarPracticeQuestionCard(question = question)

            Spacer(modifier = Modifier.height(18.dp))
            Text("Chọn đáp án đúng", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = GrammarInk)
            Spacer(modifier = Modifier.height(10.dp))

            question.options.forEach { option ->
                GrammarAnswerOption(
                    text = option,
                    selected = selectedAnswer == option,
                    checked = checked,
                    correct = option == question.correctAnswer,
                    onClick = {
                        if (!checked) selectedAnswer = option
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (checked) {
                GrammarResultCard(isCorrect = isCorrect, question = question)
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun GrammarQuizResultScreen(
    grammar: Grammar,
    correctCount: Int,
    totalQuestions: Int,
    score: Int,
    isSaving: Boolean,
    saveError: String?,
    onRetry: () -> Unit,
    onBackClick: () -> Unit
) {
    val passed = score >= 80
    val color = if (passed) GrammarMint else GrammarCoral

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
                .size(78.dp)
                .background(color.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (passed) Icons.Default.CheckCircle else Icons.Default.SentimentDissatisfied,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(42.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            if (passed) "Đã hoàn thành" else "Chưa đạt",
            color = GrammarInk,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(grammar.title, color = GrammarMuted, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
        Spacer(modifier = Modifier.height(18.dp))
        Text("$score%", color = color, fontSize = 52.sp, fontWeight = FontWeight.ExtraBold)
        Text("$correctCount/$totalQuestions câu đúng", color = GrammarMuted, fontSize = 14.sp)
        Text(
            if (passed) "Bài sau đã được mở khóa nếu đây là lần đầu bạn đạt quiz." else "Cần đạt tối thiểu 80% để mở khóa bài sau.",
            color = GrammarMuted,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            modifier = Modifier.padding(top = 12.dp)
        )

        if (isSaving) {
            Spacer(modifier = Modifier.height(14.dp))
            CircularProgressIndicator(color = GrammarMint, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        }

        if (!saveError.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(saveError, color = GrammarCoral, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(22.dp))
        Button(
            onClick = onBackClick,
            colors = ButtonDefaults.buttonColors(containerColor = color),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Quay lại danh sách", fontWeight = FontWeight.ExtraBold)
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = onRetry,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Làm lại quiz", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun GrammarPracticeLocked(
    lessonAccess: GrammarLessonAccess,
    onBackClick: () -> Unit
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
        Text("Quiz đang khóa", color = GrammarInk, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        Text(
            "Hoàn thành \"${lessonAccess.previousTitle ?: "bài trước"}\" để luyện ${lessonAccess.grammar.title}.",
            color = GrammarMuted,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
        Spacer(modifier = Modifier.height(18.dp))
        Button(onClick = onBackClick, colors = ButtonDefaults.buttonColors(containerColor = GrammarCoral)) {
            Text("Quay lại danh sách")
        }
    }
}

@Composable
private fun GrammarPracticeTopBar(
    level: String,
    progress: Float,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GrammarSurface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.Default.Close, contentDescription = "Đóng", tint = GrammarMuted)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(CircleShape),
            color = GrammarMint,
            trackColor = Color(0xFFE7ECEF)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(level, color = GrammarCoral, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun GrammarPracticeQuestionCard(
    question: GrammarQuestion
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, GrammarLine)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = question.content,
                color = GrammarInk,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 28.sp
            )
        }
    }
}

@Composable
private fun GrammarAnswerOption(
    text: String,
    selected: Boolean,
    checked: Boolean,
    correct: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        checked && correct -> GrammarMint
        checked && selected && !correct -> GrammarCoral
        selected -> JapaneseIndigo
        else -> GrammarLine
    }
    val backgroundColor = when {
        checked && correct -> GrammarMint.copy(alpha = 0.12f)
        checked && selected && !correct -> GrammarCoral.copy(alpha = 0.10f)
        selected -> JapaneseIndigo.copy(alpha = 0.10f)
        else -> Color.White
    }
    val icon = if (checked && correct) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked
    val iconTint = when {
        checked && correct -> GrammarMint
        checked && selected && !correct -> GrammarCoral
        selected -> JapaneseIndigo
        else -> GrammarMuted
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !checked) { onClick() },
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = iconTint)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text, color = GrammarInk, fontSize = 15.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
        }
    }
}

@Composable
private fun GrammarResultCard(
    isCorrect: Boolean,
    question: GrammarQuestion
) {
    val color = if (isCorrect) GrammarMint else GrammarCoral
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.38f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isCorrect) Icons.Default.CheckCircle else Icons.Default.SentimentDissatisfied,
                    contentDescription = null,
                    tint = color
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isCorrect) "Chính xác" else "Chưa đúng",
                    color = color,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Đáp án: ${question.correctAnswer}", color = GrammarInk, fontSize = 14.sp, fontWeight = FontWeight.Bold, lineHeight = 21.sp)
            if (question.hint.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = color.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(10.dp))
                Text("Giải thích:", color = GrammarInk, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(question.hint, color = GrammarMuted, fontSize = 13.sp, lineHeight = 19.sp)
            }
        }
    }
}

@Composable
private fun GrammarPracticeEmpty(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrammarSurface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Chưa có câu luyện tập cho bài này.", color = GrammarInk, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onBackClick, colors = ButtonDefaults.buttonColors(containerColor = GrammarCoral)) {
            Text("Quay lại bài học")
        }
    }
}
