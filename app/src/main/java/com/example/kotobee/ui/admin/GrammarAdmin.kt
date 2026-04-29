package com.example.kotobee.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotobee.data.model.Grammar
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGrammarScreen() {
    var title by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("N4") }
    var meaning by remember { mutableStateOf("") }
    var formation by remember { mutableStateOf("") }
    var usageNote by remember { mutableStateOf("") }

    var isSaving by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf("") }

    val db = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Thêm Bài Học Ngữ Pháp", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Mẫu ngữ pháp (Ví dụ: ~ことになっている)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = level,
            onValueChange = { level = it },
            label = { Text("Cấp độ (N5, N4, N3...)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = meaning,
            onValueChange = { meaning = it },
            label = { Text("Ý nghĩa") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = formation,
            onValueChange = { formation = it },
            label = { Text("Cấu trúc (Xuống dòng cho nhiều cấu trúc)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = usageNote,
            onValueChange = { usageNote = it },
            label = { Text("Cách dùng / Lưu ý") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        // (Phần nhập Example bạn có thể làm thêm các nút (+) để add linh hoạt)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                isSaving = true
                // Tạo ID ngẫu nhiên cho Document
                val newId = UUID.randomUUID().toString()
                val newLesson = Grammar(
                    id = newId,
                    level = level,
                    title = title,
                    meaning = meaning,
                    formation = formation,
                    usageNote = usageNote,
                    examples = emptyList() // Bạn code thêm phần nhập List nhé
                )

                db.collection("grammar_lessons").document(newId).set(newLesson)
                    .addOnSuccessListener {
                        isSaving = false
                        saveMessage = "Lưu thành công!"
                        // Reset form
                        title = ""; meaning = ""; formation = ""; usageNote = ""
                    }
                    .addOnFailureListener {
                        isSaving = false
                        saveMessage = "Lỗi: ${it.message}"
                    }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isSaving
        ) {
            Text(if (isSaving) "Đang lưu..." else "LƯU BÀI HỌC")
        }

        if (saveMessage.isNotEmpty()) {
            Text(saveMessage, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 16.dp))
        }
    }
}