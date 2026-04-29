package com.example.kotobee.ui.lessons
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- BẢNG MÀU DỰA TRÊN KOTOBEE (JAPANESE PASTEL) ---
val BackgroundColor = Color(0xFFF8F9FA)
val PrimaryRed = Color(0xFFD32F2F)
val PrimaryRedLight = Color(0xFFFFEBEE)
val TextDark = Color(0xFF212121)
val TextGray = Color(0xFF757575)
val SurfaceColor = Color(0xFFFFFFFF)
val TagKunColor = Color(0xFFE3F2FD)
val TagKunText = Color(0xFF1976D2)
val TagOnColor = Color(0xFFFBE9E7)
val TagOnText = Color(0xFFD84315)

// --- DATA CLASS & MOCK DATA ---
data class KanjiVocab(
    val kanjiWord: String,
    val furigana: String,
    val meaning: String
)

data class KanjiDetailModel(
    val character: String,
    val hanViet: String,
    val level: String,
    val mnemonic: String,
    val kunyomi: List<String>,
    val onyomi: List<String>,
    val radical: String,
    val vocabularies: List<KanjiVocab>
)

val mockKanjiData = KanjiDetailModel(
    character = "日",
    hanViet = "NHẬT",
    level = "N5",
    mnemonic = "Đây là hình dạng của mặt trời.",
    kunyomi = listOf("ひ", "び", "か"),
    onyomi = listOf("ニチ", "ジツ"),
    radical = "日 (Nhật)",
    vocabularies = listOf(
        KanjiVocab("日本人", "にほんじん", "Người Nhật"),
        KanjiVocab("休日", "きゅうじつ", "Ngày nghỉ"),
        KanjiVocab("日曜日", "にちようび", "Chủ nhật"),
        KanjiVocab("毎日", "まいにち", "Mỗi ngày")
    )
)

// --- MAIN SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanjiDetailScreen(
    kanjiData: KanjiDetailModel = mockKanjiData,
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text("Chi tiết Kanji", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Bookmark */ }) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Lưu")
                    }
                    IconButton(onClick = { /* TODO: Hint */ }) {
                        Icon(Icons.Default.Lightbulb, contentDescription = "Gợi ý")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceColor,
                    titleContentColor = TextDark,
                    navigationIconContentColor = TextDark,
                    actionIconContentColor = TextDark
                )
            )
        },
        bottomBar = {
            BottomNavigationSection(currentIndex = 1, totalItems = 15)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 1. Thẻ Kanji chính
            item { HeroKanjiCard(kanjiData) }

            // 2. Cách ghi nhớ
            item { SectionTitle("CÁCH GHI NHỚ") }
            item { MnemonicCard(kanjiData.mnemonic) }

            // 3. Âm đọc & Bộ thủ
            item { SectionTitle("ÂM ĐỌC & BỘ THỦ") }
            item { ReadingAndRadicalCard(kanjiData) }

            // 4. Từ vựng minh họa
            item { SectionTitle("TỪ VỰNG MINH HỌA") }
            items(kanjiData.vocabularies) { vocab ->
                VocabularyItem(vocab)
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

// --- COMPOSABLES ---

@Composable
fun HeroKanjiCard(data: KanjiDetailModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            // Level Badge
            Surface(
                shape = CircleShape,
                color = PrimaryRed,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(
                    text = data.level,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = data.character,
                    fontSize = 100.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryRed
                )
                Text(
                    text = data.hanViet,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

@Composable
fun MnemonicCard(mnemonic: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Giả lập vùng chứa hình ảnh cách ghi nhớ
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(BackgroundColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("☀️ ➔ 🌞 ➔ 日", fontSize = 32.sp) // Thay bằng Image thực tế
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = mnemonic,
                fontSize = 15.sp,
                color = TextDark,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReadingAndRadicalCard(data: KanjiDetailModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Âm KUN
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Âm Kun",
                    modifier = Modifier.width(80.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextGray
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    data.kunyomi.forEach { reading ->
                        ReadingChip(text = reading, backgroundColor = TagKunColor, textColor = TagKunText)
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = BackgroundColor)

            // Âm ON
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Âm On",
                    modifier = Modifier.width(80.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextGray
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    data.onyomi.forEach { reading ->
                        ReadingChip(text = reading, backgroundColor = TagOnColor, textColor = TagOnText)
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = BackgroundColor)

            // Bộ thủ
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Bộ thủ",
                    modifier = Modifier.width(80.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextGray
                )
                Text(
                    text = data.radical,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextDark
                )
            }
        }
    }
}

@Composable
fun ReadingChip(text: String, backgroundColor: Color, textColor: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun VocabularyItem(vocab: KanjiVocab) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nút loa (Audio)
            IconButton(
                onClick = { /* TODO: Play Audio */ },
                modifier = Modifier
                    .size(48.dp)
                    .background(PrimaryRedLight, CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Phát âm",
                    tint = PrimaryRed
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Chữ Hán & Furigana
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vocab.furigana,
                    fontSize = 12.sp,
                    color = TextGray
                )
                Text(
                    text = vocab.kanjiWord,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryRed
                )
            }

            // Ý nghĩa
            Text(
                text = vocab.meaning,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = TextGray,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun BottomNavigationSection(currentIndex: Int, totalItems: Int) {
    Surface(
        color = SurfaceColor,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nút Back
            FilledIconButton(
                onClick = { /* TODO: Prev */ },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = PrimaryRedLight,
                    contentColor = PrimaryRed
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Trang trước")
            }

            // Chỉ báo tiến độ
            Text(
                text = "$currentIndex / $totalItems",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            // Nút Next
            FilledIconButton(
                onClick = { /* TODO: Next */ },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = PrimaryRed,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Trang sau")
            }
        }
    }
}