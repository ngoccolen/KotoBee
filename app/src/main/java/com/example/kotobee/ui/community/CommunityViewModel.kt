package com.example.kotobee.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kotobee.data.repository.CommunityRepository
import com.example.kotobee.model.Comment
import com.example.kotobee.model.CommunityPost
import com.example.kotobee.model.SharedResourceType
import com.example.kotobee.model.SharedStudyResource
import com.example.kotobee.model.StudyGroup
import com.example.kotobee.model.StudyGroupPrivacy
import com.example.kotobee.model.StudyLeaderboards
import com.example.kotobee.model.StudyMessage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CommunityUiState {
    object Loading : CommunityUiState()
    data class Success(val posts: List<CommunityPost>) : CommunityUiState()
    data class Error(val message: String) : CommunityUiState()
}

data class StudyLeaderboardUiState(
    val isLoading: Boolean = true,
    val leaderboards: StudyLeaderboards = StudyLeaderboards(),
    val hasLoaded: Boolean = false,
    val errorMessage: String? = null
)

class CommunityViewModel(
    private val repository: CommunityRepository
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<CommunityUiState>(CommunityUiState.Loading)
    val uiState = _uiState.asStateFlow()

    // Map chứa bình luận của từng bài viết
    private val _commentsMap = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())
    val commentsMap = _commentsMap.asStateFlow()

    private val _studyGroups = MutableStateFlow(seedStudyGroups())
    val studyGroups = _studyGroups.asStateFlow()

    private val _groupMessages = MutableStateFlow(seedGroupMessages())
    val groupMessages = _groupMessages.asStateFlow()

    private val _leaderboardState = MutableStateFlow(StudyLeaderboardUiState())
    val leaderboardState = _leaderboardState.asStateFlow()

    private var currentPosts = mutableListOf<CommunityPost>()
    private var isLoadingMore = false
    private var isLoadingLeaderboards = false
    private var hasMorePosts = true

    init {
        loadInitialPosts()
        refreshStudyLeaderboards()
    }

    private fun loadInitialPosts() {
        viewModelScope.launch {
            try {
                _uiState.value = CommunityUiState.Loading
                repository.getCommunityFeed(limit = PAGE_SIZE).collect { posts ->
                    val currentUserId = auth.currentUser?.uid
                    currentPosts = posts.map { it.withLikeState(currentUserId) }.toMutableList()
                    hasMorePosts = posts.size >= PAGE_SIZE
                    _uiState.value = CommunityUiState.Success(currentPosts.toList())
                }
            } catch (e: Exception) {
                _uiState.value = CommunityUiState.Error(e.message ?: "Lỗi tải dữ liệu")
            }
        }
    }

    fun loadMore() {
        if (isLoadingMore || !hasMorePosts || _uiState.value !is CommunityUiState.Success) return

        viewModelScope.launch {
            isLoadingMore = true
            try {
                val currentUserId = auth.currentUser?.uid
                val morePosts = repository.loadMorePosts(limit = PAGE_SIZE)
                    .map { it.withLikeState(currentUserId) }
                hasMorePosts = morePosts.size >= PAGE_SIZE

                if (morePosts.isNotEmpty()) {
                    val existingIds = currentPosts.map { it.id }.filter { it.isNotBlank() }.toSet()
                    val newPosts = morePosts.filter { post -> post.id.isBlank() || post.id !in existingIds }

                    if (newPosts.isNotEmpty()) {
                        currentPosts.addAll(newPosts)
                        _uiState.value = CommunityUiState.Success(currentPosts.toList())
                    }
                }
            } catch (_: Exception) {
            } finally {
                isLoadingMore = false
            }
        }
    }

    fun loadStudyLeaderboardsIfNeeded() {
        if (_leaderboardState.value.hasLoaded || isLoadingLeaderboards) return
        refreshStudyLeaderboards()
    }

    fun refreshStudyLeaderboards() {
        if (isLoadingLeaderboards) return

        viewModelScope.launch {
            isLoadingLeaderboards = true
            val currentState = _leaderboardState.value
            _leaderboardState.value = currentState.copy(
                isLoading = currentState.leaderboards.entries.isEmpty(),
                errorMessage = null
            )
            try {
                _leaderboardState.value = StudyLeaderboardUiState(
                    isLoading = false,
                    leaderboards = repository.getStudyLeaderboards(
                        currentUserEmail = auth.currentUser?.email
                    ),
                    hasLoaded = true
                )
            } catch (e: Exception) {
                _leaderboardState.value = _leaderboardState.value.copy(
                    isLoading = false,
                    hasLoaded = currentState.hasLoaded,
                    errorMessage = e.message ?: "Lỗi tải bảng xếp hạng"
                )
            } finally {
                isLoadingLeaderboards = false
            }
        }
    }

    // --- LẮNG NGHE BÌNH LUẬN TỪ REPOSITORY ---
    fun loadComments(postId: String) {
        if (postId.isBlank()) return
        viewModelScope.launch {
            repository.getComments(postId).collect { comments ->
                val updatedMap = _commentsMap.value.toMutableMap()
                updatedMap[postId] = comments
                _commentsMap.value = updatedMap
            }
        }
    }

    // --- GỬI BÌNH LUẬN ---
    fun addComment(post: CommunityPost, content: String) {
        val currentUser = auth.currentUser ?: return
        if (post.id.isBlank() || content.isBlank()) return

        val username = currentUser.displayName?.takeIf { it.isNotBlank() } ?: "Người học"

        viewModelScope.launch {
            repository.addComment(
                postId = post.id,
                userId = currentUser.uid,
                username = username,
                content = content
            )
            // Lưu ý: Không cần tự cộng số lượng ở local nữa,
            // vì FireStore update xong hàm getCommunityFeed sẽ tự đẩy data mới về!
        }
    }

    // --- THẢ TIM ---
    fun likePost(post: CommunityPost) {
        val userId = auth.currentUser?.uid ?: return
        if (post.id.isBlank()) return

        // Cập nhật UI ngay cho mượt
        val wasLiked = post.isLikedByMe || userId in post.likedBy
        currentPosts = currentPosts.map { item ->
            if (item.id == post.id) item.toggledLike(userId, wasLiked) else item
        }.toMutableList()
        _uiState.value = CommunityUiState.Success(currentPosts.toList())

        viewModelScope.launch {
            val result = repository.toggleLike(postId = post.id, userId = userId, isLiked = wasLiked)
            if (result.isFailure) {
                currentPosts = currentPosts.map { item ->
                    if (item.id == post.id) item.toggledLike(userId, !wasLiked) else item
                }.toMutableList()
                _uiState.value = CommunityUiState.Success(currentPosts.toList())
            }
        }
    }

    fun canManagePost(post: CommunityPost): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        return post.id.isNotBlank() && post.author.uid == userId
    }

    fun editPost(post: CommunityPost, content: String) {
        val userId = auth.currentUser?.uid ?: return
        val trimmedContent = content.trim()
        if (!canManagePost(post) || (trimmedContent.isBlank() && post.imageUrls.isEmpty())) return

        val previousPosts = currentPosts.toList()
        val updatedAt = System.currentTimeMillis()
        currentPosts = currentPosts.map { item ->
            if (item.id == post.id) {
                item.copy(content = trimmedContent, updatedAt = updatedAt)
            } else {
                item
            }
        }.toMutableList()
        _uiState.value = CommunityUiState.Success(currentPosts.toList())

        viewModelScope.launch {
            val result = repository.updatePost(
                postId = post.id,
                authorId = userId,
                content = trimmedContent,
                tags = post.tags
            )
            if (result.isFailure) {
                currentPosts = previousPosts.toMutableList()
                _uiState.value = CommunityUiState.Success(currentPosts.toList())
            }
        }
    }

    fun deletePost(post: CommunityPost) {
        val userId = auth.currentUser?.uid ?: return
        if (!canManagePost(post)) return

        val previousPosts = currentPosts.toList()
        val previousComments = _commentsMap.value
        currentPosts = currentPosts.filterNot { it.id == post.id }.toMutableList()
        _uiState.value = CommunityUiState.Success(currentPosts.toList())
        _commentsMap.value = _commentsMap.value - post.id

        viewModelScope.launch {
            val result = repository.deletePost(postId = post.id, authorId = userId)
            if (result.isFailure) {
                currentPosts = previousPosts.toMutableList()
                _uiState.value = CommunityUiState.Success(currentPosts.toList())
                _commentsMap.value = previousComments
            }
        }
    }

    fun createStudyGroup(
        name: String,
        description: String,
        focusLevel: String,
        isPrivate: Boolean
    ) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return

        val createdGroup = StudyGroup(
            id = "local-${System.currentTimeMillis()}",
            name = trimmedName,
            description = description.trim().ifBlank { "Nhóm học riêng để chia sẻ tài liệu và cùng nhau ôn tập." },
            focusLevel = focusLevel,
            weeklyGoal = "Hoàn thành 3 buổi học nhóm trong tuần",
            privacy = if (isPrivate) StudyGroupPrivacy.PRIVATE else StudyGroupPrivacy.PUBLIC,
            memberCount = 1,
            onlineCount = 1,
            accentColorHex = "#26A69A",
            coverLabel = focusLevel,
            isJoinedByMe = true,
            membersPreview = listOf(currentUserName()),
            resources = emptyList(),
            recentNotes = listOf("Bạn vừa tạo nhóm học tập này.")
        )

        _studyGroups.value = listOf(createdGroup) + _studyGroups.value
    }

    fun joinStudyGroup(groupId: String) {
        _studyGroups.value = _studyGroups.value.map { group ->
            if (group.id == groupId && !group.isJoinedByMe) {
                group.copy(
                    isJoinedByMe = true,
                    memberCount = group.memberCount + 1,
                    membersPreview = (listOf(currentUserName()) + group.membersPreview).distinct().take(4),
                    recentNotes = listOf("${currentUserName()} đã tham gia nhóm.") + group.recentNotes
                )
            } else {
                group
            }
        }
    }

    fun sendGroupMessage(groupId: String, content: String) {
        val trimmedContent = content.trim()
        if (trimmedContent.isBlank()) return

        val currentUser = auth.currentUser
        val message = StudyMessage(
            id = "message-${System.currentTimeMillis()}",
            senderName = currentUserName(),
            senderEmail = currentUser?.email.orEmpty(),
            content = trimmedContent,
            timestamp = System.currentTimeMillis(),
            isMine = true
        )

        val currentMessages = _groupMessages.value[groupId].orEmpty()
        _groupMessages.value = _groupMessages.value + (groupId to (currentMessages + message))

        _studyGroups.value = _studyGroups.value.map { group ->
            if (group.id == groupId) {
                group.copy(
                    unreadCount = 0,
                    recentNotes = listOf("${currentUserName()}: $trimmedContent") + group.recentNotes
                )
            } else {
                group
            }
        }
    }

    fun inviteToStudyGroup(groupId: String, email: String) {
        val normalizedEmail = email.trim().lowercase()
        if (normalizedEmail.isBlank() || !normalizedEmail.contains("@")) return

        _studyGroups.value = _studyGroups.value.map { group ->
            if (group.id == groupId && normalizedEmail !in group.pendingInviteEmails && normalizedEmail !in group.membersPreview) {
                group.copy(
                    pendingInviteEmails = group.pendingInviteEmails + normalizedEmail,
                    recentNotes = listOf("Đã gửi lời mời đến $normalizedEmail.") + group.recentNotes
                )
            } else {
                group
            }
        }
    }

    fun acceptStudyGroupInvite(groupId: String, email: String) {
        val normalizedEmail = email.trim().lowercase()
        _studyGroups.value = _studyGroups.value.map { group ->
            if (group.id == groupId && normalizedEmail in group.pendingInviteEmails) {
                group.copy(
                    pendingInviteEmails = group.pendingInviteEmails - normalizedEmail,
                    membersPreview = (group.membersPreview + normalizedEmail.substringBefore("@")).distinct().take(4),
                    memberCount = group.memberCount + 1,
                    recentNotes = listOf("$normalizedEmail đã chấp nhận lời mời.") + group.recentNotes
                )
            } else {
                group
            }
        }
    }

    fun addSharedResource(
        groupId: String,
        title: String,
        description: String,
        type: SharedResourceType
    ) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isBlank()) return

        val ownerName = currentUserName()
        val newResource = SharedStudyResource(
            id = "resource-${System.currentTimeMillis()}",
            title = trimmedTitle,
            description = description.trim(),
            type = type,
            ownerName = ownerName,
            updatedAt = System.currentTimeMillis(),
            itemCount = when (type) {
                SharedResourceType.POST -> 1
                SharedResourceType.FOLDER -> 12
                SharedResourceType.FLASHCARD -> 24
            }
        )

        _studyGroups.value = _studyGroups.value.map { group ->
            if (group.id == groupId) {
                group.copy(
                    unreadCount = group.unreadCount + 1,
                    resources = listOf(newResource) + group.resources,
                    recentNotes = listOf("$ownerName đã chia sẻ ${type.label.lowercase()} mới.") + group.recentNotes
                )
            } else {
                group
            }
        }
    }

    private fun currentUserName(): String {
        return auth.currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "Người học"
    }

    class Factory(private val repository: CommunityRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CommunityViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CommunityViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    private companion object {
        const val PAGE_SIZE = 20L

        fun seedStudyGroups(): List<StudyGroup> {
            val now = System.currentTimeMillis()
            return listOf(
                StudyGroup(
                    id = "n4-sprint",
                    name = "N4 Sprint cùng nhau",
                    description = "Nhóm riêng cho lịch ôn N4: chia sẻ đề, flashcard và ghi chú sau mỗi buổi học.",
                    focusLevel = "N4",
                    weeklyGoal = "Ôn 45 mẫu ngữ pháp và 180 từ mới",
                    privacy = StudyGroupPrivacy.PRIVATE,
                    memberCount = 12,
                    onlineCount = 4,
                    unreadCount = 5,
                    accentColorHex = "#E53935",
                    coverLabel = "N4",
                    isJoinedByMe = true,
                    membersPreview = listOf("Minh Anh", "Haru", "Tuấn", "Lan"),
                    resources = listOf(
                        SharedStudyResource(
                            id = "n4-flashcard",
                            title = "Flashcard từ vựng tuần 3",
                            description = "Kanji dễ nhầm và ví dụ ngắn để ôn nhanh trước quiz.",
                            type = SharedResourceType.FLASHCARD,
                            ownerName = "Haru",
                            updatedAt = now - 1000L * 60L * 35L,
                            itemCount = 64
                        ),
                        SharedStudyResource(
                            id = "n4-folder",
                            title = "Thư mục đề luyện đọc N4",
                            description = "Bài đọc theo chủ đề đời sống, có đáp án và từ khóa.",
                            type = SharedResourceType.FOLDER,
                            ownerName = "Minh Anh",
                            updatedAt = now - 1000L * 60L * 60L * 4L,
                            itemCount = 8
                        )
                    ),
                    recentNotes = listOf(
                        "Haru đã thêm flashcard từ vựng tuần 3.",
                        "Lan ghim lịch học tối thứ 4 và thứ 7."
                    )
                ),
                StudyGroup(
                    id = "kaiwa-room",
                    name = "Kaiwa mỗi tối",
                    description = "Luyện hội thoại 15 phút mỗi ngày, ưu tiên mẫu câu ngắn và phản xạ tự nhiên.",
                    focusLevel = "N5-N3",
                    weeklyGoal = "5 phiên luyện nói và 1 bài recap",
                    privacy = StudyGroupPrivacy.PRIVATE,
                    memberCount = 8,
                    onlineCount = 2,
                    unreadCount = 2,
                    accentColorHex = "#5E6AD2",
                    coverLabel = "会話",
                    isJoinedByMe = true,
                    membersPreview = listOf("An", "Yuki", "Bảo"),
                    resources = listOf(
                        SharedStudyResource(
                            id = "kaiwa-post",
                            title = "Mẫu câu đặt lịch hẹn",
                            description = "Bài viết ngắn kèm cách trả lời lịch sự.",
                            type = SharedResourceType.POST,
                            ownerName = "Yuki",
                            updatedAt = now - 1000L * 60L * 90L,
                            itemCount = 1
                        )
                    ),
                    recentNotes = listOf("Yuki chia sẻ bài viết mẫu câu đặt lịch hẹn.")
                ),
                StudyGroup(
                    id = "kanji-board",
                    name = "Kanji Board",
                    description = "Cùng gom thư mục Hán tự theo bộ thủ, ví dụ và mẹo ghi nhớ.",
                    focusLevel = "N5-N2",
                    weeklyGoal = "Mỗi người góp 20 thẻ Kanji chất lượng",
                    privacy = StudyGroupPrivacy.PUBLIC,
                    memberCount = 36,
                    onlineCount = 7,
                    accentColorHex = "#F59E0B",
                    coverLabel = "漢字",
                    isJoinedByMe = false,
                    membersPreview = listOf("Khoa", "Mika", "Nhi"),
                    resources = listOf(
                        SharedStudyResource(
                            id = "kanji-folder",
                            title = "Bộ thủ thường gặp",
                            description = "Thư mục tổng hợp 18 bộ thủ nền tảng.",
                            type = SharedResourceType.FOLDER,
                            ownerName = "Khoa",
                            updatedAt = now - 1000L * 60L * 60L * 12L,
                            itemCount = 18
                        )
                    ),
                    recentNotes = listOf("Khoa cập nhật thư mục bộ thủ thường gặp.")
                )
            )
        }

        fun seedGroupMessages(): Map<String, List<StudyMessage>> {
            val now = System.currentTimeMillis()
            return mapOf(
                "n4-sprint" to listOf(
                    StudyMessage(
                        id = "n4-m1",
                        senderName = "Haru",
                        senderEmail = "haru@example.com",
                        content = "Tối nay mình ôn てしまう với ておく nhé.",
                        timestamp = now - 1000L * 60L * 42L
                    ),
                    StudyMessage(
                        id = "n4-m2",
                        senderName = "Minh Anh",
                        senderEmail = "minhanh@example.com",
                        content = "Mình vừa share flashcard tuần 3, ai rảnh vào quiz thử nha.",
                        timestamp = now - 1000L * 60L * 28L
                    )
                ),
                "kaiwa-room" to listOf(
                    StudyMessage(
                        id = "kaiwa-m1",
                        senderName = "Yuki",
                        senderEmail = "yuki@example.com",
                        content = "Hôm nay luyện đặt lịch hẹn, mỗi người gửi 2 câu mẫu.",
                        timestamp = now - 1000L * 60L * 72L
                    )
                ),
                "kanji-board" to listOf(
                    StudyMessage(
                        id = "kanji-m1",
                        senderName = "Khoa",
                        senderEmail = "khoa@example.com",
                        content = "Mình đang gom kanji theo bộ thủ, mọi người góp thêm ví dụ nha.",
                        timestamp = now - 1000L * 60L * 120L
                    )
                )
            )
        }
    }
}

private fun CommunityPost.withLikeState(userId: String?): CommunityPost {
    val uniqueLikedBy = likedBy.distinct()
    val normalizedLikesCount = if (uniqueLikedBy.isNotEmpty()) {
        uniqueLikedBy.size
    } else {
        likesCount.coerceAtLeast(0)
    }
    return copy(
        likedBy = uniqueLikedBy,
        likesCount = normalizedLikesCount,
        isLikedByMe = userId != null && userId in uniqueLikedBy
    )
}

private fun CommunityPost.toggledLike(userId: String, wasLiked: Boolean): CommunityPost {
    val updatedLikedBy = if (wasLiked) {
        likedBy - userId
    } else {
        likedBy + userId
    }.distinct()

    return copy(
        likedBy = updatedLikedBy,
        likesCount = updatedLikedBy.size,
        isLikedByMe = !wasLiked
    )
}
