package com.example.kotobee.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kotobee.R
import com.example.kotobee.di.AppContainer
import com.example.kotobee.ui.community.CommunityScreen
import com.example.kotobee.ui.community.CommunityViewModel
import com.example.kotobee.ui.lessons.LearningScreen
import com.example.kotobee.ui.profile.ProfileScreen

val ThemeBackground = Color(0xFFFAFAFA)
val CardBorderColor = Color(0xFFE0E0E0)
val ProgressPrimary = Color(0xFFD32F2F)
val AccentRed = Color(0xFFE53935)
val ProgressTrack = Color(0xFFF7F7F7)
val TextDark = Color(0xFF333333)
val TextGray = Color(0xFF757575)
val SoftPink = Color(0xFFFFF5F5)
val SoftBorder = Color(0xFFEEEEEE)

private data class BottomDestination(
    val label: String,
    val icon: ImageVector
)

@Composable
fun MainScreen(navController: NavController) {
    var selectedItem by rememberSaveable { mutableStateOf(0) }
    val context = LocalContext.current
    val appContainer = remember { AppContainer(context.applicationContext) }
    val communityViewModel: CommunityViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory = CommunityViewModel.Factory(repository = appContainer.communityRepository)
        )
    val items = listOf(
        BottomDestination("Trang chủ", Icons.Default.Home),
        BottomDestination("Học tập", Icons.Default.MenuBook),
        BottomDestination("Cộng đồng", Icons.Default.Groups),
        BottomDestination("Hồ sơ", Icons.Default.Person)
    )
    val openLearningTab: () -> Unit = { selectedItem = 1 }
    val openVocabLibrary: () -> Unit = {
        selectedItem = 1
        navController.navigate("deck_list")
    }

    Scaffold(
        bottomBar = {
            Column {
                HorizontalDivider(color = AccentRed, thickness = 1.dp)
                NavigationBar(containerColor = ThemeBackground, tonalElevation = 0.dp) {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = selectedItem == index,
                            onClick = { selectedItem = index },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = if (selectedItem == index) ProgressPrimary else Color.Gray
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedItem == index) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AccentRed,
                                selectedTextColor = AccentRed,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        when (selectedItem) {
            0 -> HomeScreen(
                navController = navController,
                modifier = Modifier.padding(padding),
                onOpenLearning = openLearningTab,
                onOpenVocabLibrary = openVocabLibrary
            )
            1 -> LearningScreen(
                navController = navController,
                modifier = Modifier.padding(padding),
                onNavigateToLesson = { route ->
                    selectedItem = 1
                    navController.navigate(route)
                }
            )
            2 -> Box(modifier = Modifier.padding(padding)) {
                CommunityScreen(
                    viewModel = communityViewModel,
                    onCreatePostClick = { navController.navigate("create_post") }
                )
            }
            3 -> Box(modifier = Modifier.padding(padding)) {
                ProfileScreen(navController = navController)
            }
        }
    }
}

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    modifier: Modifier = Modifier,
    onOpenLearning: () -> Unit = {},
    onOpenVocabLibrary: () -> Unit = { navController.navigate("deck_list") }
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val dailyTasks by viewModel.dailyTasks.collectAsState()
    var showAddTaskDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadUserData() }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, target ->
                viewModel.addNewDailyTask(title, target)
                showAddTaskDialog = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ThemeBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        HomeHeader(userProfile = userProfile, dailyTasks = dailyTasks)
        Spacer(modifier = Modifier.height(18.dp))

        StudyDashboardCard(userProfile = userProfile, dailyTasks = dailyTasks)
        Spacer(modifier = Modifier.height(18.dp))



        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            HomeQuickAction(
                title = "Vào học",
                subtitle = "Chọn kỹ năng",
                icon = Icons.Default.School,
                modifier = Modifier.weight(1f),
                onClick = onOpenLearning
            )
            HomeQuickAction(
                title = "Flashcard",
                subtitle = "Ôn nhanh",
                icon = Icons.Outlined.Bookmarks,
                modifier = Modifier.weight(1f),
                onClick = onOpenVocabLibrary
            )
        }

        Spacer(modifier = Modifier.height(22.dp))
        DailyTasksSection(
            tasks = dailyTasks,
            onAddTaskClick = { showAddTaskDialog = true },
            onTaskClick = { task -> viewModel.incrementTaskProgress(task) }
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun HomeHeader(userProfile: UserProfile, dailyTasks: List<DailyTask>) {
    val completedTasks = dailyTasks.count { it.current >= it.target }
    val totalTasks = dailyTasks.size
    val allDone = totalTasks > 0 && completedTasks == totalTasks
    val mascotImage = if (allDone) R.drawable.logo else R.drawable.jp_vocabulary
    val dialogueText = if (allDone) {
        "Làm tốt lắm ${userProfile.username.ifEmpty { "cậu" }}! Đã hoàn thành hết nhiệm vụ hôm nay rồi!"
    } else {
        "Chào ${userProfile.username.ifEmpty { "cậu" }}! Hãy cùng nhau cố gắng hơn nữa nha"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFE53935), Color(0xFFB71C1C))
                    ),
                    RoundedCornerShape(22.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("KotoBee", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 15.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(userProfile.jlptLevel, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = dialogueText,
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = mascotImage),
                        contentDescription = "KotoBee Mascot",
                        modifier = Modifier.size(74.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
private fun StudyDashboardCard(userProfile: UserProfile, dailyTasks: List<DailyTask>) {
    val completedTasks = dailyTasks.count { it.current >= it.target }
    val totalTasks = dailyTasks.size
    val taskProgress = if (totalTasks == 0) 0f else completedTasks.toFloat() / totalTasks.toFloat()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, SoftBorder),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tổng quan học tập", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                    Text("Cấp độ ${userProfile.jlptLevel}", color = TextGray, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SoftPink)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("${userProfile.todayStudyPoints} điểm", color = ProgressPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                DashboardMetric(
                    icon = Icons.Default.Star,
                    value = userProfile.todayStudyPoints.toString(),
                    label = "Điểm hôm nay",
                    modifier = Modifier.weight(1f)
                )
                DashboardMetric(
                    icon = Icons.Default.MenuBook,
                    value = userProfile.learnedVocab.toString(),
                    label = "Từ đã học",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                DashboardMetric(
                    icon = Icons.Default.LocalFireDepartment,
                    value = userProfile.streak.toString(),
                    label = "Streak hiện tại",
                    modifier = Modifier.weight(1f)
                )
                DashboardMetric(
                    icon = Icons.Default.DateRange,
                    value = userProfile.activeDays.toString(),
                    label = "Ngày hoạt động",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Nhiệm vụ hôm nay", color = TextDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("$completedTasks/$totalTasks", color = ProgressPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { taskProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = ProgressPrimary,
                trackColor = ProgressTrack
            )
        }
    }
}

