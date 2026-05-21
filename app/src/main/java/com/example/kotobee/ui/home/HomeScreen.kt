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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.kotobee.ui.components.KotoBeeTopBar
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
    val homeViewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val userProfile by homeViewModel.userProfile.collectAsState()

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
        topBar = {
            KotoBeeTopBar(
                username = userProfile.username,
                avatarUrl = userProfile.avatarUrl,
                onAvatarClick = { selectedItem = 3 }
            )
        },
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
                viewModel = homeViewModel,
                modifier = Modifier.padding(padding),
                onOpenLearning = openLearningTab,
                onOpenVocabLibrary = openVocabLibrary,
                onOpenProfile = { selectedItem = 3 }
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
    onOpenVocabLibrary: () -> Unit = { navController.navigate("deck_list") },
    onOpenProfile: () -> Unit = {}
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val dailyTasks by viewModel.dailyTasks.collectAsState()
    val currentGoal by viewModel.currentGoal.collectAsState()
    val showCompletionDialog by viewModel.showCompletionDialog.collectAsState()
    val earnedBadge by viewModel.earnedBadge.collectAsState()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadUserData() }

    // Completion dialog
    if (showCompletionDialog) {
        GoalCompletionDialog(
            badge = earnedBadge,
            onDismiss = { viewModel.dismissCompletionDialog() },
            onViewProfile = {
                viewModel.dismissCompletionDialog()
                onOpenProfile()
            },
            onCreateNewGoal = {
                viewModel.dismissCompletionDialog()
                showAddGoalDialog = true
            }
        )
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, target ->
                viewModel.addNewDailyTask(title, target)
                showAddTaskDialog = false
            }
        )
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { title, milestones ->
                viewModel.createNewGoal(title, milestones)
                showAddGoalDialog = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ThemeBackground)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 22.dp)
    ) {
        // 1. Redesigned HomeHeader: Spans 100% of phone width, touching screen edges
        HomeHeader(userProfile = userProfile, dailyTasks = dailyTasks)
        Spacer(modifier = Modifier.height(24.dp))

        // 2. Learning progress cards styled with soft red gradients (padded horizontal)
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            LearningCardsSection(
                userProfile = userProfile,
                onLearnClick = onOpenLearning,
                onReviewClick = onOpenVocabLibrary
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        // 3. Goal Path Section (padded horizontal)
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            GoalPathSection(
                goal = currentGoal,
                onCreateGoal = { showAddGoalDialog = true },
                onMilestoneClick = { milestone ->
                    viewModel.completeMilestone(milestone.id)
                },
                onDeleteGoal = { viewModel.deleteCurrentGoal() }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        // 4. Daily tasks card section with a bold red border and solid white background (padded horizontal)
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "Nhiệm vụ",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            DailyTasksSection(
                tasks = dailyTasks,
                onAddTaskClick = { showAddTaskDialog = true },
                onTaskClick = { task -> viewModel.incrementTaskProgress(task) }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun HomeHeader(userProfile: UserProfile, dailyTasks: List<DailyTask>) {
    val completedTasks = dailyTasks.count { it.current >= it.target }
    val totalTasks = dailyTasks.size
    val allDone = totalTasks > 0 && completedTasks == totalTasks
    val mascotImage = if (allDone) R.drawable.logo_8 else R.drawable.logo_1
    val dialogueText = if (allDone) {
        "Làm tốt lắm ${userProfile.username.ifEmpty { "cậu" }}! Đã hoàn thành hết nhiệm vụ hôm nay rồi!"
    } else {
        "Chào ${userProfile.username.ifEmpty { "cậu" }}! Hãy cùng nhau cố gắng hơn nữa nha"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFEF5350), Color(0xFFC62828))
                    )
                )
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .padding(start = 20.dp, top = 8.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = dialogueText,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                
                Image(
                    painter = painterResource(id = mascotImage),
                    contentDescription = "KotoBee Mascot",
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
fun LearningCardsSection(
    userProfile: UserProfile,
    onLearnClick: () -> Unit,
    onReviewClick: () -> Unit
) {
    val learnedVal = if (userProfile.learnedVocab == 0) 9 else userProfile.learnedVocab % 15
    val reviewVal = if (userProfile.streak == 0) 1 else userProfile.streak % 30

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Card 1: Học từ mới
        Card(
            modifier = Modifier.fillMaxWidth().height(128.dp),
            shape = RoundedCornerShape(22.dp),
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
                    .border(1.dp, Color(0xFFFFD5D5), RoundedCornerShape(22.dp))
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(62.dp)) {
                            CircularProgressIndicator(
                                progress = { learnedVal.toFloat() / 15f },
                                color = AccentRed,
                                strokeWidth = 5.dp,
                                trackColor = Color(0xFFFFEBEE),
                                modifier = Modifier.fillMaxSize()
                            )
                            Text(
                                text = "$learnedVal/15",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                color = TextDark
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onLearnClick,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Học từ mới", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Image(
                        painter = painterResource(id = R.drawable.logo_5),
                        contentDescription = "Mascot Vocab",
                        modifier = Modifier.size(92.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        // Card 2: Ôn tập ngay
        Card(
            modifier = Modifier.fillMaxWidth().height(128.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFFEBEE), Color(0xFFFFF5F5))
                        )
                    )
                    .border(1.dp, Color(0xFFFFD5D5), RoundedCornerShape(22.dp))
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_3),
                        contentDescription = "Mascot Review",
                        modifier = Modifier.size(92.dp),
                        contentScale = ContentScale.Fit
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(62.dp)) {
                            CircularProgressIndicator(
                                progress = { reviewVal.toFloat() / 30f },
                                color = AccentRed,
                                strokeWidth = 5.dp,
                                trackColor = Color(0xFFFFEBEE),
                                modifier = Modifier.fillMaxSize()
                            )
                            Text(
                                text = "$reviewVal/30",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                color = TextDark
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onReviewClick,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Ôn tập ngay", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyTasksSection(
    tasks: List<DailyTask>,
    onAddTaskClick: () -> Unit,
    onTaskClick: (DailyTask) -> Unit
) {
    val completedTasks = tasks.count { it.current >= it.target }
    val totalTasks = tasks.size
    val taskProgress = if (totalTasks == 0) 0f else completedTasks.toFloat() / totalTasks.toFloat()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(2.dp, Color(0xFFD32F2F)), // Bold red border
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nhiệm vụ hằng ngày",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$completedTasks/$totalTasks",
                        color = ProgressPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onAddTaskClick, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Thêm nhiệm vụ",
                            tint = ProgressPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { taskProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = ProgressPrimary,
                trackColor = Color(0xFFFFEBEE)
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (tasks.isEmpty()) {
                Text(
                    text = "Chưa có nhiệm vụ nào cho hôm nay. Nhấn nút + để thêm nhiệm vụ mới!",
                    color = TextGray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                tasks.forEachIndexed { index, task ->
                    TaskItemUI(
                        task = task,
                        index = index,
                        onClick = { onTaskClick(task) }
                    )
                    if (index < tasks.size - 1) {
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItemUI(task: DailyTask, index: Int, onClick: () -> Unit) {
    val done = task.current >= task.target
    val progressFraction = if (task.target == 0) 0f else task.current.toFloat() / task.target.toFloat()
    val progressPercent = if (task.target == 0) 0 else (task.current * 100) / task.target

    // Map mascot dynamically based on task index or keyword
    val mascotRes = when {
        task.title.contains("ảnh", ignoreCase = true) || task.title.contains("hình", ignoreCase = true) -> R.drawable.jp_reading
        task.title.contains("nghe", ignoreCase = true) || task.title.contains("shadowing", ignoreCase = true) -> R.drawable.jp_listening
        task.title.contains("nói", ignoreCase = true) || task.title.contains("speaking", ignoreCase = true) -> R.drawable.jp_speaking
        task.title.contains("ngữ pháp", ignoreCase = true) || task.title.contains("grammar", ignoreCase = true) -> R.drawable.jp_grammar
        task.title.contains("viết", ignoreCase = true) || task.title.contains("writing", ignoreCase = true) -> R.drawable.jp_writing
        else -> {
            when (index % 5) {
                0 -> R.drawable.jp_vocabulary
                1 -> R.drawable.jp_listening
                2 -> R.drawable.jp_speaking
                3 -> R.drawable.jp_reading
                else -> R.drawable.jp_grammar
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (done) Color(0xFFFFF5F5) else Color.White)
            .border(
                1.dp,
                if (done) Color(0xFFFFCDD2) else SoftBorder,
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = task.title,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(99.dp)),
                color = if (done) Color(0xFF2E7D32) else AccentRed,
                trackColor = Color(0xFFF5F5F5)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${task.current}/${task.target}",
                    color = TextGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$progressPercent%",
                    color = if (done) Color(0xFF2E7D32) else AccentRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))
        
        Image(
            painter = painterResource(id = mascotRes),
            contentDescription = "Mascot Task",
            modifier = Modifier.size(52.dp),
            contentScale = ContentScale.Fit
        )
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
