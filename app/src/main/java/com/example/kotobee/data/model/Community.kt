package com.example.kotobee.model

data class UserProfile(
    val uid: String = "",
    val username: String = "",
    val avatarUrl: String = "",
    val jlptLevel: String = "N5",
    val streak: Int = 0
)

data class CommunityPost(
    val id: String = "",
    val author: UserProfile = UserProfile(), // Gom nhóm User info
    val content: String = "",
    val imageUrls: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val updatedAt: Long = 0L,
    val likesCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val commentsCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val sharedDeck: DeckSnapshot? = null
)

// Dùng snapshot cho Feed để nhẹ hơn Deck gốc
data class DeckSnapshot(
    val deckId: String = "",
    val name: String = "",
    val wordCount: Int = 0
)

enum class StudyGroupPrivacy {
    PRIVATE,
    PUBLIC
}

enum class SharedResourceType(val label: String) {
    POST("Bài viết"),
    FOLDER("Thư mục"),
    FLASHCARD("Flashcard")
}

data class SharedStudyResource(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val type: SharedResourceType = SharedResourceType.POST,
    val ownerName: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val itemCount: Int = 0
)

data class StudyMessage(
    val id: String = "",
    val senderName: String = "",
    val senderEmail: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isMine: Boolean = false
)

data class StudyGroup(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val focusLevel: String = "N5",
    val weeklyGoal: String = "",
    val privacy: StudyGroupPrivacy = StudyGroupPrivacy.PRIVATE,
    val memberCount: Int = 0,
    val onlineCount: Int = 0,
    val unreadCount: Int = 0,
    val accentColorHex: String = "#E53935",
    val coverLabel: String = "",
    val isJoinedByMe: Boolean = false,
    val membersPreview: List<String> = emptyList(),
    val pendingInviteEmails: List<String> = emptyList(),
    val resources: List<SharedStudyResource> = emptyList(),
    val recentNotes: List<String> = emptyList()
)

data class StudyLeaderboardEntry(
    val userId: String = "",
    val username: String = "",
    val avatarUrl: String = "",
    val jlptLevel: String = "N5",
    val streak: Int = 0,
    val totalPoints: Int = 0,
    val isCurrentUser: Boolean = false
)

data class StudyLeaderboards(
    val entries: List<StudyLeaderboardEntry> = emptyList(),
    val currentUserEntry: StudyLeaderboardEntry? = null
)
