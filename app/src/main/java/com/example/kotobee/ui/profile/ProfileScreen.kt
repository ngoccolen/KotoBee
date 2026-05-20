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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kotobee.R
import com.example.kotobee.di.AppContainer
import com.example.kotobee.ui.auth.AuthState

object ColorPalette {
    val Background = Color.White
    val CardBackground = Color.White
    val Primary = Color(0xFFE53935)
    val Border = Color(0xFFE53935)
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
        item { ProfileHeader(profileState) }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item { QuickStatsRow(profileState) }
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
fun LearningSummaryCard(state: ProfileState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ColorPalette.Border),
        colors = CardDefaults.cardColors(containerColor = ColorPalette.CardBackground),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Tổng quan học tập",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ColorPalette.Text
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
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
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
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
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
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
                border = BorderStroke(1.dp, ColorPalette.Primary),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = ColorPalette.Primary, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = value,
                    color = ColorPalette.Text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = label, color = ColorPalette.TextSub, fontSize = 11.sp, lineHeight = 14.sp)
            }
        }
    }
}

@Composable
fun StatCard(icon: ImageVector, value: String, description: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.heightIn(min = 128.dp),
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
            Text(
                text = description,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = ColorPalette.TextSub,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun StreakOverviewCard(state: ProfileState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ColorPalette.Border),
        colors = CardDefaults.cardColors(containerColor = ColorPalette.CardBackground),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.White,
                shape = CircleShape,
                border = BorderStroke(1.dp, ColorPalette.Primary),
                modifier = Modifier.size(58.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = ColorPalette.Primary, modifier = Modifier.size(32.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Streak hiện tại", color = ColorPalette.TextSub, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("${state.streak} ngày", color = ColorPalette.Text, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                Text("Học flashcard, làm nhiệm vụ hoặc luyện kanji để giữ chuỗi.", color = ColorPalette.TextSub, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ActivityChartCard(activityData: List<ActivityDay>) {
    val weeklyData = remember(activityData) { buildWeeklyActivity(activityData) }
    val maxWeeklyPoints = weeklyData.maxOfOrNull { it.value }?.coerceAtLeast(1) ?: 1
    val currentWeekPoints = weeklyData.lastOrNull()?.value ?: 0

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
                    text = "Bảng theo dõi học tập",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorPalette.Text
                )
                Text(text = "12 tuần", fontSize = 14.sp, color = ColorPalette.Primary)
            }
            Text(
                "Mỗi cột là tổng điểm học của một tuần.",
                color = ColorPalette.TextSub,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ColorPalette.Border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tuần này", color = ColorPalette.TextSub, fontSize = 12.sp)
                    Text(
                        "$currentWeekPoints điểm",
                        color = ColorPalette.Primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyData.forEach { week ->
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
                            val fraction = week.value.toFloat() / maxWeeklyPoints.toFloat()
                            val barHeight = if (week.value > 0) {
                                (108.dp * fraction).coerceAtLeast(8.dp)
                            } else {
                                3.dp
                            }
                            Box(
                                modifier = Modifier
                                    .width(16.dp)
                                    .height(barHeight)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .background(
                                        if (week.isCurrent) ColorPalette.Primary else ColorPalette.Primary.copy(alpha = 0.28f)
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = week.label,
                            color = if (week.isCurrent) ColorPalette.Primary else ColorPalette.TextSub,
                            fontSize = 10.sp,
                            fontWeight = if (week.isCurrent) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

private data class ActivityWeek(
    val label: String,
    val value: Int,
    val isCurrent: Boolean
)

private fun buildWeeklyActivity(activityData: List<ActivityDay>): List<ActivityWeek> {
    val weeks = activityData.takeLast(84).chunked(7).takeLast(12)
    return weeks.mapIndexed { index, days ->
        ActivityWeek(
            label = if (index == weeks.lastIndex) "Nay" else "T-${weeks.lastIndex - index}",
            value = days.sumOf { it.value },
            isCurrent = index == weeks.lastIndex
        )
    }.ifEmpty {
        List(12) { index ->
            ActivityWeek(
                label = if (index == 11) "Nay" else "T-${11 - index}",
                value = 0,
                isCurrent = index == 11
            )
        }
    }
}

@Composable
fun RecentActivityCard(activities: List<RecentActivity>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ColorPalette.Border),
        colors = CardDefaults.cardColors(containerColor = ColorPalette.CardBackground),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Hoạt động gần đây",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ColorPalette.Text
            )
            Spacer(modifier = Modifier.height(8.dp))
            activities.forEachIndexed { index, activity ->
                RecentActivityRow(activity)
                if (index < activities.lastIndex) {
                    Divider(color = ColorPalette.Border.copy(alpha = 0.18f), thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
private fun RecentActivityRow(activity: RecentActivity) {
    val icon = when (activity.type) {
        "streak" -> Icons.Filled.LocalFireDepartment
        "task" -> Icons.Filled.CheckCircle
        "study" -> Icons.Filled.MenuBook
        else -> Icons.Filled.Info
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = Color.White,
            shape = CircleShape,
            border = BorderStroke(1.dp, ColorPalette.Primary),
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = ColorPalette.Primary, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.title,
                color = ColorPalette.Text,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = activity.subtitle,
                color = ColorPalette.TextSub,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (activity.meta.isNotBlank()) {
            Text(
                text = activity.meta,
                color = ColorPalette.Primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
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
            color = Color.White,
            shape = CircleShape,
            border = BorderStroke(1.dp, if (item.name == "Đăng xuất") Color.Red else ColorPalette.Primary),
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
