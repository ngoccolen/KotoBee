package com.example.kotobee.ui.community

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.kotobee.R
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kotobee.model.Comment
import com.example.kotobee.model.CommunityPost
import com.example.kotobee.model.SharedResourceType
import com.example.kotobee.model.SharedStudyResource
import com.example.kotobee.model.StudyLeaderboardEntry
import com.example.kotobee.model.StudyMessage
import com.example.kotobee.model.StudyGroup
import com.example.kotobee.model.StudyGroupPrivacy
import com.example.kotobee.util.formatRelativeTime

val ThemeBackground = Color.White
val CardBorderColor = Color(0xFFE53935)
val ProgressPrimary = Color(0xFFD32F2F)
val TextDark = Color(0xFF333333)
val TextGray = Color(0xFF757575)
val CardBackground = Color.White

private val ProgressTrack = Color(0xFFF7F7F7)
private val StudyTeal = Color(0xFF26A69A)
private val StudyBlue = Color(0xFF5E6AD2)
private val WarmAmber = Color(0xFFF59E0B)
private val SoftSurface = Color(0xFFFFFFFF)
private val DividerSoft = Color(0xFFF4D5D7)

private enum class CommunityTab(val label: String) {
    POSTS("Cộng đồng"),
    LEADERBOARD("BXH")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    viewModel: CommunityViewModel,
    onCreatePostClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val commentsMap by viewModel.commentsMap.collectAsState()
    val leaderboardState by viewModel.leaderboardState.collectAsState()
    val listState = rememberLazyListState()

