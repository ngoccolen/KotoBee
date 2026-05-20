import re

with open(r'd:\Documents\KotoBee\app\src\main\java\com\example\kotobee\ui\home\HomeScreen.kt', 'r', encoding='utf-8') as f:
    code = f.read()

# Update colors
code = code.replace(
    'val ThemeBackground = Color(0xFFFFFFFF)',
    'val ThemeBackground = Color(0xFFF9FAFB)'
)
code = code.replace(
    'val CardBorderColor = Color(0xFFE53935)',
    'val CardBorderColor = Color(0xFFE5E7EB)'
)
code = code.replace(
    'val ProgressPrimary = Color(0xFFD32F2F)',
    'val ProgressPrimary = Color(0xFF6366F1)'
)
code = code.replace(
    'val ProgressTrack = Color(0xFFF7F7F7)',
    'val ProgressTrack = Color(0xFFE5E7EB)'
)
code = code.replace(
    'val TextDark = Color(0xFF333333)',
    'val TextDark = Color(0xFF1F2937)'
)
code = code.replace(
    'val TextGray = Color(0xFF757575)',
    'val TextGray = Color(0xFF6B7280)'
)

# Update Header Gradient
code = code.replace(
    'Brush.horizontalGradient(listOf(Color(0xFFB71C1C), ProgressPrimary, Color(0xFFE53935)))',
    'Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFD946EF)))'
)

# Replace DashboardMetric definition
metric_old = """@Composable
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
            .background(Color.White)
            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(Color.White, CircleShape)
                .border(1.dp, CardBorderColor, CircleShape),
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
}"""

metric_new = """@Composable
private fun DashboardMetric(
    iconId: Int,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .heightIn(min = 82.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(painterResource(id = iconId), contentDescription = null, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(value, color = TextDark, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(label, color = TextGray, fontSize = 11.sp, lineHeight = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}"""
code = code.replace(metric_old, metric_new)

# Replace DashboardMetric calls
calls_old = """            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
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
            }"""

calls_new = """            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                DashboardMetric(
                    iconId = R.drawable.star_3d,
                    value = userProfile.todayStudyPoints.toString(),
                    label = "Điểm hôm nay",
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
                DashboardMetric(
                    iconId = R.drawable.book_3d,
                    value = userProfile.learnedVocab.toString(),
                    label = "Từ đã học",
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                DashboardMetric(
                    iconId = R.drawable.fire_3d,
                    value = userProfile.streak.toString(),
                    label = "Streak hiện tại",
                    color = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )
                DashboardMetric(
                    iconId = R.drawable.calendar_3d,
                    value = userProfile.activeDays.toString(),
                    label = "Ngày hoạt động",
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
            }"""
code = code.replace(calls_old, calls_new)

# Update bottom bar icons/colors
code = code.replace(
    'tint = if (selectedItem == index) ProgressPrimary else Color.Gray',
    'tint = if (selectedItem == index) ProgressPrimary else Color(0xFF9CA3AF)'
)
code = code.replace('IndicatorColor = Color.Transparent', 'indicatorColor = ProgressPrimary.copy(alpha=0.1f)')

# Update Quick Actions icons
qa_old = """        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
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
        }"""
qa_new = """        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            HomeQuickAction(
                title = "Vào học",
                subtitle = "Chọn kỹ năng",
                iconId = R.drawable.pencil_3d,
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f),
                onClick = onOpenLearning
            )
            HomeQuickAction(
                title = "Flashcard",
                subtitle = "Ôn nhanh",
                iconId = R.drawable.memo_3d,
                color = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f),
                onClick = onOpenVocabLibrary
            )
        }"""
code = code.replace(qa_old, qa_new)

qa_def_old = """@Composable
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
        border = BorderStroke(1.dp, CardBorderColor),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(Color.White, CircleShape)
                    .border(1.dp, CardBorderColor, CircleShape),
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
}"""
qa_def_new = """@Composable
private fun HomeQuickAction(
    title: String,
    subtitle: String,
    iconId: Int,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .heightIn(min = 116.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(painterResource(id = iconId), contentDescription = null, modifier = Modifier.size(26.dp))
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
}"""
code = code.replace(qa_def_old, qa_def_new)

with open(r'd:\Documents\KotoBee\app\src\main\java\com\example\kotobee\ui\home\HomeScreen.kt', 'w', encoding='utf-8') as f:
    f.write(code)

print('Updated HomeScreen.kt')
