import sys

file_path = 'app/src/main/java/com/example/kotobee/ui/community/CommunityScreen.kt'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace the CommunityTab enum
old_enum = '''private enum class CommunityTab(val label: String) {
    POSTS("Bài viết"),
    GROUPS("Nhóm học tập")
}'''

new_enum = '''private enum class CommunityTab(val label: String) {
    POSTS("Cộng đồng"),
    LEADERBOARD("BXH")
}'''
content = content.replace(old_enum, new_enum)

# Add selectedTab state and tabs in CommunityScreen
old_column = '''        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                PostsContent(
                    state = uiState,
                    leaderboardState = leaderboardState,
                    listState = listState,
                    onLike = viewModel::likePost,
                    onPostClick = { selectedPostId = it.id },
                    onRetryLeaderboard = viewModel::refreshStudyLeaderboards
                )
            }
        }'''

new_column = '''        ) { padding ->
            var selectedTab by remember { mutableStateOf(CommunityTab.POSTS) }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                CommunityTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
                if (selectedTab == CommunityTab.POSTS) {
                    PostsContent(
                        state = uiState,
                        listState = listState,
                        onLike = viewModel::likePost,
                        onPostClick = { selectedPostId = it.id }
                    )
                } else {
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
                }
            }
        }'''
content = content.replace(old_column, new_column)

# Update PostsContent to remove leaderboard
old_posts_content_def = '''private fun PostsContent(
    state: CommunityUiState,
    leaderboardState: StudyLeaderboardUiState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onLike: (CommunityPost) -> Unit,
    onPostClick: (CommunityPost) -> Unit,
    onRetryLeaderboard: () -> Unit
) {'''

new_posts_content_def = '''private fun PostsContent(
    state: CommunityUiState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onLike: (CommunityPost) -> Unit,
    onPostClick: (CommunityPost) -> Unit
) {'''
content = content.replace(old_posts_content_def, new_posts_content_def)

# Remove leaderboard from PostsContent LazyColumn
old_leaderboard_in_lazy = '''            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                item {
                    StudyLeaderboardCard(
                        state = leaderboardState,
                        onRetry = onRetryLeaderboard
                    )
                }'''

new_leaderboard_in_lazy = '''            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {'''
content = content.replace(old_leaderboard_in_lazy, new_leaderboard_in_lazy)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Refactored CommunityScreen")