    var selectedPostId by remember { mutableStateOf<String?>(null) }
    var editingPost by remember { mutableStateOf<CommunityPost?>(null) }
    var editContent by remember { mutableStateOf("") }
    var deletingPost by remember { mutableStateOf<CommunityPost?>(null) }

    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItem >= totalItems - 2
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadMore()
    }

    LaunchedEffect(selectedPostId) {
        selectedPostId?.let { viewModel.loadComments(it) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = ThemeBackground,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onCreatePostClick,
                    containerColor = ProgressPrimary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Đăng bài")
                }
            }
        ) { padding ->
            var selectedTab by remember { mutableStateOf(CommunityTab.POSTS) }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                CommunityHeader()
                CommunityTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        selectedTab = tab
                        if (tab == CommunityTab.LEADERBOARD) {
                            viewModel.loadStudyLeaderboardsIfNeeded()
                        }
                    }
                )
                if (selectedTab == CommunityTab.POSTS) {
                    PostsContent(
                        state = uiState,
                        listState = listState,
                        onLike = viewModel::likePost,
                        onPostClick = { selectedPostId = it.id },
                        canManagePost = viewModel::canManagePost,
                        onEditPost = { post ->
                            editingPost = post
                            editContent = post.content
                        },
                        onDeletePost = { post ->
                            deletingPost = post
                        }
                    )
                } else {
                    LeaderboardTabContent(
                        state = leaderboardState,
                        onRetry = viewModel::refreshStudyLeaderboards
                    )
                }
            }
        }

        val currentPosts = (uiState as? CommunityUiState.Success)?.posts
        val selectedPost = currentPosts?.find { it.id == selectedPostId }

        AnimatedVisibility(
            visible = selectedPost != null,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it })
        ) {
            selectedPost?.let { post ->
                BackHandler { selectedPostId = null }
                PostDetailScreen(
                    post = post,
                    comments = commentsMap[post.id] ?: emptyList(),
                    onBack = { selectedPostId = null },
                    onLike = { viewModel.likePost(post) },
                    onSubmitComment = { text -> viewModel.addComment(post, text) },
                    canManagePost = viewModel.canManagePost(post),
                    onEditPost = {
                        editingPost = post
                        editContent = post.content
                    },
                    onDeletePost = {
                        deletingPost = post
                    }
                )
            }
        }

        editingPost?.let { post ->
            AlertDialog(
                onDismissRequest = { editingPost = null },
                title = {
                    Text("Sửa bài viết", color = TextDark, fontWeight = FontWeight.ExtraBold)
                },
                text = {
                    OutlinedTextField(
                        value = editContent,
                        onValueChange = { editContent = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        placeholder = { Text("Nội dung bài viết", color = TextGray) },
                        colors = communityTextFieldColors()
                    )
                },
                confirmButton = {
                    TextButton(
                        enabled = editContent.trim().isNotBlank() || post.imageUrls.isNotEmpty(),
                        onClick = {
                            viewModel.editPost(post, editContent)
                            editingPost = null
                        }
                    ) {
                        Text("Lưu", color = ProgressPrimary, fontWeight = FontWeight.ExtraBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingPost = null }) {
                        Text("Hủy", color = TextGray, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = Color.White
            )
        }

        deletingPost?.let { post ->
            AlertDialog(
                onDismissRequest = { deletingPost = null },
                title = {
                    Text("Xóa bài viết?", color = TextDark, fontWeight = FontWeight.ExtraBold)
                },
                text = {
                    Text(
                        "Bài viết và các bình luận bên dưới sẽ bị xóa khỏi cộng đồng.",
                        color = TextGray,
                        lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deletePost(post)
                            if (selectedPostId == post.id) {
                                selectedPostId = null
                            }
                            deletingPost = null
                        }
                    ) {
                        Text("Xóa", color = ProgressPrimary, fontWeight = FontWeight.ExtraBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deletingPost = null }) {
                        Text("Hủy", color = TextGray, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = Color.White
            )
        }

    }
}

@Composable
private fun CommunityHeader() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(118.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(listOf(Color(0xFFB71C1C), ProgressPrimary, Color(0xFFE53935))))
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Cộng đồng", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Giao lưu học hỏi và cùng nhau tiến bộ!",
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
            Image(
                painter = painterResource(id = R.drawable.logo_6),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(96.dp)
            )
        }
    }
}

@Composable
private fun CommunityTabs(
    selectedTab: CommunityTab,
    onTabSelected: (CommunityTab) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CommunityTab.values().forEach { tab ->
            val selected = selectedTab == tab
            val selectedColor = if (tab == CommunityTab.POSTS) ProgressPrimary else StudyTeal
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (selected) Color.White else Color.Transparent)
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (tab == CommunityTab.POSTS) Icons.Default.Article else Icons.Default.School,
                    contentDescription = null,
                    tint = if (selected) selectedColor else TextGray,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = tab.label,
                    color = if (selected) selectedColor else TextGray,
                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PostsContent(
    state: CommunityUiState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onLike: (CommunityPost) -> Unit,
    onPostClick: (CommunityPost) -> Unit,
    canManagePost: (CommunityPost) -> Boolean,
    onEditPost: (CommunityPost) -> Unit,
    onDeletePost: (CommunityPost) -> Unit
) {
    when (state) {
        is CommunityUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ProgressPrimary)
            }
        }

        is CommunityUiState.Success -> {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {

                if (state.posts.isEmpty()) {
                    item {
                        EmptyCommunityState(
                            title = "Chưa có bài viết nào",
                            body = "Bắt đầu chia sẻ một ghi chú học tập, hình ảnh bài làm hoặc bộ flashcard hay."
                        )
                    }
                } else {
                    items(state.posts, key = { it.id }) { post ->
                        CommunityPostCard(
                            post = post,
                            onLike = { onLike(post) },
                            onPostClick = { onPostClick(post) },
                            onEdit = { onEditPost(post) },
                            onDelete = { onDeletePost(post) },
                            canManage = canManagePost(post),
                            isDetailView = false
                        )
                    }
                }
            }
        }

        is CommunityUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message, color = ProgressPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CommunityFeedHeader(
    postCount: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .background(Brush.horizontalGradient(listOf(Color(0xFFB71C1C), ProgressPrimary, Color(0xFFE53935))))
                .padding(18.dp)
        ) {
            Column {
                Text("KotoBee Circle", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Bảng tin học tập, câu hỏi nhanh và tài nguyên cộng đồng.",
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HeaderMetric(icon = Icons.Default.Article, value = postCount.toString(), label = "bài viết")
                }
            }
        }
    }
}

