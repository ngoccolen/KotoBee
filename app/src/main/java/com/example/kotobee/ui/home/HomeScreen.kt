package com.example.kotobee.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kotobee.R
import com.example.kotobee.ui.lessons.LearningScreen
import com.example.kotobee.ui.lessons.vocab.DeckListScreen
import com.example.kotobee.ui.profile.ProfileScreen

val ThemeBackground = Color(0xFFFFFDFD) // Nền trắng ngà ánh hồng
val CardBorderColor = Color(0xFFFFCDD2) // Viền card hồng nhạt
val ProgressPrimary = Color(0xFFE53935) // Đỏ sậm làm màu chủ đạo cho thanh tiến trình
val ProgressTrack = Color(0xFFFFEBEE) // Màu nền của thanh tiến trình (hồng siêu nhạt)
val TextDark = Color(0xFF333333)
val TextGray = Color(0xFF757575)

@Composable
fun MainScreen(navController: NavController) {
    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf(
        Triple("Trang chủ", Icons.Default.Home, 0),
        Triple("Học tập", Icons.Default.MenuBook, 1),
        Triple("Quét ảnh", Icons.Default.PhotoCamera, 2),
        Triple("Hồ sơ", Icons.Default.Person, 3)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = ThemeBackground,
                tonalElevation = 8.dp
            ) {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = selectedItem == item.third,
                        onClick = { selectedItem = item.third },
                        icon = {
                            Icon(
                                imageVector = item.second,
                                contentDescription = item.first,
                                tint = if (selectedItem == item.third) ProgressPrimary else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                item.first,
                                fontSize = 11.sp,
                                fontWeight = if (selectedItem == item.third) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedItem == item.third) ProgressPrimary else Color.Gray
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ProgressPrimary,
                            selectedTextColor = ProgressPrimary,
                            indicatorColor = CardBorderColor
                        )
                    )
                }
            }
        }
    ) { padding ->
        when (selectedItem) {
            0 -> HomeScreen(navController = navController, modifier = Modifier.padding(padding))
            1 -> LearningScreen(navController = navController, modifier = Modifier.padding(padding))
            // ĐÃ SỬA: Gọi màn hình VisionFlashcardScreen và bọc trong Box có padding để không bị thanh điều hướng che mất
            2 -> Box(modifier = Modifier.padding(padding)) {  }
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
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val dailyTasks by viewModel.dailyTasks.collectAsState()

    var showAddTaskDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadUserData()
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ThemeBackground)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // --- Header (Tên người dùng) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Xin chào,", fontSize = 16.sp, color = TextGray)
                Text(
                    text = userProfile.username.ifEmpty { "Người học" },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }
            Box(
                modifier = Modifier.size(50.dp).background(Color.White, CircleShape).border(2.dp, CardBorderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = "Avatar", tint = ProgressPrimary)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 1. Thẻ Gradient Ôn tập tiếp ---
        ContinueLearningGradientCard()

        Spacer(modifier = Modifier.height(24.dp))
        Text("Nhiệm vụ", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextDark, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(12.dp))

        // --- 2. Tiến trình Level (N5 -> N4) ---
        LevelProgressSection(userProfile)

        Spacer(modifier = Modifier.height(24.dp))

        // --- 3. Nhiệm vụ hằng ngày ---
        DailyTasksSection(
            tasks = dailyTasks,
            onAddTaskClick = { showAddTaskDialog = true },
            onTaskClick = { task -> viewModel.incrementTaskProgress(task) }
        )

        Spacer(modifier = Modifier.height(28.dp))

        // --- 4. Tiến độ từng kỹ năng (Mỗi kỹ năng 1 hàng, thanh linear) ---
        SkillsProgressList(userProfile.skills_progress)

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ContinueLearningGradientCard() {
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFF8A80), Color(0xFFE53935)) // Đỏ hồng sang đỏ đậm
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(gradientBrush)
            .clickable { /* TODO: Chuyển đến bài học */ }
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = R.drawable.jp_reading),
                contentDescription = "Học tiếp",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(90.dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { 0.65f },
                        modifier = Modifier.fillMaxSize(),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f),
                        strokeWidth = 4.dp,
                        strokeCap = StrokeCap.Round
                    )
                    Text("1/30", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("Ôn tập ngay", color = ProgressPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LevelProgressSection(userProfile: UserProfile) {
    val animatedProgress by animateFloatAsState(targetValue = userProfile.level_progress, animationSpec = tween(1000))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Mục tiêu: Lên ${userProfile.next_level}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = ProgressPrimary,
                    trackColor = ProgressTrack,
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(userProfile.current_level, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ProgressPrimary)
                    Text("${(userProfile.level_progress * 100).toInt()}%", fontSize = 12.sp, color = TextGray)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))
            Image(
                painter = painterResource(id = R.drawable.jp_grammar),
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun DailyTasksSection(tasks: List<DailyTask>, onAddTaskClick: () -> Unit, onTaskClick: (DailyTask) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Nhiệm vụ hằng ngày", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                IconButton(onClick = onAddTaskClick, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Rounded.Add, contentDescription = "Thêm", tint = ProgressPrimary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (tasks.isEmpty()) {
                Text("Chưa có nhiệm vụ nào. Nhấn '+' để thêm!", color = TextGray, fontSize = 14.sp)
            } else {
                tasks.forEachIndexed { index, task ->
                    TaskItemUI(task, onClick = { onTaskClick(task) })
                    if (index < tasks.size - 1) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = ProgressTrack)
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItemUI(task: DailyTask, onClick: () -> Unit) {
    val progressRatio = if (task.target > 0) (task.current.toFloat() / task.target.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(targetValue = progressRatio, animationSpec = tween(800))
    val percent = (progressRatio * 100).toInt()

    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = task.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextGray)
            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = ProgressPrimary,
                trackColor = ProgressTrack,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${task.current}/${task.target}", fontSize = 12.sp, color = TextGray)
                Text("$percent%", fontSize = 12.sp, color = TextGray)
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Image(
            painter = painterResource(id = R.drawable.jp_vocabulary),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(50.dp)
        )
    }
}

// --- THAY ĐỔI MỚI Ở ĐÂY: Dạng List nằm ngang ---
@Composable
fun SkillsProgressList(skillsMap: Map<String, Float>) {
    Text("Tiến độ kỹ năng", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
    Spacer(modifier = Modifier.height(12.dp))

    Column(modifier = Modifier.fillMaxWidth()) {
        skillsMap.forEach { (skillName, progress) ->
            SkillHorizontalCard(skillName = skillName, progress = progress)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SkillHorizontalCard(skillName: String, progress: Float) {
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(1000))

    // Tự động phân loại ảnh dựa theo tên kỹ năng
    val imageRes = when (skillName.lowercase()) {
        "từ vựng" -> R.drawable.jp_vocabulary
        "ngữ pháp" -> R.drawable.jp_grammar
        "nghe hiểu", "nghe" -> R.drawable.jp_listening // Nếu bạn có file ảnh jp_listening, nếu không nó sẽ mặc định dùng jp_reading
        "đọc hiểu", "đọc" -> R.drawable.jp_reading
        "viết", "hán tự" -> R.drawable.jp_writing // Nếu có
        else -> R.drawable.jp_reading
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { /* TODO: Chuyển đến màn học kĩ năng tương ứng */ },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ảnh minh họa to rõ ràng bên trái
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = skillName,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(70.dp) // Cỡ to theo yêu cầu
            )

            Spacer(modifier = Modifier.width(20.dp))

            // Thanh tiến trình và Tên bên phải
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = skillName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text(text = "${(progress * 100).toInt()}%", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = ProgressPrimary)
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = ProgressPrimary,
                    trackColor = ProgressTrack,
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onConfirm: (String, Int) -> Unit) {
    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm nhiệm vụ hằng ngày", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tên nhiệm vụ (VD: Học 5 từ mới)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("Mục tiêu (Số lượng, VD: 5)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val targetInt = target.toIntOrNull() ?: 1
                    if (title.isNotBlank()) onConfirm(title, targetInt)
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