@Composable
private fun DashboardMetric(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .heightIn(min = 78.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SoftPink)
            .border(1.dp, SoftBorder, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = ProgressPrimary, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(value, color = TextDark, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(label, color = TextGray, fontSize = 11.sp, lineHeight = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}



@Composable
private fun HomeQuickAction(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .heightIn(min = 116.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(SoftPink, Color.White)
                    ),
                    RoundedCornerShape(20.dp)
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFEF5350).copy(alpha = 0.15f), Color.Transparent)
                        ),
                        CircleShape
                    )
                    .border(1.dp, SoftBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = ProgressPrimary, modifier = Modifier.size(21.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                title,
                color = TextDark,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(subtitle, color = TextGray, fontSize = 12.sp, lineHeight = 16.sp)
        }
    }
}

@Composable
fun DailyTasksSection(
    tasks: List<DailyTask>,
    onAddTaskClick: () -> Unit,
    onTaskClick: (DailyTask) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, SoftBorder),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Nhiệm vụ hôm nay", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                }
                IconButton(onClick = onAddTaskClick) {
                    Icon(Icons.Default.Add, contentDescription = "Thêm", tint = ProgressPrimary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (tasks.isEmpty()) {
                Text("Chưa có nhiệm vụ nào. Cố gắng từng ngày nha", color = TextGray, fontSize = 14.sp)
            } else {
                tasks.forEachIndexed { index, task ->
                    TaskItemUI(task = task, onClick = { onTaskClick(task) })
                    if (index < tasks.size - 1) Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun TaskItemUI(task: DailyTask, onClick: () -> Unit) {
    val done = task.current >= task.target
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (done) Color(0xFFE8F5E9) else Color.White)
            .border(1.dp, if (done) Color(0xFF2E7D32).copy(alpha = 0.3f) else SoftBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (done) Icons.Default.CheckCircle else Icons.Default.TaskAlt,
            contentDescription = null,
            tint = if (done) Color(0xFF2E7D32) else ProgressPrimary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(task.title, fontWeight = FontWeight.Bold, color = TextDark)
            Text("${task.current}/${task.target} lượt", color = TextGray, fontSize = 12.sp)
        }
    }
}

@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onConfirm: (String, Int) -> Unit) {
    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.border(1.dp, SoftBorder, RoundedCornerShape(24.dp)),
        title = { Text("Thêm nhiệm vụ", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tên nhiệm vụ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = taskTextFieldColors()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("Mục tiêu") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = taskTextFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) onConfirm(title.trim(), target.toIntOrNull() ?: 1)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ProgressPrimary)
            ) {
                Text("Thêm", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy", color = TextGray) }
        }
    )
}

@Composable
private fun taskTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    disabledContainerColor = Color.White,
    errorContainerColor = Color.White,
    focusedBorderColor = ProgressPrimary,
    unfocusedBorderColor = SoftBorder,
    focusedLabelColor = ProgressPrimary,
    unfocusedLabelColor = TextGray,
    cursorColor = ProgressPrimary
)
