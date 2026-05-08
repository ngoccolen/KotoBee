package com.example.kotobee.ui.lessons.vocab

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kotobee.data.model.Deck

val SurfaceWhite = Color(0xFFFFFFFF)
val DividerColor = Color(0xFFEEF0F4)

@Composable
fun DeckListScreen(
    viewModel: VocabManagerViewModel = viewModel(),
    onDeckClick: (String) -> Unit
) {
    val decks by viewModel.decks.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (decks.isEmpty()) { viewModel.loadDecks() }
    }

    // ĐÃ FIX: Dùng Scaffold để tránh nút FAB bị thanh điều hướng điện thoại che mất
    Scaffold(
        containerColor = AppBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryBlue,
                contentColor = SurfaceWhite,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.navigationBarsPadding() // Đẩy lên khỏi thanh điều hướng
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm thư mục")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                text = "Thư viện của tôi",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 24.dp)
            )

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (decks.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                            Text("Chưa có thư mục nào.\nNhấn '+' góc dưới để tạo.", color = TextLight, textAlign = TextAlign.Center, fontSize = 15.sp)
                        }
                    }
                } else {
                    items(decks) { deck ->
                        DeckItemCard(
                            deck = deck,
                            onClick = { onDeckClick(deck.id) },
                            onEdit = { name, desc -> viewModel.updateDeck(deck.id, name, desc) },
                            onDelete = { viewModel.deleteDeck(deck.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(100.dp)) } // Padding chống che chữ
                }
            }
        }
    }

    if (showAddDialog) {
        DeckActionDialog(
            title = "Tạo thư mục mới",
            initialName = "",
            initialDesc = "",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, desc ->
                viewModel.createDeck(name, desc)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun DeckItemCard(deck: Deck, onClick: () -> Unit, onEdit: (String, String) -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = SurfaceWhite,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(Color(0xFFEBF3FC), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = deck.name, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = deck.description.ifEmpty { "Không có mô tả" }, fontSize = 13.sp, color = TextLight, maxLines = 1)
            }

            // MENU 3 CHẤM
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = TextLight)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(
                        text = { Text("Chỉnh sửa") },
                        onClick = { expanded = false; showEditDialog = true },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = PrimaryBlue) }
                    )
                    DropdownMenuItem(
                        text = { Text("Xóa thư mục", color = TextRed) },
                        onClick = { expanded = false; showDeleteConfirm = true },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = TextRed) }
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        DeckActionDialog(
            title = "Chỉnh sửa thư mục",
            initialName = deck.name,
            initialDesc = deck.description,
            onDismiss = { showEditDialog = false },
            onConfirm = { name, desc ->
                onEdit(name, desc)
                showEditDialog = false
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Xác nhận xóa", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn xóa thư mục '${deck.name}' không? Các từ vựng bên trong sẽ bị mất.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteConfirm = false }) {
                    Text("Xóa", color = TextRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Hủy", color = TextDark) }
            },
            containerColor = Color.White
        )
    }
}

// Hàm dùng chung cho cả việc Tạo và Sửa
@Composable
fun DeckActionDialog(title: String, initialName: String, initialDesc: String, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDesc) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = SurfaceWhite, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    placeholder = { Text("Tên thư mục", color = TextLight) },
                    singleLine = true, shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue, unfocusedBorderColor = DividerColor),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    placeholder = { Text("Mô tả ngắn", color = TextLight) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue, unfocusedBorderColor = DividerColor),
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Hủy", color = TextLight, fontWeight = FontWeight.Medium) }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { if (name.isNotBlank()) onConfirm(name, description) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) { Text("Lưu", fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}