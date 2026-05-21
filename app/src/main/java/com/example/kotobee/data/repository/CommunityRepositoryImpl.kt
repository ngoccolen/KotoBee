package com.example.kotobee.data.repository

import com.example.kotobee.model.Comment
import com.example.kotobee.model.CommunityPost
import com.example.kotobee.model.StudyLeaderboardEntry
import com.example.kotobee.model.StudyLeaderboards
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CommunityRepositoryImpl(
    private val firestore: FirebaseFirestore
) : CommunityRepository {

    private var lastVisibleDocument: DocumentSnapshot? = null

    override fun getCommunityFeed(limit: Long): Flow<List<CommunityPost>> = callbackFlow {
        val query = firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                val posts = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(CommunityPost::class.java)?.copy(id = doc.id)
                }
                lastVisibleDocument = snapshot.documents.lastOrNull()
                trySend(posts)
            } else {
                trySend(emptyList())
            }
        }
        awaitClose { listener.remove() }
    }

    override suspend fun loadMorePosts(limit: Long): List<CommunityPost> {
        return try {
            val query = firestore.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisibleDocument)
                .limit(limit)
                .get()
                .await()

            if (!query.isEmpty) {
                lastVisibleDocument = query.documents.lastOrNull()
                query.documents.mapNotNull { doc ->
                    doc.toObject(CommunityPost::class.java)?.copy(id = doc.id)
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun createPost(post: CommunityPost): Result<Unit> {
        return try {
            firestore.collection("posts").add(post).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePost(
        postId: String,
        authorId: String,
        content: String,
        tags: List<String>
    ): Result<Unit> {
        return runCatching {
            require(postId.isNotBlank()) { "Bài viết không hợp lệ" }
            require(authorId.isNotBlank()) { "Bạn cần đăng nhập để chỉnh sửa bài viết" }

            val postRef = firestore.collection("posts").document(postId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                if (!snapshot.exists()) {
                    throw IllegalStateException("Bài viết không còn tồn tại")
                }
                if (snapshot.postAuthorUid() != authorId) {
                    throw SecurityException("Bạn chỉ có thể chỉnh sửa bài viết của mình")
                }

                transaction.update(
                    postRef,
                    mapOf(
                        "content" to content,
                        "tags" to tags,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
            }.await()
        }
    }

    override suspend fun deletePost(postId: String, authorId: String): Result<Unit> {
        return runCatching {
            require(postId.isNotBlank()) { "Bài viết không hợp lệ" }
            require(authorId.isNotBlank()) { "Bạn cần đăng nhập để xóa bài viết" }

            val postRef = firestore.collection("posts").document(postId)
            val snapshot = postRef.get().await()
            if (!snapshot.exists()) {
                throw IllegalStateException("Bài viết không còn tồn tại")
            }
            if (snapshot.postAuthorUid() != authorId) {
                throw SecurityException("Bạn chỉ có thể xóa bài viết của mình")
            }

            val comments = postRef.collection("comments").get().await().documents
            comments.chunked(450).forEach { chunk ->
                val batch = firestore.batch()
                chunk.forEach { comment -> batch.delete(comment.reference) }
                batch.commit().await()
            }
            postRef.delete().await()
        }
    }

    override suspend fun getStudyLeaderboards(limit: Int, currentUserEmail: String?): StudyLeaderboards {
        val safeLimit = limit.coerceAtLeast(1)
        val normalizedCurrentUserEmail = currentUserEmail?.trim()?.takeIf { it.isNotBlank() }

        val fastEntries = firestore.collection("users")
            .orderBy("totalStudyPoints", Query.Direction.DESCENDING)
            .limit(safeLimit.toLong())
            .get()
            .await()
            .documents
            .map { userDoc ->
                userDoc.toLeaderboardEntry(
                    totalPoints = userDoc.numberValue("totalStudyPoints"),
                    currentUserEmail = normalizedCurrentUserEmail
                )
            }

        val fallbackEntries = if (fastEntries.count { it.totalPoints > 0 } < safeLimit) {
            loadLegacyLeaderboardEntries(normalizedCurrentUserEmail)
        } else {
            emptyList()
        }
        val currentUserEntry = loadCurrentUserLeaderboardEntry(normalizedCurrentUserEmail)
        val candidates = fastEntries + fallbackEntries + listOfNotNull(currentUserEntry)
        val currentUserId = candidates.firstOrNull { it.isCurrentUser }?.userId

        return buildStudyLeaderboards(
            candidates = candidates,
            currentUserId = currentUserId,
            limit = safeLimit
        )
    }

    override suspend fun toggleLike(postId: String, userId: String, isLiked: Boolean): Result<Unit> {
        return runCatching {
            val postRef = firestore.collection("posts").document(postId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val currentLikedBy = (snapshot.get("likedBy") as? List<*>)
                    ?.mapNotNull { it as? String }
                    ?.toMutableSet()
                    ?: mutableSetOf()

                if (isLiked) {
                    currentLikedBy.remove(userId)
                } else {
                    currentLikedBy.add(userId)
                }

                val updatedLikedBy = currentLikedBy.toList()
                transaction.update(
                    postRef,
                    mapOf(
                        "likedBy" to updatedLikedBy,
                        "likesCount" to updatedLikedBy.size
                    )
                )
            }.await()
        }
    }

    override suspend fun shareDeckWithEmail(deckId: String, friendEmail: String): Result<Unit> {
        return try {
            val userSnapshot = firestore.collection("users")
                .whereEqualTo("email", friendEmail)
                .get()
                .await()

            if (userSnapshot.isEmpty) {
                return Result.failure(Exception("Không tìm thấy người dùng với email này!"))
            }

            val friendId = userSnapshot.documents.first().id

            firestore.collection("decks").document(deckId)
                .update("sharedWith", FieldValue.arrayUnion(friendId))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- LẤY BÌNH LUẬN REALTIME TỪ FIREBASE ---
    override fun getComments(postId: String): Flow<List<Comment>> = callbackFlow {
        val listener = firestore.collection("posts").document(postId).collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING) // Sắp xếp cũ trên, mới dưới
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val comments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Comment::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(comments)
            }
        awaitClose { listener.remove() }
    }

    // --- THÊM BÌNH LUẬN LÊN FIREBASE (ĐÃ SỬA LỖI SAI BẢNG) ---
    override suspend fun addComment(
        postId: String,
        userId: String,
        username: String,
        content: String
    ): Result<Unit> {
        return runCatching {
            val postRef = firestore.collection("posts").document(postId) // <-- Sửa ở đây

            val commentData = hashMapOf(
                "userId" to userId,
                "username" to username,
                "content" to content,
                "timestamp" to System.currentTimeMillis() // Đồng bộ key với Model Comment
            )

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val currentCount = snapshot.getLong("commentsCount") ?: 0L

                val commentRef = postRef.collection("comments").document()

                transaction.set(commentRef, commentData)
                transaction.update(postRef, "commentsCount", currentCount + 1)
            }.await()
        }
    }

    private suspend fun loadLegacyLeaderboardEntries(currentUserEmail: String?): List<StudyLeaderboardEntry> {
        return firestore.collection("users")
            .get()
            .await()
            .documents
            .map { userDoc ->
                userDoc.toLeaderboardEntry(
                    totalPoints = userDoc.totalStudyPointsForLeaderboard(),
                    currentUserEmail = currentUserEmail
                )
            }
    }

    private suspend fun loadCurrentUserLeaderboardEntry(currentUserEmail: String?): StudyLeaderboardEntry? {
        if (currentUserEmail.isNullOrBlank()) return null

        val userDoc = firestore.collection("users")
            .whereEqualTo("email", currentUserEmail)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?: return null

        return userDoc.toLeaderboardEntry(
            totalPoints = userDoc.totalStudyPointsForLeaderboard(readLegacyWhenStoredZero = true),
            currentUserEmail = currentUserEmail
        )
    }

    private suspend fun DocumentSnapshot.totalStudyPointsForLeaderboard(
        readLegacyWhenStoredZero: Boolean = false
    ): Int {
        val storedTotal = numberValueOrNull("totalStudyPoints")
        if (storedTotal != null && (storedTotal > 0 || !readLegacyWhenStoredZero)) {
            return storedTotal
        }

        val legacyTotal = reference.collection("study_activity")
            .get()
            .await()
            .documents
            .sumOf { it.numberValue("value", "points") }
        return maxOf(storedTotal ?: 0, legacyTotal)
    }

    private fun DocumentSnapshot.toLeaderboardEntry(
        totalPoints: Int,
        currentUserEmail: String?
    ): StudyLeaderboardEntry {
        return StudyLeaderboardEntry(
            userId = getString("uid") ?: id,
            username = getString("username")
                ?: getString("displayName")
                ?: getString("email")?.substringBefore("@")
                ?: "Người học",
            avatarUrl = firstString(
                "avatar_url",
                "avatarUrl",
                "photoUrl",
                "photo_url",
                "picture"
            ),
            jlptLevel = getString("jlpt_level") ?: getString("current_level") ?: "N5",
            streak = numberValue("streak"),
            totalPoints = totalPoints,
            isCurrentUser = currentUserEmail != null && getString("email")?.trim()?.equals(currentUserEmail, ignoreCase = true) == true
        )
    }
}

private fun DocumentSnapshot.firstString(vararg fields: String): String {
    fields.forEach { field ->
        getString(field)?.takeIf { it.isNotBlank() }?.let { return it }
    }
    return ""
}

private fun DocumentSnapshot.postAuthorUid(): String {
    val authorMap = get("author") as? Map<*, *>
    return getString("author.uid")
        ?: authorMap?.get("uid")?.toString()
        ?: ""
}

private fun DocumentSnapshot.numberValue(vararg fields: String): Int {
    fields.forEach { field ->
        when (val value = get(field)) {
            is Number -> return value.toInt()
            is String -> value.toIntOrNull()?.let { return it }
        }
    }
    return 0
}

private fun DocumentSnapshot.numberValueOrNull(vararg fields: String): Int? {
    fields.forEach { field ->
        when (val value = get(field)) {
            is Number -> return value.toInt()
            is String -> value.toIntOrNull()?.let { return it }
        }
    }
    return null
}
