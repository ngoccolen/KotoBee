package com.example.kotobee.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kotobee.R
import com.example.kotobee.ui.lessons.ProfileScreen
import com.example.kotobee.ui.practice.PracticeScreen

// Màu sắc tươi tươi mang hơi hướng nhật bản
val JapaneseIndigo = Color(0xFF3F51B5) // Xanh đậm thanh lịch
val JapaneseIndigoLight = Color(0xFFE8EAF6)
val SakuraPink = Color(0xFFF06292)
val JapanesePastelBackground = Color(0xFFFFFEFA)
val JapanesePastelPrimary = Color(0xFF81D4FA)
val JapanesePastelPrimaryContainer = Color(0xFFE3F2FD)
val JapanesePastelSecondary = Color(0xFFF8BBD0)
val JapanesePastelTertiary = Color(0xFFFFF9C4)
val JapanesePastelSuccess = Color(0xFFA5D6A7)
val JapanFlagRed = Color(0xFFBC002D)
val DarkGray = Color(0xFF212121)
val GrayText = Color(0xFF616161)
val LightGrayBorder = Color(0xFFE0E0E0)
val ProgressYellow = Color(0xFFFFC107)

@Composable
fun MainScreen(navController: NavController) {
    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf(
        Triple("Trang chủ", Icons.Default.Home, 0),
        Triple("Học tập", Icons.Default.MenuBook, 1),
        Triple("Luyện tập", Icons.Default.Headphones, 2),
        Triple("Hồ sơ", Icons.Default.Person, 3)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = JapanesePastelBackground,
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
                                tint = if (selectedItem == item.third) JapanesePastelPrimary else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                item.first,
                                fontSize = 11.sp,
                                fontWeight = if (selectedItem == item.third) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedItem == item.third) JapanesePastelPrimary else Color.Gray
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = JapanesePastelPrimary,
                            selectedTextColor = JapanesePastelPrimary,
                            indicatorColor = JapanesePastelPrimaryContainer
                        )
                    )
                }
            }
        }
    ) { padding ->
        when (selectedItem) {
            0 -> HomeScreen(navController = navController, modifier = Modifier.padding(padding))
            1 -> LearningScreen(navController = navController, modifier = Modifier.padding(padding))
            2 -> PracticeScreen(modifier = Modifier.padding(padding))
            3 -> Box(modifier = Modifier.padding(padding)) {
                ProfileScreen(navController = navController)
            }
            else -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Nội dung ${items[selectedItem].first}", fontSize = 20.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
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

    LaunchedEffect(Unit) {
        viewModel.loadUserData()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JapanesePastelBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // Phần Header có nền màu
        Box(modifier = Modifier.fillMaxWidth()) {
            // Lớp nền xanh bo cong phía dưới
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        color = JapaneseIndigo,
                        shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                    )
            )

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                HeaderSection(userProfile)

                Spacer(modifier = Modifier.height(20.dp))

                // Thẻ học tiếp tục đè lên phần nền
                ContinueLearningCard(
                    lessonTitle = "Bài 5: Thời gian",
                    lessonSubtitle = "Chương 2: Giờ và Phút",
                    lastPracticedDays = 3,
                    progress = 0.65f,
                    onClick = { /* Điều hướng */ }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Phần nội dung phía dưới
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = "Tiến độ kỹ năng",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGray
            )
            Spacer(modifier = Modifier.height(16.dp))
            SkillsGrid(navController = navController)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun HeaderSection(userProfile: UserProfile) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Bên trái: Avatar và Thông tin
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Avatar giả định
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White, CircleShape)
                    .border(2.dp, SakuraPink, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = JapaneseIndigo,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Chào bạn, ${userProfile.username}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Cấp độ: ${userProfile.jlpt_level}",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Bên phải: Tim và Chuông
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Heart indicator
            Row(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = SakuraPink,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("5", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun ContinueLearningCard(
    lessonTitle: String,
    lessonSubtitle: String,
    lastPracticedDays: Int,
    progress: Float,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nội dung chữ bên trái
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hãy bắt đầu học thôi!",
                        color = JapaneseIndigo,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = lessonTitle,
                        color = DarkGray,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = lessonSubtitle,
                        color = GrayText,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Luyện tập $lastPracticedDays ngày trước",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }

                // Hình minh họa bên phải (Giả định bằng Box/Icon)
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(JapaneseIndigoLight, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Chỗ này sau này bạn thay bằng Image nhé
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = JapaneseIndigo,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Thanh tiến trình và Nút Tiếp tục
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .clip(CircleShape),
                    color = JapaneseIndigo,
                    trackColor = JapaneseIndigoLight
                )

                Spacer(modifier = Modifier.width(20.dp))

                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = JapaneseIndigo),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text("Tiếp tục", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
@Composable
fun SkillsGrid(navController: NavController) {
    // Định nghĩa vài mã màu xanh pastel để đan xen cho đẹp mắt
    val BluePastel1 = Color(0xFF64B5F6)
    val BluePastel2 = Color(0xFF4FC3F7)
    val BluePastel3 = Color(0xFF81D4FA)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp) // Tự động tạo khoảng cách 16.dp giữa các Card
    ) {
        SkillCard(
            title = "Nói",
            progress = 90,
            imageRes = R.drawable.jp_speaking,
            backgroundColor = BluePastel1,
            onClick = { navController.navigate("speaking_practice") }
        )

        SkillCard(
            title = "Đọc",
            progress = 30, // visual
            imageRes = R.drawable.jp_reading,
            backgroundColor = BluePastel2,
            onClick = { navController.navigate("reading_practice") }
        )

        SkillCard(
            title = "Nghe",
            progress = 80, // visual
            imageRes = R.drawable.jp_listening,
            backgroundColor = BluePastel3,
            onClick = { navController.navigate("listening_practice") }
        )

        SkillCard(
            title = "Viết",
            progress = 50, // visual
            imageRes = R.drawable.jp_writing,
            backgroundColor = BluePastel1,
            onClick = { navController.navigate("writing_practice") }
        )

        SkillCard(
            title = "Từ vựng",
            progress = 50, // visual
            imageRes = R.drawable.jp_vocabulary,
            backgroundColor = BluePastel2,
            onClick = { navController.navigate("deck_list") }
        )

        SkillCard(
            title = "Ngữ pháp",
            progress = 50, // visual
            imageRes = R.drawable.jp_grammar,
            backgroundColor = BluePastel3,
            onClick = { navController.navigate("grammar_dashboard") }
        )
    }
}
@Composable
fun SkillCard(
    modifier: Modifier = Modifier,
    title: String,
    progress: Int,
    imageRes: Int,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp), // Vẫn giữ bo góc cho toàn bộ Card
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tiến độ",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "$progress%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Thanh tiến trình
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }

            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(80.dp) // Đã vứt phần .clip() đi
            )
        }
    }
}
@Composable
fun LearningScreen(navController: NavController, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JapanesePastelBackground)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Khám phá bài học",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = JapanesePastelPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Lựa chọn kỹ năng bạn muốn trau dồi hôm nay.",
            fontSize = 15.sp,
            color = GrayText
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Danh sách các module học tập đã được gán route, cập nhật màu pastel
        LearningModuleCard(
            title = "Từ vựng",
            subtitle = "Học từ mới qua Flashcard",
            icon = Icons.Default.Style,
            backgroundColor = JapanesePastelTertiary.copy(alpha = 0.5f),
            iconColor = ProgressYellow,
            onClick = { navController.navigate("deck_list") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LearningModuleCard(
            title = "Ngữ pháp",
            subtitle = "Sắp xếp câu và học cấu trúc",
            icon = Icons.Default.Book,
            backgroundColor = Color(0xFFE8EAF6),
            iconColor = Color(0xFF3F51B5),
            onClick = { navController.navigate("grammar_practice") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LearningModuleCard(
            title = "Luyện nghe",
            subtitle = "Hội thoại thực tế tại quán Cafe",
            icon = Icons.Default.Headphones,
            backgroundColor = Color(0xFFF3E5F5),
            iconColor = Color(0xFF9C27B0),
            onClick = { navController.navigate("listening_practice") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LearningModuleCard(
            title = "Đọc hiểu",
            subtitle = "Cuộc sống ở Tokyo",
            icon = Icons.Default.MenuBook,
            backgroundColor = Color(0xFFE0F7FA),
            iconColor = Color(0xFF00BCD4),
            onClick = { navController.navigate("reading_practice") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LearningModuleCard(
            title = "Viết Kanji",
            subtitle = "Luyện nét chữ Hán trên Canvas",
            icon = Icons.Default.Create,
            backgroundColor = Color(0xFFFBE9E7),
            iconColor = Color(0xFFFF5722),
            onClick = { navController.navigate("writing_practice") }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun LearningModuleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.padding(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = GrayText
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Đi tới",
                tint = iconColor.copy(alpha = 0.7f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}