@Composable
private fun HeaderMetric(
    icon: ImageVector,
    value: String,
    label: String
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(ProgressTrack)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
    }
}

@Composable
private fun LeaderboardTabContent(
    state: StudyLeaderboardUiState,
    onRetry: () -> Unit
) {
    val entries = state.leaderboards.entries
    val currentUserEntry = state.leaderboards.currentUserEntry

    Column(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ProgressPrimary)
            }
        } else if (state.errorMessage != null && entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.errorMessage, color = ProgressPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    TextButton(onClick = onRetry) { Text("Tải lại", color = ProgressPrimary, fontWeight = FontWeight.Bold) }
                }
            }
        } else if (entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                EmptyLeaderboardState()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp)
            ) {
                if (entries.isNotEmpty()) {
                    item {
                        LeaderboardHeader(totalUsers = entries.size)
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    itemsIndexed(entries) { index, entry ->
                        LeaderboardRow(
                            rankLabel = "${index + 1}",
                            entry = entry,
                            points = entry.totalPoints
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                currentUserEntry?.let { entry ->
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Điểm của bạn",
                            color = TextDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                    item {
                        LeaderboardRow(
                            rankLabel = "Bạn",
                            entry = entry,
                            points = entry.totalPoints
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardHeader(totalUsers: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Bảng xếp hạng học tập",
                    color = TextDark,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Xếp theo tổng điểm học tập",
                    color = TextGray,
                    fontSize = 12.sp
                )
            }
            Text(
                text = "$totalUsers người",
                color = ProgressPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun LeaderboardRow(
    rankLabel: String,
    entry: StudyLeaderboardEntry,
    points: Int
) {
    val containerColor = if (entry.isCurrentUser) ProgressPrimary.copy(alpha = 0.06f) else Color.White
    val borderColor = if (entry.isCurrentUser) ProgressPrimary.copy(alpha = 0.38f) else CardBorderColor

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = rankLabel,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = if (entry.isCurrentUser) ProgressPrimary else TextDark,
                modifier = Modifier.width(42.dp)
            )
            
            AvatarImage(avatarUrl = entry.avatarUrl, name = entry.username, size = 48)
            
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (entry.isCurrentUser) {
                        "${entry.username.ifBlank { "Người học" }} (Bạn)"
                    } else {
                        entry.username.ifBlank { "Người học" }
                    },
                    color = TextDark,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text("Trình độ ${entry.jlptLevel}", color = TextGray, fontSize = 12.sp)
                
            }
            Text("$points điểm", color = ProgressPrimary, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun EmptyLeaderboardState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ProgressTrack)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.BarChart, contentDescription = null, tint = ProgressPrimary, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Chưa có điểm học tập", color = TextDark, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
        Text("BXH sẽ tự cập nhật khi người dùng phát sinh điểm học tập.", color = TextGray, fontSize = 12.sp, lineHeight = 17.sp)
    }
}

@Composable
fun CommunityPostCard(
    post: CommunityPost,
    onLike: () -> Unit,
    onPostClick: () -> Unit,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    canManage: Boolean = false,
    isDetailView: Boolean
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isDetailView) 0.dp else 16.dp, vertical = if (isDetailView) 0.dp else 8.dp)
            .clickable(enabled = !isDetailView) { onPostClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
        shape = if (isDetailView) RoundedCornerShape(0.dp) else RoundedCornerShape(22.dp),
        border = if (isDetailView) null else BorderStroke(1.5.dp, ProgressPrimary.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarImage(
                    avatarUrl = post.author.avatarUrl,
                    name = post.author.username,
                    size = 42
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        post.author.username.ifBlank { "Người học" },
                        color = TextDark,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(buildPostSubtitle(post), color = TextGray, fontSize = 12.sp)
                }
                IconButton(onClick = onLike) {
                    Icon(
                        imageVector = if (post.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Thích",
                        tint = if (post.isLikedByMe) ProgressPrimary else TextGray
                    )
                }
                if (canManage) {
                    Box {
                        IconButton(onClick = { isMenuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Tùy chọn", tint = TextGray)
                        }
                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sửa bài viết", color = TextDark) },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = TextGray) },
                                onClick = {
                                    isMenuExpanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Xóa bài viết", color = ProgressPrimary) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ProgressPrimary) },
                                onClick = {
                                    isMenuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            if (post.content.isNotBlank()) {
                Text(
                    post.content,
                    color = TextDark,
                    modifier = Modifier.padding(top = 14.dp),
                    lineHeight = 21.sp,
                    fontSize = 15.sp
                )
            }

            if (post.tags.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(post.tags) { tag ->
                        SmallInfoChip(label = "#$tag", color = StudyBlue)
                    }
                }
            }

            post.sharedDeck?.let { deck ->
                SharedDeckPreview(
                    title = deck.name,
                    wordCount = deck.wordCount,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            if (post.imageUrls.isNotEmpty()) {
                AsyncImage(
                    model = post.imageUrls.first(),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(top = 14.dp)
                        .fillMaxWidth()
                        .height(230.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Row(
                modifier = Modifier.padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatWithIcon(
                    icon = if (post.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    text = "${post.likesCount}",
                    tint = if (post.isLikedByMe) ProgressPrimary else TextGray
                )
                Spacer(modifier = Modifier.width(16.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(enabled = !isDetailView) { onPostClick() }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Bình luận", tint = TextGray, modifier = Modifier.size(19.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = "${post.commentsCount}", color = TextGray, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun SharedDeckPreview(
    title: String,
    wordCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(StudyTeal.copy(alpha = 0.1f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(StudyTeal.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Style, contentDescription = null, tint = StudyTeal)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextDark, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("$wordCount thẻ flashcard", color = TextGray, fontSize = 12.sp)
        }
    }
}

@Composable
private fun StudyGroupsContent(
    groups: List<StudyGroup>,
    onGroupClick: (StudyGroup) -> Unit,
    onJoinGroup: (String) -> Unit,
    onCreateGroupClick: () -> Unit
) {
    val myGroups = groups.filter { it.isJoinedByMe }
    val discoverGroups = groups.filterNot { it.isJoinedByMe }
    val highlightResources = groups
        .flatMap { group -> group.resources.map { resource -> group to resource } }
        .sortedByDescending { it.second.updatedAt }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        item {
            StudyGroupsHero(
                joinedCount = myGroups.size,
                resourceCount = highlightResources.size,
                onCreateGroupClick = onCreateGroupClick
            )
        }

        item {
            SectionTitle(
                title = "Nhóm của bạn",
                subtitle = "Không gian riêng để học chung và share tài liệu"
            )
        }

        if (myGroups.isEmpty()) {
            item {
                EmptyCommunityState(
                    title = "Bạn chưa có nhóm học tập",
                    body = "Tạo nhóm riêng cho lớp, bạn học hoặc mục tiêu JLPT của bạn."
                )
            }
        } else {
            items(myGroups, key = { it.id }) { group ->
                StudyGroupCard(
                    group = group,
                    onClick = { onGroupClick(group) },
                    onJoinGroup = { onJoinGroup(group.id) }
                )
            }
        }

        if (highlightResources.isNotEmpty()) {
            item {
                SectionTitle(
                    title = "Tài nguyên mới chia sẻ",
                    subtitle = "Bài viết, thư mục và flashcard từ các nhóm"
                )
            }

            items(highlightResources.take(4), key = { "${it.first.id}-${it.second.id}" }) { (group, resource) ->
                SharedResourceCard(
                    resource = resource,
                    groupName = group.name,
                    accentColor = accentFromHex(group.accentColorHex),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        if (discoverGroups.isNotEmpty()) {
            item {
                SectionTitle(
                    title = "Khám phá nhóm học",
                    subtitle = "Xin vào nhóm phù hợp với mục tiêu hiện tại"
                )
            }

            items(discoverGroups, key = { it.id }) { group ->
                StudyGroupCard(
                    group = group,
                    onClick = { onGroupClick(group) },
                    onJoinGroup = { onJoinGroup(group.id) }
                )
            }
        }
    }
}

@Composable
private fun StudyGroupsHero(
    joinedCount: Int,
    resourceCount: Int,
    onCreateGroupClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(StudyTeal, StudyBlue)
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Groups, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Study Groups", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Nhóm riêng cho từng mục tiêu học", color = Color.White.copy(alpha = 0.86f), fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HeaderMetric(icon = Icons.Default.Lock, value = joinedCount.toString(), label = "nhóm của bạn")
                    HeaderMetric(icon = Icons.Default.Folder, value = resourceCount.toString(), label = "tài nguyên")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onCreateGroupClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = StudyTeal)
                ) {
                    Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tạo nhóm học", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun StudyGroupCard(
    group: StudyGroup,
    onClick: () -> Unit,
    onJoinGroup: () -> Unit
) {
    val accent = accentFromHex(group.accentColorHex)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.5.dp, ProgressPrimary.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(accent)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GroupCover(label = group.coverLabel.ifBlank { group.focusLevel }, accent = accent)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                group.name,
                                color = TextDark,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            PrivacyChip(group.privacy)
                        }
                        Text(
                            group.description,
                            color = TextGray,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricPill(icon = Icons.Default.School, label = group.focusLevel, color = accent, modifier = Modifier.weight(1f))
                    MetricPill(icon = Icons.Default.PeopleAlt, label = "${group.memberCount} thành viên", color = StudyBlue, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    MemberPreview(members = group.membersPreview)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "${group.onlineCount} đang học",
                        color = TextGray,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (group.unreadCount > 0) {
                        SmallInfoChip(label = "${group.unreadCount} mới", color = ProgressPrimary)
                    } else if (!group.isJoinedByMe) {
                        OutlinedButton(
                            onClick = onJoinGroup,
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            border = BorderStroke(1.dp, accent.copy(alpha = 0.45f))
                        ) {
                            Text("Tham gia", color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupCover(
    label: String,
    accent: Color
) {
    Box(
        modifier = Modifier
            .size(58.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(accent.copy(alpha = 0.13f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = accent,
            fontSize = if (label.length <= 2) 18.sp else 15.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PrivacyChip(privacy: StudyGroupPrivacy) {
    val privateGroup = privacy == StudyGroupPrivacy.PRIVATE
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (privateGroup) ProgressTrack else StudyTeal.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (privateGroup) Icons.Default.Lock else Icons.Default.Groups,
            contentDescription = null,
            tint = if (privateGroup) ProgressPrimary else StudyTeal,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (privateGroup) "Riêng tư" else "Mở",
            color = if (privateGroup) ProgressPrimary else StudyTeal,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MetricPill(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(17.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            label,
            color = TextDark,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MemberPreview(members: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        members.take(3).forEach { name ->
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(SoftSurface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.take(1).uppercase(),
                    color = ProgressPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun SharedResourceCard(
    resource: SharedStudyResource,
    groupName: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, DividerSoft),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(resourceIcon(resource.type), contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    resource.title,
                    color = TextDark,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    "${resource.type.label} trong $groupName",
                    color = TextGray,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (resource.description.isNotBlank()) {
                    Text(
                        resource.description,
                        color = TextGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("${resource.itemCount}", color = accentColor, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                Text(resourceUnit(resource.type), color = TextGray, fontSize = 11.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    post: CommunityPost,
    comments: List<Comment>,
    onBack: () -> Unit,
    onLike: () -> Unit,
    onSubmitComment: (String) -> Unit,
    canManagePost: Boolean = false,
    onEditPost: () -> Unit = {},
    onDeletePost: () -> Unit = {}
) {
    var commentText by remember { mutableStateOf("") }

    Scaffold(
        containerColor = ThemeBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Bài viết của ${post.author.username.ifBlank { "Người học" }}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại", tint = TextDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Viết bình luận...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ProgressPrimary,
                        unfocusedBorderColor = CardBorderColor,
                        cursorColor = ProgressPrimary,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            onSubmitComment(commentText.trim())
                            commentText = ""
                        }
                    },
                    modifier = Modifier.background(ProgressPrimary, CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gửi", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                CommunityPostCard(
                    post = post,
                    onLike = onLike,
                    onPostClick = {},
                    onEdit = onEditPost,
                    onDelete = onDeletePost,
                    canManage = canManagePost,
                    isDetailView = true
                )
            }

            item {
                HorizontalDivider(thickness = 8.dp, color = ProgressTrack)
                Text(
                    "Bình luận (${comments.size})",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = TextDark,
                    modifier = Modifier.padding(16.dp)
                )
            }

            if (comments.isEmpty()) {
                item {
                    EmptyCommunityState(
                        title = "Chưa có bình luận",
                        body = "Hãy là người đầu tiên mở cuộc trò chuyện."
                    )
                }
            } else {
                items(comments, key = { it.id }) { comment ->
                    CommentBubble(username = comment.username, content = comment.content)
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudyGroupDetailScreen(
    group: StudyGroup,
    messages: List<StudyMessage>,
    onBack: () -> Unit,
    onJoinGroup: () -> Unit,
    onInviteByEmail: (String) -> Unit,
    onAcceptInvite: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onShareResource: (String, String, SharedResourceType) -> Unit
) {
    var showShareSheet by remember { mutableStateOf(false) }
    var showInviteSheet by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }
    val accent = accentFromHex(group.accentColorHex)

    if (showShareSheet) {
        ShareResourceSheet(
            onDismiss = { showShareSheet = false },
            onShare = { title, description, type ->
                onShareResource(title, description, type)
                showShareSheet = false
            }
        )
    }

    if (showInviteSheet) {
        InviteByEmailSheet(
            group = group,
            onDismiss = { showInviteSheet = false },
            onInvite = { email ->
                onInviteByEmail(email)
                showInviteSheet = false
            },
            onAcceptInvite = onAcceptInvite
        )
    }

    Scaffold(
        containerColor = ThemeBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        group.name,
                        color = TextDark,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = TextDark)
                    }
                },
                actions = {
                    IconButton(onClick = { showInviteSheet = true }) {
                        Icon(Icons.Default.GroupAdd, contentDescription = "Mời bằng Gmail", tint = accent)
                    }
                    if (!group.isJoinedByMe) {
                        TextButton(onClick = onJoinGroup) {
                            Text("Tham gia", color = accent, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ThemeBackground)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .imePadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showShareSheet = true }) {
                    Icon(Icons.Default.Share, contentDescription = "Chia sẻ", tint = accent)
                }
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Nhắn vào nhóm...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(22.dp),
                    maxLines = 3,
                    colors = communityTextFieldColors()
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        onSendMessage(messageText)
                        messageText = ""
                    },
                    enabled = messageText.isNotBlank(),
                    modifier = Modifier.background(accent, CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gửi", tint = Color.White)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                ChatGroupHeader(
                    group = group,
                    accent = accent,
                    onShareResource = { showShareSheet = true }
                )
            }

            if (messages.isEmpty()) {
                item {
                    EmptyCommunityState(
                        title = "Chưa có tin nhắn",
                        body = "Gửi lời chào hoặc thả lịch học đầu tiên cho nhóm."
                    )
                }
            } else {
                items(messages, key = { it.id }) { message ->
                    StudyMessageBubble(message = message, accent = accent)
                }
            }

            item {
                SectionTitle(
                    title = "Tài nguyên trong nhóm",
                    subtitle = "Bài viết, thư mục và flashcard đã được chia sẻ"
                )
            }

            if (group.resources.isEmpty()) {
                item {
                    EmptyCommunityState(
                        title = "Chưa có tài nguyên",
                        body = "Chia sẻ bài viết, thư mục hoặc bộ flashcard đầu tiên cho nhóm."
                    )
                }
            } else {
                items(group.resources, key = { it.id }) { resource ->
                    SharedResourceCard(
                        resource = resource,
                        groupName = group.name,
                        accentColor = accent,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }

            if (group.recentNotes.isNotEmpty()) {
                item {
                    SectionTitle(
                        title = "Hoạt động gần đây",
                        subtitle = "Những cập nhật mới nhất của nhóm"
                    )
                }

                items(group.recentNotes) { note ->
                    ActivityRow(note = note, accent = accent)
                }
            }
        }
    }
}

@Composable
private fun ChatGroupHeader(
    group: StudyGroup,
    accent: Color,
    onShareResource: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.22f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GroupCover(label = group.coverLabel.ifBlank { group.focusLevel }, accent = accent)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(group.name, color = TextDark, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp)
                    Text(
                        "${group.memberCount} thành viên · ${group.onlineCount} đang học",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }
                TextButton(onClick = onShareResource) {
                    Text("Ghim tài liệu", color = accent, fontWeight = FontWeight.ExtraBold)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(group.description, color = TextDark, fontSize = 14.sp, lineHeight = 20.sp)
            if (group.pendingInviteEmails.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Đang chờ: ${group.pendingInviteEmails.joinToString(", ")}",
                    color = WarmAmber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StudyMessageBubble(
    message: StudyMessage,
    accent: Color
) {
    val alignment = if (message.isMine) Alignment.End else Alignment.Start
    val bubbleColor = if (message.isMine) accent else Color.White
    val textColor = if (message.isMine) Color.White else TextDark

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        horizontalAlignment = alignment
    ) {
        if (!message.isMine) {
            Text(
                message.senderName.ifBlank { message.senderEmail.substringBefore("@") },
                color = TextGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
        }
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (message.isMine) 18.dp else 4.dp,
                bottomEnd = if (message.isMine) 4.dp else 18.dp
            ),
            color = bubbleColor,
            border = if (message.isMine) null else BorderStroke(1.dp, DividerSoft)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(message.content, color = textColor, fontSize = 14.sp, lineHeight = 20.sp)
                Text(
                    formatRelativeTime(message.timestamp),
                    color = textColor.copy(alpha = 0.72f),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InviteByEmailSheet(
    group: StudyGroup,
    onDismiss: () -> Unit,
    onInvite: (String) -> Unit,
    onAcceptInvite: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 28.dp)
        ) {
            Text("Mời bằng Gmail", color = TextDark, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                "Người được mời sẽ thấy lời mời và bấm chấp nhận trước khi vào nhóm.",
                color = TextGray,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Gmail của bạn học") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = communityTextFieldColors()
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = { onInvite(email) },
                enabled = email.contains("@"),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StudyTeal)
            ) {
                Text("Gửi lời mời", color = Color.White, fontWeight = FontWeight.ExtraBold)
            }

            if (group.pendingInviteEmails.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))
                Text("Lời mời đang chờ", color = TextDark, fontWeight = FontWeight.ExtraBold)
                group.pendingInviteEmails.forEach { pendingEmail ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(pendingEmail, color = TextDark, modifier = Modifier.weight(1f))
                        TextButton(onClick = { onAcceptInvite(pendingEmail) }) {
                            Text("Chấp nhận", color = StudyTeal, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupDetailHeader(
    group: StudyGroup,
    accent: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
                    .background(Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.72f)))),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GroupCover(label = group.coverLabel.ifBlank { group.focusLevel }, accent = Color.White)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(group.name, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        Text(group.weeklyGoal, color = Color.White.copy(alpha = 0.86f), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(group.description, color = TextDark, lineHeight = 20.sp)
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricPill(icon = Icons.Default.PeopleAlt, label = "${group.memberCount} thành viên", color = StudyBlue, modifier = Modifier.weight(1f))
                    MetricPill(icon = Icons.Default.School, label = group.focusLevel, color = accent, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PrivacyChip(group.privacy)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("${group.onlineCount} người đang học", color = TextGray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(
    note: String,
    accent: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(accent)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(note, color = TextDark, fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateStudyGroupSheet(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf("N4") }
    var privateGroup by remember { mutableStateOf(true) }
    val levels = listOf("N5", "N4", "N3", "N2", "N1")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 28.dp)
        ) {
            Text("Tạo nhóm học tập", color = TextDark, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text("Không gian riêng để share bài, thư mục và flashcard.", color = TextGray, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên nhóm") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = communityTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Mô tả ngắn") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = communityTextFieldColors()
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text("Mục tiêu JLPT", color = TextDark, fontWeight = FontWeight.Bold)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                items(levels) { level ->
                    FilterChip(
                        selected = selectedLevel == level,
                        onClick = { selectedLevel = level },
                        label = { Text(level) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StudyTeal.copy(alpha = 0.15f),
                            selectedLabelColor = StudyTeal
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (selectedLevel == level) StudyTeal else DividerSoft,
                            selectedBorderColor = StudyTeal,
                            enabled = true,
                            selected = selectedLevel == level
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = privateGroup,
                    onClick = { privateGroup = true },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text("Riêng tư") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ProgressTrack, selectedLabelColor = ProgressPrimary)
                )
                FilterChip(
                    selected = !privateGroup,
                    onClick = { privateGroup = false },
                    leadingIcon = { Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text("Mở") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StudyTeal.copy(alpha = 0.15f), selectedLabelColor = StudyTeal)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onCreate(name, description, selectedLevel, privateGroup) },
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StudyTeal, contentColor = Color.White)
            ) {
                Text("Tạo nhóm", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareResourceSheet(
    onDismiss: () -> Unit,
    onShare: (String, String, SharedResourceType) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(SharedResourceType.FLASHCARD) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 28.dp)
        ) {
            Text("Chia sẻ tài nguyên", color = TextDark, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text("Gửi bài viết, thư mục hoặc flashcard vào nhóm.", color = TextGray, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
            Spacer(modifier = Modifier.height(18.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SharedResourceType.values()) { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        leadingIcon = { Icon(resourceIcon(type), contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text(type.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StudyTeal.copy(alpha = 0.14f),
                            selectedLabelColor = StudyTeal
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tiêu đề") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = communityTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Ghi chú") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = communityTextFieldColors()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onShare(title, description, selectedType) },
                enabled = title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StudyTeal, contentColor = Color.White)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chia sẻ vào nhóm", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun CommentBubble(username: String, content: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        AvatarImage(avatarUrl = "", name = username, size = 36)
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF0F2F5))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(text = username.ifBlank { "Người học" }, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = TextDark)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = content, fontSize = 14.sp, color = TextDark, lineHeight = 19.sp)
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(title, color = TextDark, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        Text(subtitle, color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun EmptyCommunityState(
    title: String,
    body: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(ProgressTrack),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = ProgressPrimary)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, color = TextDark, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        Text(body, color = TextGray, fontSize = 13.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun AvatarImage(
    avatarUrl: String,
    name: String,
    size: Int
) {
    if (avatarUrl.isNotBlank()) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = null,
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(CardBorderColor),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(ProgressTrack),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = ProgressPrimary, modifier = Modifier.size((size * 0.55f).dp))
        }
    }
}

@Composable
private fun SmallInfoChip(
    label: String,
    color: Color
) {
    Text(
        text = label,
        color = color,
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 9.dp, vertical = 5.dp)
    )
}

@Composable
private fun StatWithIcon(
    icon: ImageVector,
    text: String,
    tint: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = text, color = TextGray, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun communityTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = StudyTeal,
    unfocusedBorderColor = DividerSoft,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    cursorColor = StudyTeal
)

private fun resourceIcon(type: SharedResourceType): ImageVector {
    return when (type) {
        SharedResourceType.POST -> Icons.Default.Article
        SharedResourceType.FOLDER -> Icons.Default.Folder
        SharedResourceType.FLASHCARD -> Icons.Default.Style
    }
}

private fun resourceUnit(type: SharedResourceType): String {
    return when (type) {
        SharedResourceType.POST -> "bài"
        SharedResourceType.FOLDER -> "mục"
        SharedResourceType.FLASHCARD -> "thẻ"
    }
}

private fun rankColor(rank: Int): Color {
    return when (rank) {
        1 -> ProgressPrimary
        2 -> StudyBlue
        3 -> WarmAmber
        else -> TextGray
    }
}

private fun accentFromHex(hex: String): Color {
    return runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(ProgressPrimary)
}

private fun buildPostSubtitle(post: CommunityPost): String {
    return buildString {
        append("JLPT ${post.author.jlptLevel}")
        if (post.author.streak > 0) append(" • ${post.author.streak} ngày")
        append(" • ${formatRelativeTime(post.timestamp)}")
    }
}
