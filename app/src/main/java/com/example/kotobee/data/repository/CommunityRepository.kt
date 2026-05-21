package com.example.kotobee.data.repository

import com.example.kotobee.model.Comment
import com.example.kotobee.model.CommunityPost
import com.example.kotobee.model.StudyLeaderboards
import kotlinx.coroutines.flow.Flow

interface CommunityRepository {
    fun getCommunityFeed(limit: Long = 20): Flow<List<CommunityPost>>
    suspend fun loadMorePosts(limit: Long = 20): List<CommunityPost>
    suspend fun toggleLike(postId: String, userId: String, isLiked: Boolean): Result<Unit>
    suspend fun shareDeckWithEmail(deckId: String, friendEmail: String): Result<Unit>
    suspend fun createPost(post: CommunityPost): Result<Unit>
    suspend fun updatePost(postId: String, authorId: String, content: String, tags: List<String> = emptyList()): Result<Unit>
    suspend fun deletePost(postId: String, authorId: String): Result<Unit>
    suspend fun getStudyLeaderboards(limit: Int = 10, currentUserEmail: String? = null): StudyLeaderboards

    // Lấy danh sách bình luận (Realtime Flow)
    fun getComments(postId: String): Flow<List<Comment>>

    // Đã thêm username để lúc hiển thị biết ai là người comment
    suspend fun addComment(
        postId: String,
        userId: String,
        username: String,
        content: String
    ): Result<Unit>
}
