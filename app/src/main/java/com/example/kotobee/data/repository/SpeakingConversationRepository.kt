package com.example.kotobee.data.repository

import com.example.kotobee.data.model.ConversationApiMessage
import com.example.kotobee.data.model.ConversationRequest
import com.example.kotobee.data.model.ConversationResponse
import com.example.kotobee.data.model.ConversationTopic
import com.example.kotobee.data.model.SpeakingConversation
import com.example.kotobee.data.model.SpeakingMessage
import com.example.kotobee.data.service.SpeakingApiService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SpeakingConversationRepository(
    private val apiService: SpeakingApiService,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    companion object {
        private const val TOPIC_COLLECTION = "conversation_topics"
        private const val CONVERSATION_COLLECTION = "speaking_conversations"
        private const val MESSAGE_COLLECTION = "messages"
    }

    suspend fun getTopics(): List<ConversationTopic> = withContext(Dispatchers.IO) {
        firestore.collection(TOPIC_COLLECTION)
            .get()
            .await()
            .documents
            .filter { it.isPublished() }
            .mapNotNull { it.toConversationTopic() }
            .sortedWith(compareBy<ConversationTopic> { levelRank(it.level) }.thenBy { it.sortOrder }.thenBy { it.title })
    }

    suspend fun getTopic(topicId: String): ConversationTopic? = withContext(Dispatchers.IO) {
        val direct = firestore.collection(TOPIC_COLLECTION)
            .document(topicId)
            .get()
            .await()

        if (direct.exists()) return@withContext direct.toConversationTopic()

        firestore.collection(TOPIC_COLLECTION)
            .whereEqualTo("topic_id", topicId)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toConversationTopic()
    }

    suspend fun loadLatestOrCreateConversation(topic: ConversationTopic): Pair<SpeakingConversation, List<SpeakingMessage>> {
        val userRef = currentUserReference() ?: error("Vui lòng đăng nhập để luyện giao tiếp.")
        val latest = latestConversation(userRef, topic.id)
        return if (latest != null) {
            latest to loadMessages(userRef, latest.id)
        } else {
            createConversation(topic)
        }
    }

    suspend fun createConversation(topic: ConversationTopic): Pair<SpeakingConversation, List<SpeakingMessage>> = withContext(Dispatchers.IO) {
        val userRef = currentUserReference() ?: error("Vui lòng đăng nhập để luyện giao tiếp.")
        val conversationRef = userRef.collection(CONVERSATION_COLLECTION).document()
        val starterText = topic.starterMessageJa.ifBlank { "こんにちは。今日は${topic.title}について話しましょう。" }
        val starterMessageRef = conversationRef.collection(MESSAGE_COLLECTION).document()

        val conversationData = mapOf(
            "topicId" to topic.id,
            "topicTitle" to topic.title,
            "level" to topic.level,
            "startedAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
            "lastMessageJa" to starterText,
            "messageCount" to 1
        )
        conversationRef.set(conversationData).await()

        val starterData = mapOf(
            "role" to "ai",
            "textJa" to starterText,
            "translationVi" to "",
            "feedbackVi" to topic.starterHintVi,
            "correctionJa" to "",
            "turnIndex" to 0,
            "source" to "topic_starter",
            "createdAt" to FieldValue.serverTimestamp()
        )
        starterMessageRef.set(starterData).await()

        val conversation = SpeakingConversation(
            id = conversationRef.id,
            topicId = topic.id,
            topicTitle = topic.title,
            level = topic.level,
            messageCount = 1,
            lastMessageJa = starterText
        )
        val starterMessage = SpeakingMessage(
            id = starterMessageRef.id,
            role = "ai",
            textJa = starterText,
            feedbackVi = topic.starterHintVi,
            turnIndex = 0,
            source = "topic_starter"
        )

        conversation to listOf(starterMessage)
    }

    suspend fun saveUserMessage(
        conversationId: String,
        textJa: String,
        turnIndex: Int,
        source: String = "speech_recognizer"
    ): SpeakingMessage = withContext(Dispatchers.IO) {
        val userRef = currentUserReference() ?: error("Vui lòng đăng nhập để luyện giao tiếp.")
        val conversationRef = userRef.collection(CONVERSATION_COLLECTION).document(conversationId)
        val messageRef = conversationRef.collection(MESSAGE_COLLECTION).document()
        val message = SpeakingMessage(
            id = messageRef.id,
            role = "user",
            textJa = textJa,
            turnIndex = turnIndex,
            source = source
        )

        messageRef.set(message.toFirestoreMap()).await()
        conversationRef.set(
            mapOf(
                "updatedAt" to FieldValue.serverTimestamp(),
                "lastMessageJa" to textJa,
                "messageCount" to FieldValue.increment(1)
            ),
            SetOptions.merge()
        ).await()

        message
    }

    suspend fun requestAiResponse(
        topic: ConversationTopic,
        userText: String,
        recentMessages: List<SpeakingMessage>
    ): ConversationResponse = withContext(Dispatchers.IO) {
        apiService.respondToConversation(
            ConversationRequest(
                topicId = topic.id,
                level = topic.level,
                topicTitle = topic.title,
                scenario = topic.scenario,
                aiRole = topic.aiRole,
                userText = userText,
                recentMessages = recentMessages
                    .takeLast(10)
                    .map { ConversationApiMessage(role = it.role, textJa = it.textJa) }
            )
        )
    }

    suspend fun saveAiMessage(
        conversationId: String,
        response: ConversationResponse,
        turnIndex: Int
    ): SpeakingMessage = withContext(Dispatchers.IO) {
        val userRef = currentUserReference() ?: error("Vui lòng đăng nhập để luyện giao tiếp.")
        val conversationRef = userRef.collection(CONVERSATION_COLLECTION).document(conversationId)
        val messageRef = conversationRef.collection(MESSAGE_COLLECTION).document()
        val message = SpeakingMessage(
            id = messageRef.id,
            role = "ai",
            textJa = response.replyTextJa,
            translationVi = response.translationVi.orEmpty(),
            feedbackVi = response.feedbackVi.orEmpty(),
            correctionJa = response.correctionJa.orEmpty(),
            turnIndex = turnIndex,
            source = "gemini_conversation"
        )

        messageRef.set(message.toFirestoreMap()).await()
        conversationRef.set(
            mapOf(
                "updatedAt" to FieldValue.serverTimestamp(),
                "lastMessageJa" to response.replyTextJa,
                "messageCount" to FieldValue.increment(1)
            ),
            SetOptions.merge()
        ).await()

        message
    }

    private suspend fun latestConversation(userRef: DocumentReference, topicId: String): SpeakingConversation? {
        return userRef.collection(CONVERSATION_COLLECTION)
            .whereEqualTo("topicId", topicId)
            .get()
            .await()
            .documents
            .maxByOrNull { it.timestampMillis("updatedAt", "startedAt") }
            ?.toSpeakingConversation()
    }

    private suspend fun loadMessages(userRef: DocumentReference, conversationId: String): List<SpeakingMessage> {
        return userRef.collection(CONVERSATION_COLLECTION)
            .document(conversationId)
            .collection(MESSAGE_COLLECTION)
            .get()
            .await()
            .documents
            .mapNotNull { it.toSpeakingMessage() }
            .sortedWith(compareBy<SpeakingMessage> { it.turnIndex }.thenBy { it.id })
    }

    private suspend fun currentUserReference(): DocumentReference? {
        val email = auth.currentUser?.email ?: return null
        val snapshot = firestore.collection("users")
            .whereEqualTo("email", email)
            .limit(1)
            .get()
            .await()
        return snapshot.documents.firstOrNull()?.reference
    }

    private fun DocumentSnapshot.toConversationTopic(): ConversationTopic? {
        val id = firstString("topic_id", "id").ifBlank { this.id }
        val title = firstString("title")
        if (id.isBlank() || title.isBlank()) return null

        return ConversationTopic(
            id = id,
            title = title,
            level = firstString("level").ifBlank { "N5" },
            description = firstString("description"),
            scenario = firstString("scenario"),
            aiRole = firstString("ai_role", "aiRole"),
            starterMessageJa = firstString("starter_message_ja", "starterMessageJa"),
            starterHintVi = firstString("starter_hint_vi", "starterHintVi"),
            sortOrder = numberValue("sort_order") ?: Int.MAX_VALUE,
            tags = stringList("tags")
        )
    }

    private fun DocumentSnapshot.toSpeakingConversation(): SpeakingConversation {
        return SpeakingConversation(
            id = id,
            topicId = firstString("topicId", "topic_id"),
            topicTitle = firstString("topicTitle", "topic_title"),
            level = firstString("level").ifBlank { "N5" },
            messageCount = numberValue("messageCount", "message_count") ?: 0,
            lastMessageJa = firstString("lastMessageJa", "last_message_ja")
        )
    }

    private fun DocumentSnapshot.toSpeakingMessage(): SpeakingMessage? {
        val text = firstString("textJa", "text_ja")
        if (text.isBlank()) return null

        return SpeakingMessage(
            id = id,
            role = firstString("role").ifBlank { "user" },
            textJa = text,
            translationVi = firstString("translationVi", "translation_vi"),
            feedbackVi = firstString("feedbackVi", "feedback_vi"),
            correctionJa = firstString("correctionJa", "correction_ja"),
            turnIndex = numberValue("turnIndex", "turn_index") ?: 0,
            source = firstString("source").ifBlank { "conversation" }
        )
    }

    private fun SpeakingMessage.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "role" to role,
            "textJa" to textJa,
            "translationVi" to translationVi,
            "feedbackVi" to feedbackVi,
            "correctionJa" to correctionJa,
            "turnIndex" to turnIndex,
            "source" to source,
            "createdAt" to FieldValue.serverTimestamp()
        )
    }

    private fun DocumentSnapshot.isPublished(): Boolean {
        val status = firstString("status")
        return status.isBlank() || status == "published"
    }

    private fun DocumentSnapshot.firstString(vararg fields: String): String {
        return fields.firstNotNullOfOrNull { field ->
            getString(field)?.trim()?.takeIf { it.isNotBlank() }
        }.orEmpty()
    }

    private fun DocumentSnapshot.stringList(vararg fields: String): List<String> {
        fields.forEach { field ->
            val value = get(field)
            val list = when (value) {
                is List<*> -> value.mapNotNull { it?.toString()?.trim()?.takeIf(String::isNotBlank) }
                is String -> value.split(",").map { it.trim() }.filter { it.isNotBlank() }
                else -> emptyList()
            }
            if (list.isNotEmpty()) return list
        }
        return emptyList()
    }

    private fun DocumentSnapshot.numberValue(vararg fields: String): Int? {
        fields.forEach { field ->
            when (val value = get(field)) {
                is Number -> return value.toInt()
                is String -> value.toIntOrNull()?.let { return it }
            }
        }
        return null
    }

    private fun DocumentSnapshot.timestampMillis(vararg fields: String): Long {
        fields.forEach { field ->
            val value = get(field)
            when (value) {
                is com.google.firebase.Timestamp -> return value.toDate().time
                is java.util.Date -> return value.time
                is Number -> return value.toLong()
            }
        }
        return 0L
    }

    private fun levelRank(level: String): Int {
        return when (level.uppercase()) {
            "N5" -> 0
            "N4" -> 1
            "N3" -> 2
            "N2" -> 3
            "N1" -> 4
            else -> 5
        }
    }
}
