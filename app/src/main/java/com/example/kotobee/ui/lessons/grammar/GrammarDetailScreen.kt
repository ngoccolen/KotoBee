package com.example.kotobee.ui.lessons.grammar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun GrammarDetailScreen(
    navController: NavController,
    viewModel: GrammarViewModel
) {
    // Thu thập dữ liệu từ viewModel
    val grammar by viewModel.currentGrammar.collectAsState()

    // Giao diện khi có dữ liệu (Trong thực tế bạn sẽ truyền ID qua Route và gọi loadGrammarDetail)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = JapaneseIndigo, modifier = Modifier.clickable { navController.popBackStack() })
            Text("Nihongo Flow", color = JapaneseIndigo, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Icon(Icons.Default.BookmarkBorder, contentDescription = "Save", tint = Color.Gray)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Text("JLPT N4 GRAMMAR", color = JapaneseIndigo, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(Color(0xFFE8EAF6), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(grammar?.title ?: "～ことになっている", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
            Text("Dùng để nói về thói quen tự mình quyết định.", color = Color.Gray, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))

            Spacer(modifier = Modifier.height(24.dp))

            // Box Ý nghĩa & Cấu trúc
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = JapaneseIndigo, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("MEANING", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(grammar?.meaning ?: "Cố gắng làm / quyết định duy trì một thói quen nào đó một cách có ý thức.", modifier = Modifier.padding(top = 8.dp))

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFFD81B60), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("FORMATION", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)).padding(12.dp)) {
                        Text(grammar?.formation ?: "V-る + ことにしている\nV-ない + ことにしている", style = androidx.compose.ui.text.TextStyle(lineHeight = 24.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Ví dụ minh họa", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // Ví dụ
            ExampleCard(
                jp = "健康のために、毎日野菜を食べることにしています。",
                romaji = "Kenko no tame ni, mainichi yasai o taberu koto ni shite imasu.",
                vi = "Vì sức khỏe, tôi (quyết định) ăn rau mỗi ngày."
            )
        }

        // Nút Luyện tập dính ở dưới cùng
        Surface(shadowElevation = 8.dp, color = Color.White) {
            Button(
                onClick = { /* Chuyển sang màn hình Quiz */ },
                colors = ButtonDefaults.buttonColors(containerColor = JapaneseIndigo),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Luyện tập ngay", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ExampleCard(jp: String, romaji: String, vi: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(jp, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Icon(Icons.Default.VolumeUp, contentDescription = "Nghe", tint = Color.Gray)
            }
            Text(romaji, color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
            Text(vi, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 8.dp)) // Xanh lá cây
        }
    }
}