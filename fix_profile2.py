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
    val Primary = Color(0xFFE53935)
    val Text = Color(0xFF212529)
    val TextSub = Color(0xFF6C757D)
    val PurpleTheme = Color(0xFF7B85D4)
    val OrangeTheme = Color(0xFFF49D6E)
}"""
code = code.replace(color_palette_old, color_palette_new)

# Update QuickStatsRow
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
            icon = Icons.Filled.MenuBook,
            value = state.learnedVocab.toString(),
            description = "Từ vựng đã thuộc",
            iconContainerColor = ColorPalette.OrangeTheme,
            iconColor = Color.White,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        StatCard(
            icon = Icons.Filled.LocalFireDepartment,
            value = state.streak.toString(),
            description = "Ngày liên tiếp",
            iconContainerColor = ColorPalette.PurpleTheme,
            iconColor = Color.White,
            modifier = Modifier.weight(1f)
        )
    }
}"""
code = code.replace(quick_stats_old, quick_stats_new)

# Update StatCard
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
fun StatCard(
    icon: ImageVector, 
    value: String, 
    description: String, 
    iconContainerColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, iconContainerColor.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.size(54.dp),
                    color = iconContainerColor,
                    shape = RoundedCornerShape(bottomEnd = 24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = description, 
                    fontSize = 12.sp, 
                    color = Color.DarkGray, 
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end=4.dp)
                )
            }
            
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = value, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}"""
code = code.replace(stat_card_old, stat_card_new)

# Update RankCard
rank_card_old = """@Composable
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
}"""

rank_card_new = """@Composable
fun RankCard(state: ProfileState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ColorPalette.PurpleTheme),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "XẾP HẠNG",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = state.rankInfo,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Bạn đang dẫn đầu nhóm!",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            Surface(
                color = Color.White.copy(alpha = 0.2f),
                shape = CircleShape,
                modifier = Modifier.size(70.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}"""
code = code.replace(rank_card_old, rank_card_new)


# Replace ActivityChartCard / BadgesCard / SettingsList Background & Border
code = code.replace(
    'shape = RoundedCornerShape(16.dp),\n        border = BorderStroke(1.dp, ColorPalette.Border),\n        colors = CardDefaults.cardColors(containerColor = ColorPalette.CardBackground),\n        elevation = CardDefaults.cardElevation(0.dp)',
    'shape = RoundedCornerShape(24.dp),\n        border = BorderStroke(1.dp, Color(0xFFF3F4F6)),\n        colors = CardDefaults.cardColors(containerColor = Color.White),\n        elevation = CardDefaults.cardElevation(1.dp)'
)

# Settings List changes
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
    val isLogout = item.name == "Đăng xuất"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.action() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = if (isLogout) Color(0xFFFFEBEE) else Color(0xFFF3F4F6),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    item.icon,
                    contentDescription = null,
                    tint = if (isLogout) Color.Red else ColorPalette.TextSub,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.name,
            fontSize = 16.sp,
            color = if (isLogout) Color.Red else ColorPalette.Text,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = Color(0xFFE5E7EB),
            modifier = Modifier.size(16.dp)
        )
    }
}"""
code = code.replace(setting_row_old, setting_row_new)

with open(r'd:\Documents\KotoBee\app\src\main\java\com\example\kotobee\ui\profile\ProfileScreen.kt', 'w', encoding='utf-8') as f:
    f.write(code)

print("Updated ProfileScreen.kt")
