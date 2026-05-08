package com.example.kotobee.ui.lessons.vocab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kotobee.data.model.VocabItem

val TextYellow = Color(0xFFF59E0B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckDetailScreen(
    deckId: String,
    viewModel: VocabManagerViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToAddVocab: (String) -> Unit,
    onNavigateToPractice: (String) -> Unit,
    onNavigateToQuiz: (String) -> Unit
) {
    val vocabs by viewModel.vocabs.collectAsState()
    val decks by viewModel.decks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val currentDeck = decks.find { it.id == deckId }

    // Đếm tiến độ học tập mượt mà
    val masteredCount = vocabs.count { it.level > 0 }
    val unmasteredCount = vocabs.count { it.level == 0 }

    LaunchedEffect(deckId) {
        viewModel.loadVocabs(deckId)
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { Text(currentDeck?.name ?: "Chi tiết", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = null, tint = TextDark) }
                },
                actions = {
                    IconButton(onClick = { onNavigateToAddVocab(deckId) }) { Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = PrimaryBlue) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            if (vocabs.isNotEmpty()) {
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.navigationBarsPadding()) {
                    if (vocabs.size >= 4) {
                        SmallFloatingActionButton(
                            onClick = { onNavigateToQuiz(deckId) }, containerColor = Color.White, contentColor = PrimaryBlue, shape = CircleShape
                        ) { Icon(Icons.Default.Quiz, contentDescription = "Trắc nghiệm") }
                        Spacer(Modifier.height(12.dp))
                    }
                    ExtendedFloatingActionButton(
                        onClick = { onNavigateToPractice(deckId) }, containerColor = PrimaryBlue, contentColor = Color.White, shape = RoundedCornerShape(16.dp),
                        icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                        text = { Text("Học Flashcard", fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(bottom = 120.dp)) {
            item { StatSection(masteredCount, unmasteredCount) }
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Danh sách từ vựng (${vocabs.size})", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                }
            }
            if (isLoading) {
                item { Box(Modifier.fillMaxWidth().padding(50.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryBlue) } }
            } else if (vocabs.isEmpty()) {
                item { EmptyVocabState { onNavigateToAddVocab(deckId) } }
            } else {
                items(vocabs, key = { it.id }) { vocab ->
                    VocabCompactCard(
                        vocab = vocab,
                        onEdit = { term, definition -> viewModel.updateVocab(deckId, vocab.id, term, definition) },
                        onDelete = { viewModel.deleteVocab(deckId, vocab.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun StatSection(mastered: Int, unmastered: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(24.dp), shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Tiến độ học tập", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem("Đã thuộc", mastered, TextGreen, Icons.Default.CheckCircle)
                StatItem("Chưa thuộc", unmastered, TextRed, Icons.Default.MenuBook)
            }
        }
    }
}

@Composable
fun StatItem(label: String, count: Int, color: Color, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Text(text = count.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Text(text = label, fontSize = 12.sp, color = TextLight)
    }
}

@Composable
fun VocabCompactCard(vocab: VocabItem, onEdit: (String, String) -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = vocab.kanji.ifEmpty { vocab.kana }, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text(text = vocab.meaning, fontSize = 14.sp, color = TextLight)
            }

            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = TextLight)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color.White)) {
                    DropdownMenuItem(
                        text = { Text("Chỉnh sửa") }, onClick = { expanded = false; showEditDialog = true },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = PrimaryBlue) }
                    )
                    DropdownMenuItem(
                        text = { Text("Xóa từ vựng", color = TextRed) }, onClick = { expanded = false; showDeleteConfirm = true },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = TextRed) }
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        EditVocabDialog(
            initialTerm = vocab.kanji.ifEmpty { vocab.kana },
            initialDefinition = vocab.meaning,
            onDismiss = { showEditDialog = false },
            onConfirm = { term, def -> onEdit(term, def); showEditDialog = false }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa từ vựng này không?") },
            confirmButton = { TextButton(onClick = { onDelete(); showDeleteConfirm = false }) { Text("Xóa", color = TextRed) } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Hủy", color = TextDark) } },
            containerColor = Color.White
        )
    }
}

@Composable
fun EditVocabDialog(initialTerm: String, initialDefinition: String, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var term by remember { mutableStateOf(initialTerm) }
    var definition by remember { mutableStateOf(initialDefinition) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Chỉnh sửa Flashcard", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = term, onValueChange = { term = it }, label = { Text("Thuật ngữ") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = definition, onValueChange = { definition = it }, label = { Text("Định nghĩa") },
                    modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue)
                )
                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Hủy", color = TextLight) }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { if (term.isNotBlank() && definition.isNotBlank()) onConfirm(term, definition) },
                        shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) { Text("Lưu") }
                }
            }
        }
    }
}

@Composable
fun EmptyVocabState(onAdd: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(64.dp), tint = DividerColor)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Thư mục còn trống rỗng", color = TextLight, fontWeight = FontWeight.Medium)
        TextButton(onClick = onAdd) { Text("Thêm từ vựng đầu tiên ngay", color = PrimaryBlue) }
    }
}