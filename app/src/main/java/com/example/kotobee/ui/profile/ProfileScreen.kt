package com.example.kotobee.ui.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kotobee.R
import com.example.kotobee.di.AppContainer
import com.example.kotobee.ui.auth.AuthState
import com.example.kotobee.data.model.Badge
import androidx.compose.ui.graphics.Brush

object ColorPalette {
    val Background = Color(0xFFFAFAFA)
    val CardBackground = Color.White
    val Primary = Color(0xFFE53935)
    val Border = Color(0xFFEEEEEE)
    val BorderAccent = Color(0xFFFFD5D5)
    val MutedSurface = Color(0xFFF8FAFC)
    val Text = Color(0xFF333333)
    val TextSub = Color(0xFF757575)
}

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val appContainer = remember { AppContainer(context.applicationContext) }
    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.Factory(appContainer.cloudinaryService)
    )
    val profileState by viewModel.profileState.collectAsState()
    val activityData by viewModel.activityData.collectAsState()
    val recentActivities by viewModel.recentActivities.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val badges by viewModel.badges.collectAsState()

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
        contentPadding = PaddingValues(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item { ProfileHeader(profileState, onEditClick = { showEditDialog = true }) }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item { QuickStatsRow(profileState) }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { BadgesCard(badges = badges) }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { LearningSummaryCard(profileState) }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { StreakOverviewCard(profileState) }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { ActivityChartCard(activityData) }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { RecentActivityCard(recentActivities) }
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
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ColorPalette.Border, RoundedCornerShape(28.dp)),
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp),
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
                    colors = profileTextFieldColors()
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
                                containerColor = Color.White,
                                labelColor = ColorPalette.Text,
                                selectedContainerColor = ColorPalette.Primary,
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = ColorPalette.Border,
                                selectedBorderColor = ColorPalette.Primary,
                                enabled = true,
                                selected = level == item
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
private fun profileTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    disabledContainerColor = Color.White,
    errorContainerColor = Color.White,
    focusedBorderColor = ColorPalette.Primary,
    unfocusedBorderColor = ColorPalette.Border,
    focusedLabelColor = ColorPalette.Primary,
    unfocusedLabelColor = ColorPalette.TextSub,
    cursorColor = ColorPalette.Primary
)

