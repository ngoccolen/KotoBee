package com.example.kotobee.ui.lessons.vocab

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotobee.data.model.VocabItem
import kotlinx.coroutines.delay

data class MatchCard(
    val id: String,
    val vocabId: String,
    val text: String,
    val type: CardType,
    val isMatched: Boolean = false
)

enum class CardType { WORD, MEANING }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchingGameScreen(
    deckId: String,
    viewModel: VocabManagerViewModel,
    onNavigateBack: () -> Unit
) {
    val vocabs by viewModel.vocabs.collectAsState()
    
    val gameVocabs = remember(vocabs) {
        vocabs.shuffled().take(6)
    }

    var cards by remember {
        mutableStateOf(
            if (gameVocabs.size >= 3) {
                val list = mutableListOf<MatchCard>()
                gameVocabs.forEach { vocab ->
                    list.add(MatchCard(id = "w_${vocab.id}", vocabId = vocab.id, text = vocab.kanji.ifEmpty { vocab.kana }, type = CardType.WORD))
                    list.add(MatchCard(id = "m_${vocab.id}", vocabId = vocab.id, text = vocab.meaning, type = CardType.MEANING))
                }
                list.shuffled()
            } else {
                emptyList()
            }
        )
    }

    var firstSelectedCard by remember { mutableStateOf<MatchCard?>(null) }
    var secondSelectedCard by remember { mutableStateOf<MatchCard?>(null) }
    var isChecking by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }
    var matchAttempts by remember { mutableIntStateOf(0) }
    var matchSuccesses by remember { mutableIntStateOf(0) }

    LaunchedEffect(firstSelectedCard, secondSelectedCard) {
        if (firstSelectedCard != null && secondSelectedCard != null) {
            isChecking = true
            delay(600)
            matchAttempts++
            if (firstSelectedCard!!.vocabId == secondSelectedCard!!.vocabId && firstSelectedCard!!.type != secondSelectedCard!!.type) {
                cards = cards.map { card ->
                    if (card.vocabId == firstSelectedCard!!.vocabId) {
                        card.copy(isMatched = true)
                    } else {
                        card
                    }
                }
                matchSuccesses++
                if (cards.all { it.isMatched }) {
                    isFinished = true
                }
            }
            firstSelectedCard = null
            secondSelectedCard = null
            isChecking = false
        }
    }

    if (gameVocabs.size < 3) {
        Box(Modifier.fillMaxSize().background(AppBackground), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Text("Cần tối thiểu 3 từ vựng trong thư mục để chơi Game Nối Từ!", color = TextDark, fontSize = 16.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onNavigateBack, colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
                    Text("Quay lại")
                }
            }
        }
    } else if (isFinished) {
        Box(Modifier.fillMaxSize().background(AppBackground), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                Text("Chúc mừng!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(Modifier.height(8.dp))
                Text("Bạn đã hoàn thành trò chơi nối từ!", fontSize = 16.sp, color = TextLight)
                Spacer(Modifier.height(24.dp))
                Text("Số lần thử: $matchAttempts lần", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = PrimaryBlue)
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Quay lại danh mục", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        Scaffold(
            containerColor = AppBackground,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .statusBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.Close, null) }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Game Nối Từ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Tiến độ: $matchSuccesses/${gameVocabs.size}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryBlue
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Nhấp chọn một từ và ý nghĩa tương ứng để ghép cặp chúng!",
                    fontSize = 14.sp,
                    color = TextLight,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(cards) { card ->
                        val isSelected = firstSelectedCard?.id == card.id || secondSelectedCard?.id == card.id
                        val isIncorrect = firstSelectedCard != null && secondSelectedCard != null && isSelected && firstSelectedCard!!.vocabId != secondSelectedCard!!.vocabId

                        val backgroundColor = when {
                            card.isMatched -> Color(0xFFF1F5F9)
                            isIncorrect -> Color(0xFFFEE2E2)
                            isSelected -> Color(0xFFEFF6FF)
                            else -> Color.White
                        }

                        val borderColor = when {
                            card.isMatched -> Color.Transparent
                            isIncorrect -> Color(0xFFEF4444)
                            isSelected -> Color(0xFF3B82F6)
                            else -> Color.Transparent
                        }

                        val textColor = when {
                            card.isMatched -> Color(0xFF94A3B8)
                            isIncorrect -> Color(0xFFEF4444)
                            isSelected -> Color(0xFF3B82F6)
                            else -> TextDark
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clickable(enabled = !card.isMatched && !isChecking) {
                                    if (firstSelectedCard == null) {
                                        firstSelectedCard = card
                                    } else if (secondSelectedCard == null && firstSelectedCard!!.id != card.id) {
                                        secondSelectedCard = card
                                    }
                                },
                            shape = RoundedCornerShape(16.dp),
                            color = backgroundColor,
                            border = BorderStroke(1.5.dp, borderColor),
                            shadowElevation = if (card.isMatched) 0.dp else 2.dp
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text(
                                    text = card.text,
                                    color = textColor,
                                    fontSize = if (card.text.length > 10) 14.sp else 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
