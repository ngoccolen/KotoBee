package com.example.kotobee.ui.community

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    viewModel: CreatePostViewModel,
    onBack: () -> Unit
) {
    // --- NẾU ĐĂNG THÀNH CÔNG: Hiển thị màn hình xác nhận + Nút Quay lại ---
    if (viewModel.isSuccess) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ThemeBackground)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = "Thành công", tint = ProgressPrimary, modifier = Modifier.size(100.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Đăng bài thành công!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    viewModel.resetSuccessState()
                    onBack() // Quay lại trang cộng đồng
                },
                colors = ButtonDefaults.buttonColors(containerColor = ProgressPrimary),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Quay lại trang Cộng đồng", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        return // Dừng vẽ giao diện soạn thảo
    }

    // --- GIAO DIỆN SOẠN THẢO BÌNH THƯỜNG ---
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.selectedImageUri = uri
    }

    val canPost = !viewModel.isUploading && (viewModel.content.isNotBlank() || viewModel.selectedImageUri != null)

    Scaffold(
        containerColor = ThemeBackground,
        topBar = {
            TopAppBar(
                title = { Text("Đăng bài", color = TextDark, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại", tint = TextDark)
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.uploadPost() },
                        enabled = canPost
                    ) {
                        Text("ĐĂNG", color = if (canPost) ProgressPrimary else TextGray, fontWeight = FontWeight.ExtraBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ThemeBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(CardBorderColor, CircleShape))
                Spacer(modifier = Modifier.width(12.dp))
                Text(viewModel.currentUserName, color = TextDark, fontWeight = FontWeight.Bold)
            }

            OutlinedTextField(
                value = viewModel.content,
                onValueChange = {
                    viewModel.content = it
                    viewModel.clearError()
                },
                placeholder = { Text("Hôm nay bạn học được gì mới?", color = TextGray) },
                modifier = Modifier.fillMaxWidth().weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = TextDark,
                    unfocusedTextColor = TextDark,
                    cursorColor = ProgressPrimary,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            viewModel.selectedImageUri?.let { uri ->
                Box(modifier = Modifier.fillMaxWidth().height(200.dp).padding(vertical = 8.dp)) {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = {
                            viewModel.selectedImageUri = null
                            viewModel.clearError()
                        },
                        modifier = Modifier.align(Alignment.TopEnd).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, "Xóa ảnh", tint = Color.White)
                    }
                }
            }

            viewModel.errorMessage?.let { message ->
                Text(text = message, color = Color(0xFFFF6B6B), modifier = Modifier.padding(bottom = 8.dp))
            }

            if (viewModel.isUploading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    color = ProgressPrimary,
                    trackColor = CardBorderColor
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                IconButton(
                    onClick = {
                        viewModel.clearError()
                        imagePickerLauncher.launch("image/*")
                    }
                ) {
                    Icon(Icons.Default.PhotoLibrary, "Chọn ảnh", tint = ProgressPrimary)
                }
            }
        }
    }
}