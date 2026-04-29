package com.example.kotobee.ui.lessons.grammar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GrammarPracticeScreen() {
    // Trạng thái cho Bottom Sheet giải thích ngữ pháp
    var showGrammarPopup by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Dữ liệu giả lập cho câu hỏi Sắp xếp từ
    val question = "Tôi phải đi đến trường."
    val originalWords = listOf("学校", "に", "行か", "なければ", "なりません", "。")

    // Trạng thái quản lý các từ đang ở dưới (chưa chọn) và ở trên (đã chọn)
    var availableWords by remember { mutableStateOf(originalWords.shuffled()) }
    var selectedWords by remember { mutableStateOf(listOf<String>()) }

    Scaffold(
        containerColor = Color(0xFFF9FAFB), // Xám nhạt dịu mắt
        topBar = { GrammarTopBar(progress = 0.4f, timerText = "01:45") },
        bottomBar = {
            GrammarBottomBar(
                isAnswerEmpty = selectedWords.isEmpty(),
                onCheckClick = { /* Logic kiểm tra đáp án */ }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Tiêu đề câu hỏi & Nút xem gợi ý
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sắp xếp từ thành câu đúng",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF374151)
                )
                IconButton(
                    onClick = { showGrammarPopup = true },
                    modifier = Modifier.background(Color(0xFFE0E7FF), RoundedCornerShape(12.dp)).size(40.dp)
                ) {
                    Icon(Icons.Filled.Lightbulb, contentDescription = "Giải thích", tint = Color(0xFF4F46E5))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Câu tiếng Việt cần dịch
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Translate, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = question, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Khu vực chứa các từ ĐÃ CHỌN (Target Area)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(2.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                if (selectedWords.isEmpty()) {
                    Text(
                        text = "Chạm vào các từ bên dưới để ghép câu...",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        selectedWords.forEach { word ->
                            WordChip(word = word, isSelected = true) {
                                // Logic khi bấm vào từ đã chọn: Trả nó về danh sách bên dưới
                                selectedWords = selectedWords - word
                                availableWords = availableWords + word
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Khu vực chứa các từ CHƯA CHỌN (Source Area)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                availableWords.forEach { word ->
                    WordChip(word = word, isSelected = false) {
                        // Logic khi bấm vào từ chưa chọn: Đưa nó lên danh sách bên trên
                        availableWords = availableWords - word
                        selectedWords = selectedWords + word
                    }
                }
            }
        }

        // Popup Giải thích ngữ pháp (Feature #7)
        if (showGrammarPopup) {
            ModalBottomSheet(
                onDismissRequest = { showGrammarPopup = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                GrammarExplanationContent()
            }
        }
    }
}

@Composable
fun WordChip(word: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF4F46E5) else Color.White)
            .border(
                width = 2.dp,
                color = if (isSelected) Color.Transparent else Color(0xFFD1D5DB),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = word,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color.White else Color(0xFF374151)
        )
    }
}

@Composable
fun GrammarTopBar(progress: Float, timerText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { /* Thoát bài */ }) {
            Icon(Icons.Filled.Close, "Đóng", tint = Color.Gray)
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(50)),
            color = Color(0xFF10B981), // Màu xanh lá tiến độ
            trackColor = Color(0xFFE5E7EB)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Timer cho Quiz ngữ pháp nhanh (Feature #5)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Timer, "Thời gian", tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(timerText, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
        }
    }
}

@Composable
fun GrammarBottomBar(isAnswerEmpty: Boolean, onCheckClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nút Lưu lỗi sai (Feature #11)
            IconButton(
                onClick = { /* Lưu vào Room Database */ },
                modifier = Modifier.background(Color(0xFFFEE2E2), RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Filled.BookmarkBorder, contentDescription = "Lưu câu này", tint = Color(0xFFEF4444))
            }

            // Nút Kiểm tra
            Button(
                onClick = onCheckClick,
                enabled = !isAnswerEmpty, // Vô hiệu hóa nếu chưa chọn từ nào
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    disabledContainerColor = Color(0xFFD1D5DB)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
                    .height(54.dp)
            ) {
                Text("KIỂM TRA", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
fun GrammarExplanationContent() {
    Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
        Text("Cấu trúc:", color = Color.Gray, fontSize = 14.sp)
        Text(
            text = "~ なければなりません",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4F46E5),
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        Text("Ý nghĩa:", fontWeight = FontWeight.Bold)
        Text("Phải làm gì đó (Bắt buộc).", fontSize = 16.sp, modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))

        HorizontalDivider(color = Color(0xFFE5E7EB))

        Text("Cách chia:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
        Text("Động từ thể Nai (bỏ ない) + なければなりません", fontSize = 16.sp, modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))

        // Nút xem thêm ví dụ (Feature #8)
        OutlinedButton(
            onClick = { /* Mở danh sách ví dụ */ },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Xem thêm ví dụ tương tự")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
