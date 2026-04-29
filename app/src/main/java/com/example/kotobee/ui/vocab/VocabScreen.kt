package com.example.kotobee.ui.vocab

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kotobee.data.model.VocabItem

val FlashcardFront = Color(0xFFFFFFFF)
val FlashcardBack = Color(0xFFF0F4F8)
val TextPrimary = Color(0xFF1E293B)
val TextSecondary = Color(0xFF64748B)
val ButtonRed = Color(0xFFFEE2E2)
val TextRed = Color(0xFFEF4444)
val ButtonYellow = Color(0xFFFEF3C7)
val TextYellow = Color(0xFFF59E0B)
val ButtonGreen = Color(0xFFDCFCE7)
val TextGreen = Color(0xFF22C55E)

@Composable
fun VocabularyPracticeScreen(
    deckId: String,
    viewModel: VocabManagerViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val vocabList by viewModel.vocabs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(deckId) {
        viewModel.loadVocabs(deckId)
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryBlue)
        }
    } else if (vocabList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Chưa có từ vựng nào trong thư mục này", color = TextSecondary)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onNavigateBack) {
                    Text("Quay lại")
                }
            }
        }
    } else {
        VocabularyPracticeContent(vocabList = vocabList, onNavigateBack = onNavigateBack)
    }
}

@Composable
fun VocabularyPracticeContent(
    vocabList: List<VocabItem>,
    onNavigateBack: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { vocabList.size })

    Scaffold(
        containerColor = AppBackground,
        topBar = { VocabTopBar(pagerState.currentPage + 1, vocabList.size, onNavigateBack) },
        bottomBar = { SpacedRepetitionBottomBar() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 32.dp),
                pageSpacing = 16.dp
            ) { page ->
                FlashcardItem(vocab = vocabList[page])
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun VocabTopBar(current: Int, total: Int, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.Filled.Close, contentDescription = null, tint = TextPrimary)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Ôn tập",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { current.toFloat() / total },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = PrimaryBlue,
                trackColor = DividerColor
            )
        }

        IconButton(onClick = { }) {
            Icon(Icons.Filled.MoreVert, contentDescription = null, tint = TextPrimary)
        }
    }
}

@Composable
fun FlashcardItem(vocab: VocabItem) {
    var flipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "flip"
    )
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 16f * density
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { flipped = !flipped },
        shape = RoundedCornerShape(32.dp),
        color = if (rotation > 90f) FlashcardBack else FlashcardFront,
        shadowElevation = 8.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (rotation <= 90f) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = vocab.kana,
                        fontSize = 22.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = vocab.kanji,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Filled.StarBorder, contentDescription = null, tint = TextLight)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                        .graphicsLayer { rotationY = 180f },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Ý nghĩa",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = vocab.meaning,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(40.dp))
                    Divider(color = DividerColor, thickness = 2.dp)
                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Ví dụ",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = vocab.example,
                        fontSize = 18.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = vocab.exampleMeaning,
                        fontSize = 15.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun SpacedRepetitionBottomBar() {
    Surface(
        color = AppBackground,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RepetitionButton(
                text = "Quên",
                containerColor = ButtonRed,
                contentColor = TextRed,
                modifier = Modifier.weight(1f)
            )
            RepetitionButton(
                text = "Khó",
                containerColor = ButtonYellow,
                contentColor = TextYellow,
                modifier = Modifier.weight(1f)
            )
            RepetitionButton(
                text = "Dễ",
                containerColor = ButtonGreen,
                contentColor = TextGreen,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun RepetitionButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { },
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.height(56.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
