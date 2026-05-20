import re

file_path = 'app/src/main/java/com/example/kotobee/ui/community/CommunityScreen.kt'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace the LazyColumn calling StudyLeaderboardCard
old_call = '''                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        item {
                            StudyLeaderboardCard(
                                state = leaderboardState,
                                onRetry = viewModel::refreshStudyLeaderboards
                            )
                        }
                    }
                }'''

new_call = '''                } else {
                    LeaderboardTabContent(
                        state = leaderboardState,
                        onRetry = viewModel::refreshStudyLeaderboards
                    )
                }'''

content = content.replace(old_call, new_call)

# Now, replace the StudyLeaderboardCard, LeaderboardRangeChip, LeaderboardRow, EmptyLeaderboardState
old_ui_components_regex = r'@Composable\nprivate fun StudyLeaderboardCard\(.*?@Composable\nprivate fun EmptyLeaderboardState.*?\}\n\}'
# We will just replace it using string split because regex with dotall might be slow or tricky.

start_str = '@Composable\nprivate fun StudyLeaderboardCard('
end_str = '@Composable\nfun CommunityPostCard('

start_idx = content.find(start_str)
end_idx = content.find(end_str)

new_ui_components = '''@Composable
private fun LeaderboardTabContent(
    state: StudyLeaderboardUiState,
    onRetry: () -> Unit
) {
    var selectedRange by remember { mutableStateOf(LeaderboardRange.WEEK) }
    val entries = state.leaderboards.entriesFor(selectedRange)

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            LeaderboardRangeChip(range = LeaderboardRange.DAY, selected = selectedRange == LeaderboardRange.DAY) { selectedRange = LeaderboardRange.DAY }
            Spacer(modifier = Modifier.width(8.dp))
            LeaderboardRangeChip(range = LeaderboardRange.WEEK, selected = selectedRange == LeaderboardRange.WEEK) { selectedRange = LeaderboardRange.WEEK }
            Spacer(modifier = Modifier.width(8.dp))
            LeaderboardRangeChip(range = LeaderboardRange.MONTH, selected = selectedRange == LeaderboardRange.MONTH) { selectedRange = LeaderboardRange.MONTH }
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ProgressPrimary)
            }
        } else if (state.errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.errorMessage, color = ProgressPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    TextButton(onClick = onRetry) { Text("Tải lại", color = ProgressPrimary, fontWeight = FontWeight.Bold) }
                }
            }
        } else if (entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                EmptyLeaderboardState(range = selectedRange)
            }
        } else {
            val maxPoints = entries.first().pointsFor(selectedRange).coerceAtLeast(1)
            val top3 = entries.take(3)
            val remaining = entries.drop(3)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp)
            ) {
                if (top3.isNotEmpty()) {
                    item {
                        TopThreePodium(top3 = top3, selectedRange = selectedRange)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                itemsIndexed(remaining) { index, entry ->
                    val rank = index + 4
                    LeaderboardRow(
                        rank = rank,
                        entry = entry,
                        points = entry.pointsFor(selectedRange),
                        progress = entry.pointsFor(selectedRange).toFloat() / maxPoints.toFloat()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun TopThreePodium(top3: List<StudyLeaderboardEntry>, selectedRange: LeaderboardRange) {
    val GoldColor = Color(0xFFFFD700)
    val SilverColor = Color(0xFFC0C0C0)
    val BronzeColor = Color(0xFFCD7F32)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        if (top3.size >= 2) {
            PodiumItem(user = top3[1], points = top3[1].pointsFor(selectedRange), height = 120, color = SilverColor, position = 2)
        }
        if (top3.isNotEmpty()) {
            PodiumItem(user = top3[0], points = top3[0].pointsFor(selectedRange), height = 160, color = GoldColor, position = 1)
        }
        if (top3.size >= 3) {
            PodiumItem(user = top3[2], points = top3[2].pointsFor(selectedRange), height = 100, color = BronzeColor, position = 3)
        }
    }
}

@Composable
private fun PodiumItem(user: StudyLeaderboardEntry, points: Int, height: Int, color: Color, position: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Box(contentAlignment = Alignment.TopCenter) {
            Box(modifier = Modifier.padding(top = if (position == 1) 16.dp else 8.dp)) {
                AvatarImage(avatarUrl = user.avatarUrl, name = user.username, size = if (position == 1) 76 else 64)
            }
            if (position == 1) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Crown",
                    tint = color,
                    modifier = Modifier
                        .size(36.dp)
                        .offset(y = (-8).dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = user.username.ifBlank { "Người học" },
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            color = TextDark,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "$points XP",
            fontSize = 13.sp,
            color = ProgressPrimary,
            fontWeight = FontWeight.ExtraBold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(color.copy(alpha = 0.6f), color.copy(alpha = 0.1f))
                    )
                )
                .border(
                    BorderStroke(1.dp, color.copy(alpha = 0.8f)),
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = "$position",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun LeaderboardRangeChip(
    range: LeaderboardRange,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        leadingIcon = {
            Icon(range.icon(), contentDescription = null, modifier = Modifier.size(16.dp))
        },
        label = { Text(range.label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ProgressPrimary.copy(alpha = 0.12f),
            selectedLabelColor = ProgressPrimary
        )
    )
}

@Composable
private fun LeaderboardRow(
    rank: Int,
    entry: StudyLeaderboardEntry,
    points: Int,
    progress: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorderColor.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$rank",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = TextGray,
                modifier = Modifier.width(28.dp)
            )
            
            AvatarImage(avatarUrl = entry.avatarUrl, name = entry.username, size = 48)
            
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.username.ifBlank { "Người học" },
                    color = TextDark,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SmallInfoChip(label = entry.jlptLevel, color = StudyBlue)
                    if (entry.streak > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = WarmAmber, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("${entry.streak} ngày", color = TextGray, fontSize = 11.sp)
                        }
                    }
                }
            }
            Text("$points XP", color = ProgressPrimary, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun EmptyLeaderboardState(range: LeaderboardRange) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ProgressTrack)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(range.icon(), contentDescription = null, tint = ProgressPrimary, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Chưa có điểm trong ${range.emptyLabel()}", color = TextDark, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
        Text("BXH sẽ tự cập nhật khi người dùng phát sinh điểm học tập.", color = TextGray, fontSize = 12.sp, lineHeight = 17.sp)
    }
}

'''

content = content[:start_idx] + new_ui_components + content[end_idx:]

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Updated Leaderboard inside CommunityScreen")
