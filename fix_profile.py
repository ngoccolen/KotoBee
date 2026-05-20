import re

with open(r'd:\Documents\KotoBee\app\src\main\java\com\example\kotobee\ui\profile\ProfileScreen.kt', 'r', encoding='utf-8') as f:
    code = f.read()

# Update ColorPalette
color_palette_old = """object ColorPalette {
    val Background = Color(0xFFFFFDFD)
    val CardBackground = Color.White
    val Primary = Color(0xFFE53935) // Đỏ sậm chủ đạo
    val PrimaryLight = Color(0xFFFFEBEE) // Hồng siêu nhạt cho nền icon/track
    val Border = Color(0xFFFFCDD2) // Viền card hồng nhạt
    val Text = Color(0xFF333333)
    val TextSub = Color(0xFF757575)
}"""

color_palette_new = """object ColorPalette {
    val Background = Color(0xFFF9FAFB)
    val CardBackground = Color.White
    val Primary = Color(0xFF6366F1) // Indigo chủ đạo
    val PrimaryLight = Color(0xFFE0E7FF) // Indigo nhạt
    val Border = Color(0xFFE5E7EB) // Gray nhạt
    val Text = Color(0xFF1F2937)
    val TextSub = Color(0xFF6B7280)
}"""
code = code.replace(color_palette_old, color_palette_new)

# Update RankCard to use trophy_3d
rank_card_old = """            Icon(
                Icons.Filled.EmojiEvents,
                contentDescription = null,
                tint = ColorPalette.Primary,
                modifier = Modifier.size(60.dp)
            )"""
rank_card_new = """            Image(
                painter = painterResource(id = R.drawable.trophy_3d),
                contentDescription = null,
                modifier = Modifier.size(65.dp)
            )"""
code = code.replace(rank_card_old, rank_card_new)

# Update StatCard definition
stat_card_old = """@Composable
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
}"""

stat_card_new = """@Composable
fun StatCard(iconId: Int, color: Color, value: String, description: String, modifier: Modifier = Modifier) {
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconId),
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = ColorPalette.Text)
            Text(text = description, fontSize = 12.sp, color = ColorPalette.TextSub)
        }
    }
}"""
code = code.replace(stat_card_old, stat_card_new)

# Update QuickStatsRow calls
quick_stats_old = """@Composable
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
}"""

quick_stats_new = """@Composable
fun QuickStatsRow(state: ProfileState) {
    Row(modifier = Modifier.fillMaxWidth()) {
        StatCard(
            iconId = R.drawable.book_3d,
            color = Color(0xFF3B82F6),
            value = state.learnedVocab.toString(),
            description = "Từ vựng đã thuộc",
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        StatCard(
            iconId = R.drawable.fire_3d,
            color = Color(0xFFEF4444),
            value = state.streak.toString(),
            description = "Ngày học liên tiếp",
            modifier = Modifier.weight(1f)
        )
    }
}"""
code = code.replace(quick_stats_old, quick_stats_new)

# Update Setting Items
settings_list_old = """    val settingItems = listOf(
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
    )"""

settings_list_new = """    val settingItems = listOf(
        SettingItem("Cập nhật hồ sơ", R.drawable.person_3d, Color(0xFF3B82F6)) {
            onEditProfileClick()
        },
        SettingItem("Ghi chú & Lịch trình", R.drawable.memo_3d, Color(0xFF10B981)) {
        },
        SettingItem("Cài đặt hệ thống", R.drawable.settings_3d, Color(0xFF8B5CF6)) {
        },
        SettingItem("Cộng đồng của bạn", R.drawable.community_3d, Color(0xFFF59E0B)) {
        },
        SettingItem("Đăng xuất", 0, Color.Red) {
            viewModel.signOut {
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            }
        }
    )"""
code = code.replace(settings_list_old, settings_list_new)

# Update SettingItem data class
setting_data_old = "data class SettingItem(val name: String, val icon: ImageVector, val action: () -> Unit)"
setting_data_new = "data class SettingItem(val name: String, val iconId: Int, val color: Color, val action: () -> Unit)"
code = code.replace(setting_data_old, setting_data_new)

# Update SettingItemRow
setting_row_old = """@Composable
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
}"""

setting_row_new = """@Composable
fun SettingItemRow(item: SettingItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.action() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (item.name == "Đăng xuất") {
            Surface(
                color = Color(0xFFFFEBEE),
                shape = CircleShape,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.ExitToApp,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            Surface(
                color = item.color.copy(alpha = 0.15f),
                shape = CircleShape,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = item.iconId),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
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
            tint = Color(0xFFD1D5DB),
            modifier = Modifier.size(16.dp)
        )
    }
}"""
code = code.replace(setting_row_old, setting_row_new)

# Save
with open(r'd:\Documents\KotoBee\app\src\main\java\com\example\kotobee\ui\profile\ProfileScreen.kt', 'w', encoding='utf-8') as f:
    f.write(code)

print("Updated ProfileScreen.kt")
