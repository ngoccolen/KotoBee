package com.example.kotobee.ui.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kotobee.R
import com.example.kotobee.ui.auth.AuthState
import java.util.Calendar

// Đã cập nhật bảng màu đồng bộ với HomeScreen
object ColorPalette {
    val Background = Color(0xFFFFFDFD)
    val CardBackground = Color.White
    val Primary = Color(0xFFE53935) // Đỏ sậm chủ đạo
    val PrimaryLight = Color(0xFFFFEBEE) // Hồng siêu nhạt cho nền icon/track
    val Border = Color(0xFFFFCDD2) // Viền card hồng nhạt
    val Text = Color(0xFF333333)
    val TextSub = Color(0xFF757575)
}

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val profileState by viewModel.profileState.collectAsState()
    val activityData by viewModel.activityData.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val context = LocalContext.current

    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    LaunchedEffect(updateState) {
        when (updateState) {
            is AuthState.Success -> {
                Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                showEditDialog = false
                viewModel.resetUpdateState()
            }
            is AuthState.Error -> {
                val error = (updateState as AuthState.Error).message
                Toast.makeText(context, "Lỗi: $error", Toast.LENGTH_SHORT).show()
                viewModel.resetUpdateState()
            }
            else -> {}
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            currentName = profileState.username,
            currentLevel = profileState.jlptLevel,
            currentAvatarUrl = profileState.avatarUrl,
            isLoading = updateState is AuthState.Loading,
            onDismiss = { showEditDialog = false },
            onSave = { newName, newLevel, newUri ->
                viewModel.updateProfile(newName, newLevel, newUri)
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPalette.Background),
        contentPadding = PaddingValues(20.dp), // Đổi thành 20dp cho giống Home
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item { ProfileHeader(profileState) }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item { QuickStatsRow(profileState) }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { RankCard(profileState) }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item { ActivityChartCard(activityData) }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item { BadgesCard() }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item {
            SettingsList(
                navController = navController,
                viewModel = viewModel,
                onEditProfileClick = { showEditDialog = true }
            )
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    currentName: String,
    currentLevel: String,
    currentAvatarUrl: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, Uri?) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var level by remember { mutableStateOf(currentLevel) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val levels = listOf("N5", "N4", "N3", "N2", "N1")
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) selectedImageUri = uri }
    )

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        modifier = Modifier.fillMaxWidth(),
        title = { Text(text = "Chỉnh sửa hồ sơ", fontWeight = FontWeight.Bold, color = ColorPalette.Text) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clickable(enabled = !isLoading) {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(selectedImageUri ?: currentAvatarUrl.ifEmpty { R.drawable.jp_vocabulary })
                            .crossfade(true)
                            .build(),
                        contentDescription = "Edit Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(2.dp, ColorPalette.Primary, CircleShape)
                    )
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoCamera,
                            contentDescription = "Change Photo",
                            tint = Color.White,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên hiển thị") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorPalette.Primary,
                        focusedLabelColor = ColorPalette.Primary
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Mục tiêu JLPT:",
                    fontSize = 14.sp,
                    color = ColorPalette.TextSub,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(levels) { item ->
                        FilterChip(
                            selected = level == item,
                            onClick = { level = item },
                            label = { Text(item) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ColorPalette.Primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, level, selectedImageUri) },
                enabled = !isLoading && name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ColorPalette.Primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Lưu", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Hủy", color = ColorPalette.TextSub)
            }
        }
    )
}

@Composable
fun ProfileHeader(state: ProfileState) {
    val context = LocalContext.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(state.avatarUrl.ifEmpty { R.drawable.jp_vocabulary })
                    .crossfade(true)
                    .build(),
                contentDescription = "User Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .border(4.dp, ColorPalette.Background, CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .border(3.dp, ColorPalette.Primary, CircleShape)
            )
            Surface(
                color = ColorPalette.Primary,
                shape = RoundedCornerShape(50),
                border = BorderStroke(2.dp, Color.White),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = state.jlptLevel,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Hồ sơ cá nhân", fontSize = 16.sp, color = ColorPalette.TextSub)
        Text(
            text = state.username.ifEmpty { "Người học" },
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = ColorPalette.Text
        )
        Text(text = state.email, fontSize = 14.sp, color = ColorPalette.Primary)
    }
}

@Composable
fun QuickStatsRow(state: ProfileState) {
    Row(modifier = Modifier.fillMaxWidth()) {
        StatCard(
            icon = Icons.Filled.MenuBook,
            value = state.learnedVocab.toString(),
            description = "Từ vựng đã thuộc",
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        StatCard(
            icon = Icons.Filled.LocalFireDepartment,
            value = state.streak.toString(),
            description = "Ngày học liên tiếp",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(icon: ImageVector, value: String, description: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ColorPalette.Border),
        colors = CardDefaults.cardColors(containerColor = ColorPalette.CardBackground),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = ColorPalette.Primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = ColorPalette.Text)
            Text(text = description, fontSize = 12.sp, color = ColorPalette.TextSub)
        }
    }
}

