package com.example.kotobee.data.repository

import android.net.Uri
import com.example.kotobee.data.model.SpeakingPairHistory
import com.example.kotobee.data.model.SpeakingPairMessage
import com.example.kotobee.data.model.SpeakingPairParticipant
import com.example.kotobee.data.model.SpeakingPairRoom
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.random.Random

class SpeakingPairRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    companion object {
        private const val ROOM_COLLECTION = "speaking_pair_rooms"
        private const val PARTICIPANT_COLLECTION = "participants"
        private const val MESSAGE_COLLECTION = "messages"
        private const val HISTORY_COLLECTION = "speaking_pair_history"
        private const val ROOM_TTL_MS = 2 * 60 * 60 * 1000L
    }

    data class CurrentPairUser(
        val userDocId: String,
        val username: String,
        val avatarUrl: String
    )

    suspend fun getCurrentUser(): CurrentPairUser = withContext(Dispatchers.IO) {
        currentUserSnapshot().let { (docId, snapshot) ->
            CurrentPairUser(
                userDocId = docId,
                username = snapshot.firstString("username", "displayName", "name").ifBlank { docId },
                avatarUrl = snapshot.firstString("avatarUrl", "avatar_url", "photoUrl")
            )
        }
    }

    suspend fun getHistory(): List<SpeakingPairHistory> = withContext(Dispatchers.IO) {
        val user = getCurrentUser()
        firestore.collection("users")
            .document(user.userDocId)
            .collection(HISTORY_COLLECTION)
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(SpeakingPairHistory::class.java) }
            .sortedByDescending { it.updatedAt }
            .take(12)
    }

    suspend fun createRoom(): String = withContext(Dispatchers.IO) {
        val user = getCurrentUser()
        val code = generateRoomCode()
        val now = System.currentTimeMillis()
        val room = SpeakingPairRoom(
            code = code,
            topicId = "free_talk",
            topicTitle = "Phòng tự do",
            level = "",
            scenario = "Hai người tự chọn chủ đề và nói chuyện luân phiên bằng tiếng Nhật.",
            hostUserId = user.userDocId,
            status = "waiting",
            currentTurnUserId = user.userDocId,
            turnIndex = 0,
            createdAt = now,
            updatedAt = now,
            expiresAt = now + ROOM_TTL_MS
        )
        val participant = user.toParticipant(joinedAt = now, isHost = true)
        val roomRef = firestore.collection(ROOM_COLLECTION).document(code)

        roomRef.set(room).await()
        roomRef.collection(PARTICIPANT_COLLECTION).document(user.userDocId).set(participant).await()
        saveHistories(room, listOf(participant))
        code
    }

    suspend fun joinRoom(code: String): String = withContext(Dispatchers.IO) {
        val normalizedCode = code.trim().uppercase()
        if (normalizedCode.length != 6) error("Mã phòng phải có 6 ký tự.")

        val user = getCurrentUser()
        val roomRef = firestore.collection(ROOM_COLLECTION).document(normalizedCode)
        val room = roomRef.get().await().toObject(SpeakingPairRoom::class.java)
            ?: error("Không tìm thấy phòng.")
        if (room.status != "waiting") error("Phòng này đã bắt đầu hoặc đã kết thúc.")
        if (room.expiresAt > 0 && room.expiresAt < System.currentTimeMillis()) error("Phòng này đã hết hạn.")

        val participants = getParticipants(normalizedCode)
        val alreadyJoined = participants.any { it.userId == user.userDocId }
        if (!alreadyJoined && participants.size >= 2) error("Phòng đã đủ 2 người.")

        val now = System.currentTimeMillis()
        val participant = user.toParticipant(joinedAt = now, isHost = user.userDocId == room.hostUserId)
        roomRef.collection(PARTICIPANT_COLLECTION)
            .document(user.userDocId)
            .set(participant, SetOptions.merge())
            .await()

        val updatedParticipants = getParticipants(normalizedCode)
        if (updatedParticipants.size >= 2) {
            roomRef.set(
                mapOf(
                    "status" to "active",
                    "updatedAt" to now
                ),
                SetOptions.merge()
            ).await()
        }
        val updatedRoom = roomRef.get().await().toObject(SpeakingPairRoom::class.java) ?: room
        saveHistories(updatedRoom, updatedParticipants)
        normalizedCode
    }

    suspend fun openRoom(code: String): String = withContext(Dispatchers.IO) {
        val normalizedCode = code.trim().uppercase()
        val user = getCurrentUser()
        val roomRef = firestore.collection(ROOM_COLLECTION).document(normalizedCode)
        roomRef.get().await().toObject(SpeakingPairRoom::class.java) ?: error("Không tìm thấy phòng.")
        if (!roomRef.collection(PARTICIPANT_COLLECTION).document(user.userDocId).get().await().exists()) {
            error("Bạn không thuộc phòng này.")
        }
        normalizedCode
    }

    suspend fun leaveRoom(code: String) = withContext(Dispatchers.IO) {
        val user = getCurrentUser()
        val roomRef = firestore.collection(ROOM_COLLECTION).document(code)
        val room = roomRef.get().await().toObject(SpeakingPairRoom::class.java) ?: return@withContext
        val now = System.currentTimeMillis()

        when (room.status) {
            "waiting" -> {
                if (room.hostUserId == user.userDocId) {
                    roomRef.set(
                        mapOf(
                            "status" to "cancelled",
                            "endedReason" to "host_left",
                            "updatedAt" to now
                        ),
                        SetOptions.merge()
                    ).await()
                } else {
                    roomRef.collection(PARTICIPANT_COLLECTION).document(user.userDocId).delete().await()
                }
            }
            "active" -> {
                roomRef.collection(PARTICIPANT_COLLECTION)
                    .document(user.userDocId)
                    .set(mapOf("isOnline" to false), SetOptions.merge())
                    .await()
                roomRef.set(
                    mapOf(
                        "status" to "finished",
                        "endedReason" to "participant_left",
                        "updatedAt" to now
                    ),
                    SetOptions.merge()
                ).await()
            }
            else -> {
                roomRef.collection(PARTICIPANT_COLLECTION)
                    .document(user.userDocId)
                    .set(mapOf("isOnline" to false), SetOptions.merge())
                    .await()
            }
        }

        val updatedRoom = roomRef.get().await().toObject(SpeakingPairRoom::class.java) ?: room
        saveHistories(updatedRoom, getParticipants(code))
    }

    suspend fun finishRoom(code: String) = withContext(Dispatchers.IO) {
        val roomRef = firestore.collection(ROOM_COLLECTION).document(code)
        val now = System.currentTimeMillis()
        roomRef.set(
            mapOf(
                "status" to "finished",
                "endedReason" to "manual",
                "updatedAt" to now
            ),
            SetOptions.merge()
        ).await()
        val room = roomRef.get().await().toObject(SpeakingPairRoom::class.java) ?: return@withContext
        saveHistories(room, getParticipants(code))
    }

    suspend fun submitMessage(
        code: String,
        audioFile: File,
        transcriptJa: String,
        durationMs: Long
    ): Unit = withContext(Dispatchers.IO) {
        val user = getCurrentUser()
        val roomRef = firestore.collection(ROOM_COLLECTION).document(code)
        val room = roomRef.get().await().toObject(SpeakingPairRoom::class.java)
            ?: error("Không tìm thấy phòng.")
        val participants = getParticipants(code)
        if (room.status != "active") error("Phòng chưa sẵn sàng để gửi lượt nói.")
        if (room.currentTurnUserId != user.userDocId) error("Chưa đến lượt của bạn.")
        if (participants.size < 2 || participants.any { !it.isOnline }) error("Người học còn lại đã rời phòng.")

        val messageId = roomRef.collection(MESSAGE_COLLECTION).document().id
        val audioRef = storage.reference.child("$ROOM_COLLECTION/$code/$messageId.m4a")
        val uploadSnapshot = audioRef.putFile(Uri.fromFile(audioFile)).await()
        val audioUrl = uploadSnapshot.storage.downloadUrl.await().toString()
        val now = System.currentTimeMillis()

        firestore.runTransaction { transaction ->
            val latestRoom = transaction.get(roomRef).toObject(SpeakingPairRoom::class.java)
                ?: error("Không tìm thấy phòng.")
            if (latestRoom.status != "active") error("Phòng đã kết thúc.")
            if (latestRoom.currentTurnUserId != user.userDocId) error("Chưa đến lượt của bạn.")

            val latestParticipants = participants
            val nextUserId = latestParticipants.firstOrNull { it.userId != user.userDocId && it.isOnline }?.userId
                ?: error("Người học còn lại đã rời phòng.")
            val message = SpeakingPairMessage(
                id = messageId,
                senderUserId = user.userDocId,
                senderName = user.username,
                audioUrl = audioUrl,
                transcriptJa = transcriptJa.trim(),
                turnIndex = latestRoom.turnIndex,
                durationMs = durationMs,
                createdAt = now
            )

            transaction.set(roomRef.collection(MESSAGE_COLLECTION).document(messageId), message)
            transaction.update(
                roomRef,
                mapOf(
                    "currentTurnUserId" to nextUserId,
                    "turnIndex" to latestRoom.turnIndex + 1,
                    "messageCount" to latestRoom.messageCount + 1,
                    "updatedAt" to now
                )
            )
            null
        }.await()

        val updatedRoom = roomRef.get().await().toObject(SpeakingPairRoom::class.java) ?: room
        saveHistories(updatedRoom, getParticipants(code))
    }

    fun observeRoom(code: String, onChange: (SpeakingPairRoom?) -> Unit, onError: (Exception) -> Unit): ListenerRegistration {
        return firestore.collection(ROOM_COLLECTION).document(code)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                onChange(snapshot?.toObject(SpeakingPairRoom::class.java))
            }
    }

    fun observeParticipants(code: String, onChange: (List<SpeakingPairParticipant>) -> Unit, onError: (Exception) -> Unit): ListenerRegistration {
        return firestore.collection(ROOM_COLLECTION).document(code)
            .collection(PARTICIPANT_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                onChange(
                    snapshot?.documents
                        ?.mapNotNull { it.toObject(SpeakingPairParticipant::class.java) }
                        ?.sortedBy { it.joinedAt }
                        .orEmpty()
                )
            }
    }

    fun observeMessages(code: String, onChange: (List<SpeakingPairMessage>) -> Unit, onError: (Exception) -> Unit): ListenerRegistration {
        return firestore.collection(ROOM_COLLECTION).document(code)
            .collection(MESSAGE_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                onChange(
                    snapshot?.documents
                        ?.mapNotNull { it.toObject(SpeakingPairMessage::class.java)?.copy(id = it.id) }
                        ?.sortedWith(compareBy<SpeakingPairMessage> { it.turnIndex }.thenBy { it.createdAt })
                        .orEmpty()
                )
            }
    }

    private suspend fun getParticipants(code: String): List<SpeakingPairParticipant> {
        return firestore.collection(ROOM_COLLECTION).document(code)
            .collection(PARTICIPANT_COLLECTION)
            .get()
            .await()
            .toObjects(SpeakingPairParticipant::class.java)
            .sortedBy { it.joinedAt }
    }

    private suspend fun saveHistories(room: SpeakingPairRoom, participants: List<SpeakingPairParticipant>) {
        participants.forEach { participant ->
            val partnerName = participants.firstOrNull { it.userId != participant.userId }?.username.orEmpty()
            val history = SpeakingPairHistory(
                roomCode = room.code,
                topicTitle = room.topicTitle,
                partnerName = partnerName,
                status = room.status,
                messageCount = room.messageCount,
                updatedAt = room.updatedAt
            )
            firestore.collection("users")
                .document(participant.userId)
                .collection(HISTORY_COLLECTION)
                .document(room.code)
                .set(history, SetOptions.merge())
                .await()
        }
    }

    private suspend fun currentUserSnapshot(): Pair<String, DocumentSnapshot> {
        val email = auth.currentUser?.email ?: error("Vui lòng đăng nhập để tạo phòng giao tiếp.")
        val snapshot = firestore.collection("users")
            .whereEqualTo("email", email)
            .limit(1)
            .get()
            .await()
        val document = snapshot.documents.firstOrNull() ?: error("Không tìm thấy hồ sơ người dùng.")
        return document.id to document
    }

    private suspend fun generateRoomCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        repeat(40) {
            val code = buildString {
                repeat(6) { append(chars[Random.nextInt(chars.length)]) }
            }
            val exists = firestore.collection(ROOM_COLLECTION).document(code).get().await().exists()
            if (!exists) return code
        }
        error("Không thể tạo mã phòng, vui lòng thử lại.")
    }

    private fun CurrentPairUser.toParticipant(joinedAt: Long, isHost: Boolean): SpeakingPairParticipant {
        return SpeakingPairParticipant(
            userId = userDocId,
            username = username,
            avatarUrl = avatarUrl,
            joinedAt = joinedAt,
            isHost = isHost,
            isOnline = true
        )
    }

    private fun DocumentSnapshot.firstString(vararg fields: String): String {
        return fields.firstNotNullOfOrNull { field ->
            getString(field)?.trim()?.takeIf { it.isNotBlank() }
        }.orEmpty()
    }
}
