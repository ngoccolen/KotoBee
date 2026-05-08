package com.example.kotobee.ui.lessons.writing

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotobee.data.model.KanjiEntity // Nhớ import đúng model Entity của bạn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanjiListScreen(
    onKanjiClick: (String) -> Unit,
    onBackClick: () -> Unit,
    // Tạm thời mình truyền dummy list, bạn có thể nối với ViewModel để lấy từ DB sau
    kanjiList: List<KanjiEntity> = emptyList()
) {
    var searchQuery by remember { mutableStateOf("") }

    // Lọc danh sách dựa trên tìm kiếm (có thể lọc theo Kanji, Hán Việt hoặc nghĩa)
    val filteredList = kanjiList.filter {
        it.character.contains(searchQuery, ignoreCase = true) ||
                it.meaning.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Danh sách Hán tự", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFFDFD), // Màu ThemeBackground
                    titleContentColor = Color(0xFF333333)
                )
            )
        },
        containerColor = Color(0xFFFFFDFD)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Thanh tìm kiếm
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Tìm kiếm hán tự, nghĩa...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE53935), // ProgressPrimary
                    focusedLeadingIconColor = Color(0xFFE53935)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lưới danh sách Kanji
            LazyVerticalGrid(
                columns = GridCells.Fixed(4), // Hiển thị 4 chữ 1 hàng
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList) { kanji ->
                    KanjiCard(kanji, onClick = { onKanjiClick(kanji.character) })
                }
            }
        }
    }
}

@Composable
fun KanjiCard(kanji: KanjiEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = kanji.character,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = kanji.meaning,
                fontSize = 10.sp,
                color = Color.Gray,
                maxLines = 1
            )
        }
    }
}