@Composable
fun RankCard(state: ProfileState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ColorPalette.Border),
        colors = CardDefaults.cardColors(containerColor = ColorPalette.CardBackground),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "XẾP HẠNG",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorPalette.TextSub
                )
                Text(
                    text = state.rankInfo,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ColorPalette.Primary
                )
                Text(
                    text = "Bạn đang dẫn đầu nhóm!",
                    fontSize = 14.sp,
                    color = ColorPalette.Text
                )
            }
            Icon(
                Icons.Filled.EmojiEvents,
                contentDescription = null,
                tint = ColorPalette.Primary,
                modifier = Modifier.size(60.dp)
            )
        }
    }
}

@Composable
fun ActivityChartCard(activityData: List<ActivityDay>) {
    val currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    val todayIndex = if (currentDayOfWeek == Calendar.SUNDAY) 6 else currentDayOfWeek - 2

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ColorPalette.Border),
        colors = CardDefaults.cardColors(containerColor = ColorPalette.CardBackground),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hoạt động tuần này",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorPalette.Text
                )
                Text(text = "Chi tiết", fontSize = 14.sp, color = ColorPalette.Primary)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                activityData.forEachIndexed { index, data ->
                    ActivityColumn(data, isHighlighted = index == todayIndex)
                }
            }
        }
    }
}

@Composable
fun ActivityColumn(data: ActivityDay, isHighlighted: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val barColor = if (isHighlighted) ColorPalette.Primary else ColorPalette.Primary.copy(alpha = 0.2f)
        val barHeight = (data.value.toFloat() / 100 * 80).dp
        Box(
            modifier = Modifier
                .width(20.dp)
                .height(barHeight)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(barColor)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = data.day,
            fontSize = 10.sp,
            color = if (isHighlighted) ColorPalette.Text else ColorPalette.TextSub,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
        )
    }
}

data class Badge(val name: String, val icon: ImageVector, val color: Color)

@Composable
fun BadgesCard() {
    val badges = listOf(
        Badge("Vua Từ Vựng", Icons.Filled.MenuBook, Color(0xFFFFB74D)),
        Badge("Chăm Chỉ", Icons.Filled.CheckCircle, ColorPalette.Primary),
        Badge("Sáng Tạo", Icons.Filled.AutoAwesome, Color(0xFF81D4FA))
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ColorPalette.Border),
        colors = CardDefaults.cardColors(containerColor = ColorPalette.CardBackground),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Bộ sưu tập Huy hiệu",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ColorPalette.Text
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(badges) { badge ->
                    BadgeItem(badge)
                }
            }
        }
    }
}

@Composable
fun BadgeItem(badge: Badge) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(90.dp)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(badge.color)
        ) {
            Icon(
                badge.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = badge.name,
            fontSize = 12.sp,
            color = ColorPalette.Text,
            fontWeight = FontWeight.Medium
        )
    }
}

data class SettingItem(val name: String, val icon: ImageVector, val action: () -> Unit)

@Composable
fun SettingsList(
    navController: NavController,
    viewModel: ProfileViewModel,
    onEditProfileClick: () -> Unit
) {
    val settingItems = listOf(
        SettingItem("Cập nhật hồ sơ", Icons.Filled.PersonOutline) {
            onEditProfileClick()
        },
        SettingItem("Ghi chú & Lịch trình", Icons.Filled.EditNote) {
        },
        SettingItem("Bộ sưu tập Huy hiệu", Icons.Filled.AutoAwesome) {
        },
        SettingItem("Gói Plus & Thanh toán", Icons.Filled.AddCircleOutline) {
        },
        SettingItem("Đăng xuất", Icons.Filled.ExitToApp) {
            viewModel.signOut {
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            }
        }
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ColorPalette.Border),
        colors = CardDefaults.cardColors(containerColor = ColorPalette.CardBackground),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            settingItems.forEach { item ->
                SettingItemRow(item)
            }
        }
    }
}

@Composable
fun SettingItemRow(item: SettingItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.action() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = if (item.name == "Đăng xuất") Color(0xFFFFEBEE) else ColorPalette.PrimaryLight,
            shape = CircleShape,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    item.icon,
                    contentDescription = null,
                    tint = if (item.name == "Đăng xuất") Color.Red else ColorPalette.Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.name,
            fontSize = 16.sp,
            color = if (item.name == "Đăng xuất") Color.Red else ColorPalette.Text,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = Color(0xFFDDDDDD),
            modifier = Modifier.size(16.dp)
        )
    }
}