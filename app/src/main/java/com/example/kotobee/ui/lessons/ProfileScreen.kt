package com.example.kotobee.ui.lessons

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

object ColorPalette {
    val Background = Color(0xFFFDFBF7)
    val CardBackground = Color.White
    val StatCardBackground = Color(0xFFFFFBF1)
    val RankCardBackground = Color(0xFFFFE8AD)
    val Highlight = Color(0xFFFFBC00)
    val Text = Color(0xFF333333)
    val TextSub = Color(0xFF777777)
    val Green = Color(0xFF4CAF50)
}

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val profileState by viewModel.profileState.collectAsState()
    val activityData by viewModel.activityData.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPalette.Background),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
        item { SettingsList(navController, viewModel) }
        item {
            if (profileState.role == "ADMIN") {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { navController.navigate("admin_dashboard") },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)), // Đỏ đậm nổi bật
                    shape = RoundedCornerShape(24.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Filled.Security, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "KHU VỰC QUẢN TRỊ VIÊN",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(state: ProfileState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
            Image(
                painter = painterResource(id = android.R.drawable.stat_sys_warning),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .border(4.dp, Color.White, CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .border(3.dp, ColorPalette.Green, CircleShape)
            )
            Surface(
                color = ColorPalette.Green,
                shape = RoundedCornerShape(50),
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
            Image(
                painter = painterResource(id = android.R.drawable.stat_notify_chat),
                contentDescription = null,
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.TopEnd)
                    .zIndex(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Hồ sơ cá nhân", fontSize = 16.sp, color = ColorPalette.TextSub)
        Text(
            text = state.username.ifEmpty { "Người dùng" },
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = ColorPalette.Text
        )
        Text(text = state.email, fontSize = 14.sp, color = ColorPalette.Highlight)
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ColorPalette.StatCardBackground),
        elevation = CardDefaults.cardElevation(4.dp)
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
                tint = ColorPalette.Highlight,
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ColorPalette.RankCardBackground),
        elevation = CardDefaults.cardElevation(4.dp)
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
                    color = ColorPalette.Text
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
                tint = ColorPalette.Highlight,
                modifier = Modifier.size(60.dp)
            )
        }
    }
}

@Composable
fun ActivityChartCard(activityData: List<ActivityDay>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ColorPalette.CardBackground),
        elevation = CardDefaults.cardElevation(4.dp)
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
                Text(text = "Chi tiết", fontSize = 14.sp, color = ColorPalette.Highlight)
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
                    val isHighlighted = index == 2
                    ActivityColumn(data, isHighlighted)
                }
            }
        }
    }
}

@Composable
fun ActivityColumn(data: ActivityDay, isHighlighted: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val barColor = if (isHighlighted) ColorPalette.Highlight else ColorPalette.Highlight.copy(alpha = 0.3f)
        val barHeight = (data.value.toFloat() / 100 * 80).dp
        Box(
            modifier = Modifier
                .width(20.dp)
                .height(barHeight)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(barColor)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = data.day, fontSize = 10.sp, color = ColorPalette.TextSub)
    }
}

data class Badge(val name: String, val icon: ImageVector, val color: Color)

@Composable
fun BadgesCard() {
    val badges = listOf(
        Badge("Vua Từ Vựng", Icons.Filled.MenuBook, ColorPalette.RankCardBackground),
        Badge("Chăm Chỉ", Icons.Filled.CheckCircle, ColorPalette.Green.copy(alpha = 0.5f)),
        Badge("Sáng Tạo", Icons.Filled.AutoAwesome, Color(0xFF81D4FA))
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ColorPalette.CardBackground),
        elevation = CardDefaults.cardElevation(4.dp)
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
fun SettingsList(navController: NavController, viewModel: ProfileViewModel) {
    val settingItems = listOf(
        SettingItem("Cập nhật hồ sơ", Icons.Filled.PersonOutline) { },
        SettingItem("Học tập & Tiến độ", Icons.Filled.Timeline) { },
        SettingItem("Bộ sưu tập Huy hiệu", Icons.Filled.AutoAwesome) { },
        SettingItem("Gói Plus & Thanh toán", Icons.Filled.AddCircleOutline) { },
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ColorPalette.CardBackground),
        elevation = CardDefaults.cardElevation(4.dp)
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
            color = Color(0xFFF5F5F5),
            shape = CircleShape,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    item.icon,
                    contentDescription = null,
                    tint = ColorPalette.TextSub,
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