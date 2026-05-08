package com.example.kotobee.ui.lessons.vocab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

val AppBackground = Color(0xFFF8F9FB)
val PrimaryBlue = Color(0xFF4A90E2)
val TextDark = Color(0xFF2C3A4B)
val TextLight = Color(0xFF8A95A5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVocabScreen(
    deckId: String,
    viewModel: VocabManagerViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    // Chỉ cần 2 biến: Thuật ngữ và Định nghĩa
    var term by remember { mutableStateOf("") }
    var definition by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { Text("Thêm Flashcard", fontWeight = FontWeight.Bold, color = TextDark) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextDark) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = term, onValueChange = { term = it },
                label = { Text("Thuật ngữ (Từ vựng)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue)
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = definition, onValueChange = { definition = it },
                label = { Text("Định nghĩa (Ý nghĩa)") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    if (term.isNotBlank() && definition.isNotBlank()) {
                        // Truyền chuỗi rỗng cho các trường không dùng nữa
                        viewModel.addVocab(deckId, kanji = "", kana = term, meaning = definition, example = "", exampleMeaning = "") {
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                enabled = !isLoading && term.isNotBlank() && definition.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Lưu Flashcard", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}