@Composable
fun ProfileHeader(state: ProfileState, onEditClick: () -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFEF5350), Color(0xFFC62828))
                    )
                )
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable { onEditClick() }
                    .align(Alignment.TopEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Chỉnh sửa hồ sơ",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(130.dp)) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(3.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    )
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(state.avatarUrl.ifEmpty { R.drawable.jp_vocabulary })
                            .crossfade(true)
                            .build(),
                        contentDescription = "User Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(106.dp)
                            .clip(CircleShape)
                            .border(3.dp, Color.White, CircleShape)
                    )
                    Surface(
                        color = ColorPalette.Primary,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(2.dp, Color.White),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 4.dp)
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

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.username.ifEmpty { "Người học" },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = state.email,
                    fontSize = 14.sp,
                    color = Color(0xFFFFCDD2),
                    fontWeight = FontWeight.Medium
                )
            }
        }
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
fun LearningSummaryCard(state: ProfileState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, ColorPalette.Border),
        colors = CardDefaults.cardColors(containerColor = ColorPalette.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Tổng quan học tập",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ColorPalette.Text
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryMetric(
                    icon = Icons.Filled.Star,
                    value = state.totalStudyPoints.toString(),
                    label = "Điểm học",
                    modifier = Modifier.weight(1f)
                )
                SummaryMetric(
                    icon = Icons.Filled.DateRange,
                    value = state.activeDays.toString(),
                    label = "Ngày hoạt động",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryMetric(
                    icon = Icons.Filled.CheckCircle,
                    value = "${state.completedTasks}/${state.totalTasks}",
                    label = "Nhiệm vụ",
                    modifier = Modifier.weight(1f)
                )
                SummaryMetric(
                    icon = Icons.Filled.History,
                    value = state.lastActivityLabel,
                    label = "Gần nhất",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(min = 82.dp),
        color = ColorPalette.MutedSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ColorPalette.Border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.White,
                shape = CircleShape,
                border = BorderStroke(1.dp, ColorPalette.BorderAccent),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = ColorPalette.Primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = value,
                    color = ColorPalette.Text,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = label,
                    color = ColorPalette.TextSub,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun StatCard(icon: ImageVector, value: String, description: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.heightIn(min = 128.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFFFF5F5), Color(0xFFFFEBEE))
                    )
                )
                .border(1.dp, ColorPalette.BorderAccent, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Surface(
                    color = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp),
                    shadowElevation = 1.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = ColorPalette.Primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = value,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ColorPalette.Text
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = ColorPalette.TextSub,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun StreakOverviewCard(state: ProfileState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFFFF3E0), Color(0xFFFFEBEE))
                    )
                )
                .border(1.dp, Color(0xFFFFE0B2), RoundedCornerShape(22.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.White,
                    shape = CircleShape,
                    border = BorderStroke(1.5.dp, Color(0xFFFF9800)),
                    modifier = Modifier.size(58.dp),
                    shadowElevation = 1.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Streak hiện tại",
                        color = Color(0xFFE65100),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${state.streak} ngày",
                        color = ColorPalette.Text,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Học flashcard, làm nhiệm vụ hoặc luyện kanji để giữ chuỗi.",
                        color = ColorPalette.TextSub,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityChartCard(activityData: List<ActivityDay>) {
    val dailyData = remember(activityData) {
        activityData.takeIf { it.size == 7 } ?: buildCurrentWeekActivity()
    }
    val maxDailyPoints = dailyData.maxOfOrNull { it.value }?.coerceAtLeast(1) ?: 1
    val currentWeekPoints = dailyData.sumOf { it.value }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, ColorPalette.Border),
        colors = CardDefaults.cardColors(containerColor = ColorPalette.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bảng theo dõi học tập",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ColorPalette.Text
                )
                Text(
                    text = "Tuần này",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorPalette.Primary
                )
            }
            Text(
                text = "Mỗi cột là điểm học trong một ngày, từ T2 đến CN.",
                color = ColorPalette.TextSub,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = ColorPalette.MutedSurface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ColorPalette.Border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tổng tuần này",
                        color = ColorPalette.TextSub,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$currentWeekPoints điểm",
                        color = ColorPalette.Primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                dailyData.forEach { day ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            val fraction = day.value.toFloat() / maxDailyPoints.toFloat()
                            val barHeight = if (day.value > 0) {
                                (108.dp * fraction).coerceAtLeast(8.dp)
                            } else {
                                3.dp
                            }
                            val barBrush = if (day.value > 0) {
                                if (day.isToday) {
                                    Brush.verticalGradient(listOf(Color(0xFFEF5350), Color(0xFFD32F2F)))
                                } else {
                                    Brush.verticalGradient(listOf(Color(0xFFFFCDD2), Color(0xFFFFEBEE)))
                                }
                            } else {
                                Brush.verticalGradient(listOf(Color(0xFFF5F5F5), Color(0xFFEEEEEE)))
                            }
                            Box(
                                modifier = Modifier
                                    .width(16.dp)
                                    .height(barHeight)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .background(barBrush)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = day.day,
                            color = if (day.isToday) ColorPalette.Primary else ColorPalette.TextSub,
                            fontSize = 10.sp,
                            fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecentActivityCard(activities: List<RecentActivity>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, ColorPalette.Border),
        colors = CardDefaults.cardColors(containerColor = ColorPalette.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Hoạt động gần đây",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ColorPalette.Text
            )
            Spacer(modifier = Modifier.height(8.dp))
            activities.forEachIndexed { index, activity ->
                RecentActivityRow(activity)
                if (index < activities.lastIndex) {
                    Divider(color = ColorPalette.Border, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
private fun RecentActivityRow(activity: RecentActivity) {
    val (icon, circleBg, accentColor) = when (activity.type) {
        "streak" -> Triple(Icons.Filled.LocalFireDepartment, Color(0xFFFFF3E0), Color(0xFFFF9800))
        "task" -> Triple(Icons.Filled.CheckCircle, Color(0xFFE8F5E9), Color(0xFF4CAF50))
        "study" -> Triple(Icons.Filled.MenuBook, Color(0xFFE3F2FD), Color(0xFF2196F3))
        else -> Triple(Icons.Filled.Info, Color(0xFFECEFF1), Color(0xFF607D8B))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = circleBg,
            shape = CircleShape,
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.title,
                color = ColorPalette.Text,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = activity.subtitle,
                color = ColorPalette.TextSub,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (activity.meta.isNotBlank()) {
            Text(
                text = activity.meta,
                color = accentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
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
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, ColorPalette.Border),
        colors = CardDefaults.cardColors(containerColor = ColorPalette.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
    val isSignOut = item.name == "Đăng xuất"
    val circleBg = if (isSignOut) Color(0xFFFFEBEE) else Color(0xFFFFF5F5)
    val accentColor = if (isSignOut) Color(0xFFD32F2F) else ColorPalette.Primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.action() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = circleBg,
            shape = CircleShape,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    item.icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.name,
            fontSize = 16.sp,
            color = accentColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = Color(0xFFCCCCCC),
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
fun BadgesCard(badges: List<Badge>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ColorPalette.CardBackground),
        border = BorderStroke(1.dp, ColorPalette.Border.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "🏆 Huy hiệu học tập",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ColorPalette.Text
                )
                if (badges.isNotEmpty()) {
                    Text(
                        text = "${badges.size} đã đạt",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorPalette.TextSub
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (badges.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ColorPalette.MutedSurface)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🏵️ Chưa có huy hiệu nào",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorPalette.Text
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Hoàn thành mục tiêu học tập để nhận huy hiệu đầu tiên!",
                            fontSize = 12.sp,
                            color = ColorPalette.TextSub,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(badges) { badge ->
                        BadgeItem(badge = badge)
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeItem(badge: Badge) {
    val frameColor = when (badge.iconName) {
        "first_goal" -> Color(0xFFCD7F32) // Bronze
        "rising_star" -> Color(0xFFC0C0C0) // Silver
        "champion" -> Color(0xFFFFD700) // Gold
        "master" -> Color(0xFF00ADEE) // Platinum
        "legend" -> Color(0xFFE53935) // Ruby/KotoBee Red
        else -> Color(0xFFFFD700)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(88.dp)
            .padding(vertical = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(68.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            frameColor.copy(alpha = 0.15f),
                            frameColor.copy(alpha = 0.05f)
                        )
                    ),
                    CircleShape
                )
                .border(2.dp, frameColor, CircleShape)
        ) {
            Icon(
                imageVector = com.example.kotobee.ui.home.getBadgeIcon(badge.iconName),
                contentDescription = badge.name,
                tint = frameColor,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = badge.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = ColorPalette.Text,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = badge.goalTitle.ifEmpty { "Mục tiêu" },
            fontSize = 10.sp,
            color = ColorPalette.TextSub,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
