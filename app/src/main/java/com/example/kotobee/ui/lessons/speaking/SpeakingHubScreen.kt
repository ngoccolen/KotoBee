package com.example.kotobee.ui.lessons.speaking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotobee.ui.home.CardBorderColor
import com.example.kotobee.ui.home.ProgressPrimary
import com.example.kotobee.ui.home.TextDark
import com.example.kotobee.ui.home.TextGray
import com.example.kotobee.ui.home.ThemeBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakingHubScreen(
    onShadowingClick: () -> Unit,
    onConversationClick: () -> Unit,
    onPairConversationClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Luyện nói", fontWeight = FontWeight.Bold, color = TextDark) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ThemeBackground)
            )
        },
        containerColor = ThemeBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SpeakingModeCard(
                title = "Shadowing",
                subtitle = "Nghe mẫu, ghi âm và nhận xét phát âm theo từng câu.",
                icon = Icons.Default.RecordVoiceOver,
                tint = ProgressPrimary,
                onClick = onShadowingClick
            )
            SpeakingModeCard(
                title = "Luyện giao tiếp",
                subtitle = "Nói theo lượt với AI, lưu lịch sử và nghe phản hồi tiếng Nhật.",
                icon = Icons.Default.ChatBubbleOutline,
                tint = Color(0xFF1E88E5),
                onClick = onConversationClick
            )
            SpeakingModeCard(
                title = "Giao tiếp 2 người",
                subtitle = "Tạo phòng riêng, nói luân phiên bằng ghi âm và lưu transcript.",
                icon = Icons.Default.Groups,
                tint = Color(0xFF2E7D32),
                onClick = onPairConversationClick
            )
        }
    }
}

@Composable
private fun SpeakingModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .size(52.dp)
                    .background(Color.White, RoundedCornerShape(14.dp))
                    .border(1.dp, CardBorderColor, RoundedCornerShape(14.dp)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextDark, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, color = TextGray, fontSize = 13.sp, lineHeight = 18.sp)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextGray)
        }
    }
}
