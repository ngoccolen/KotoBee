package com.example.kotobee.data.repository

import com.example.kotobee.model.Comment
import com.example.kotobee.model.CommunityPost
import com.example.kotobee.model.StudyLeaderboardEntry
import com.example.kotobee.model.StudyLeaderboards
import com.example.kotobee.util.StudyActivityTracker
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

    override suspend fun getStudyLeaderboards(limit: Int): StudyLeaderboards {
        val todayKey = StudyActivityTracker.todayDateKey()
        val weekKeys = StudyActivityTracker.currentWeekDateKeys()
        val monthKeys = StudyActivityTracker.currentMonthDateKeys()

        val entries = firestore.collection("users")
            .get()
            .await()
            .documents
            .map { userDoc ->
                var todayPoints = 0
                var weeklyPoints = 0
                var monthlyPoints = 0

                val activitySnapshot = userDoc.reference
                    .collection("study_activity")
                    .get()
                    .await()

                activitySnapshot.documents.forEach { activityDoc ->
                    val date = activityDoc.getString("date") ?: activityDoc.id
                    val points = activityDoc.numberValue("value", "points")

                    if (date == todayKey) todayPoints += points
                    if (date in weekKeys) weeklyPoints += points
                    if (date in monthKeys) monthlyPoints += points
                }

                StudyLeaderboardEntry(
                    userId = userDoc.getString("uid") ?: userDoc.id,
                    username = userDoc.getString("username")
                        ?: userDoc.getString("displayName")
                        ?: userDoc.getString("email")?.substringBefore("@")
                        ?: "Người học",
                    avatarUrl = userDoc.getString("avatarUrl") ?: userDoc.getString("avatar_url") ?: "",
                    jlptLevel = userDoc.getString("jlpt_level") ?: userDoc.getString("current_level") ?: "N5",
                    streak = userDoc.numberValue("streak"),
                    todayPoints = todayPoints,
                    weeklyPoints = weeklyPoints,
                    monthlyPoints = monthlyPoints
                )
            }

        return StudyLeaderboards(
            daily = entries
                .filter { it.todayPoints > 0 }
                .sortedByDescending { it.todayPoints }
                .take(limit),
            weekly = entries
                .filter { it.weeklyPoints > 0 }
                .sortedByDescending { it.weeklyPoints }
                .take(limit),
            monthly = entries
                .filter { it.monthlyPoints > 0 }
                .sortedByDescending { it.monthlyPoints }
                .take(limit)
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
