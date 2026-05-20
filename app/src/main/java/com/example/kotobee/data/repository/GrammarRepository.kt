package com.example.kotobee.data.repository

import com.example.kotobee.data.model.Example
import com.example.kotobee.data.model.Grammar
import com.example.kotobee.data.model.GrammarProgress
import com.example.kotobee.data.model.GrammarQuestion
import com.example.kotobee.data.model.GrammarQuizSaveResult
import com.example.kotobee.util.StudyActivityTracker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlin.math.max
import java.util.Locale

class GrammarRepository {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    suspend fun getAllGrammarLessons(): List<Grammar> {
        return db.collection(GRAMMAR_COLLECTION)
            .get()
            .await()
            .documents
            .filter { it.isPublished() }
            .mapNotNull { it.toGrammar() }
            .sortedWith(grammarComparator())
    }

    suspend fun getGrammarLessonsByLevel(level: String): List<Grammar> {
        return getAllGrammarLessons()
            .filter { it.level.equals(level, ignoreCase = true) }
            .sortedWith(grammarComparator())
    }

    suspend fun getGrammarById(grammarId: String): Grammar? {
        val document = db.collection(GRAMMAR_COLLECTION)
            .document(grammarId)
            .get()
            .await()

        if (document.exists() && document.isPublished()) {
            document.toGrammar()?.let { return it }
        }

        return db.collection(GRAMMAR_COLLECTION)
            .whereEqualTo("grammar_id", grammarId)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull { it.isPublished() }
            ?.toGrammar()
    }

    suspend fun getQuestionsForLesson(lessonId: String): List<GrammarQuestion> {
        val embeddedQuestions = db.collection(GRAMMAR_COLLECTION)
            .document(lessonId)
            .get()
            .await()
            .toQuestions(lessonId)

        val externalQuestions = getExternalQuestionsForLesson(lessonId)

        return mergeGrammarQuizQuestions(
            embeddedQuestions = embeddedQuestions,
            externalQuestions = externalQuestions
        )
    }

    suspend fun getGrammarProgress(): Map<String, GrammarProgress> {
        val userRef = currentUserReference() ?: return emptyMap()
        return userRef.collection(GRAMMAR_PROGRESS_COLLECTION)
            .get()
            .await()
            .documents
            .mapNotNull { it.toGrammarProgress() }
            .associateBy { it.grammarId }
    }

    suspend fun saveGrammarQuizResult(
        grammar: Grammar,
        score: Int,
        correctCount: Int,
        totalQuestions: Int
    ): GrammarQuizSaveResult {
        val userRef = currentUserReference()
            ?: throw IllegalStateException("Bạn cần đăng nhập để lưu tiến độ quiz.")

        val safeScore = score.coerceIn(0, 100)
        val safeCorrectCount = correctCount.coerceAtLeast(0)
        val safeTotalQuestions = totalQuestions.coerceAtLeast(0)
        val passed = safeScore >= PASSING_SCORE
        val progressRef = userRef.collection(GRAMMAR_PROGRESS_COLLECTION).document(grammar.id)

        var awardedPoints = false
        var savedBestScore = safeScore

        db.runTransaction { transaction ->
            val snapshot = transaction.get(progressRef)
            val wasCompleted = snapshot.getBoolean("completed") == true
            val previousBest = snapshot.numberValue("bestScore")
            val shouldUseAttemptStats = safeScore >= previousBest
            savedBestScore = max(previousBest, safeScore)
            val completed = wasCompleted || passed

            val payload = mutableMapOf<String, Any?>(
                "grammarId" to grammar.id,
                "level" to grammar.level,
                "completed" to completed,
                "bestScore" to savedBestScore,
                "correctCount" to if (shouldUseAttemptStats) safeCorrectCount else snapshot.numberValue("correctCount"),
                "totalQuestions" to if (shouldUseAttemptStats) safeTotalQuestions else snapshot.numberValue("totalQuestions"),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            if (completed && !wasCompleted) {
                payload["completedAt"] = FieldValue.serverTimestamp()
                awardedPoints = true
            }

            transaction.set(progressRef, payload, SetOptions.merge())
        }.await()

        if (awardedPoints) {
            StudyActivityTracker.recordStudyActivity(
                userRef = userRef,
                points = 20L,
                source = "grammar_quiz"
            )
        }

        return GrammarQuizSaveResult(
            passed = passed,
            awardedPoints = awardedPoints,
            bestScore = savedBestScore
        )
    }

    private fun DocumentSnapshot.toGrammar(): Grammar? {
        val id = firstString("grammar_id", "id").ifBlank { this.id }
        val title = firstString("title", "pattern")
        if (id.isBlank() || title.isBlank()) return null

        return Grammar(
            id = id,
            level = firstString("level").ifBlank { "N5" },
            title = title,
            romaji = firstString("romaji", "reading"),
            meaning = firstString("meaning_vi", "meaning"),
            summary = firstString("summary", "note"),
            formation = firstString("structure", "formation"),
            usageNote = listOf(firstString("usage"), firstString("note"))
                .filter(String::isNotBlank)
                .distinct()
                .joinToString("\n\n"),
            tags = stringList("tags"),
            sourceName = firstString("sourceName", "source_name"),
            sourceUrl = firstString("sourceUrl", "source_url"),
            examples = toExamples(),
            sortOrder = numberValue("sort_order").takeIf { it > 0 } ?: Int.MAX_VALUE,
            questionCount = toQuestions(id).size
        )
    }

    private fun DocumentSnapshot.toExamples(): List<Example> {
        val rawExamples = get("examples") as? List<*> ?: return emptyList()

        return rawExamples.mapNotNull { row ->
            val item = row.asMap() ?: return@mapNotNull null
            Example(
                jp = item.firstString("jp", "jpText", "text"),
                romaji = item.firstString("romaji", "reading"),
                vi = item.firstString("vi", "viText", "meaning_vi", "translation"),
                en = item.firstString("en")
            )
        }.filter { it.jp.isNotBlank() || it.vi.isNotBlank() }
    }

    private fun DocumentSnapshot.toQuestions(defaultLessonId: String): List<GrammarQuestion> {
        val rawQuestions = (get("questions") ?: get("quiz")) as? List<*> ?: return emptyList()

        return rawQuestions.mapIndexedNotNull { index, row ->
            val item = row.asMap() ?: return@mapIndexedNotNull null
            item.toGrammarQuestion(defaultLessonId, "embedded_$index")
        }
    }

    private fun DocumentSnapshot.toGrammarQuestion(defaultLessonId: String): GrammarQuestion? {
        val item = data ?: return null
        return item.toGrammarQuestion(defaultLessonId, id)
    }

    private suspend fun getExternalQuestionsForLesson(lessonId: String): List<GrammarQuestion> {
        val documents = mutableListOf<DocumentSnapshot>()

        listOf("lessonId", "grammarId", "grammar_id").forEach { field ->
            documents += db.collection(GRAMMAR_QUESTIONS_COLLECTION)
                .whereEqualTo(field, lessonId)
                .get()
                .await()
                .documents
        }

        return documents
            .distinctBy { it.reference.path }
            .mapNotNull { it.toGrammarQuestion(lessonId) }
    }

    private fun Map<String, Any?>.toGrammarQuestion(defaultLessonId: String, fallbackId: String): GrammarQuestion? {
        val options = (this["options"] as? List<*>)
            ?.mapNotNull { it?.toString()?.takeIf(String::isNotBlank) }
            .orEmpty()
        val correctAnswer = firstString("correctAnswer", "answer").ifBlank {
            this["correctIndex"].asInt()
                ?.let { options.getOrNull(it) }
                .orEmpty()
        }
        val content = firstString("content", "question")

        if (content.isBlank() || options.isEmpty() || correctAnswer.isBlank()) return null

        return GrammarQuestion(
            id = firstString("id").ifBlank { fallbackId },
            lessonId = firstString("lessonId", "grammarId", "grammar_id").ifBlank { defaultLessonId },
            type = firstString("type").ifBlank { "MULTIPLE_CHOICE" },
            content = content,
            options = options,
            correctAnswer = correctAnswer,
            hint = firstString("hint", "explanation")
        )
    }

    private suspend fun currentUserReference(): DocumentReference? {
        val email = auth.currentUser?.email ?: return null
        val snapshot = db.collection("users")
            .whereEqualTo("email", email)
            .limit(1)
            .get()
            .await()
        return snapshot.documents.firstOrNull()?.reference
    }

    private fun DocumentSnapshot.toGrammarProgress(): GrammarProgress? {
        val grammarId = firstString("grammarId", "grammar_id").ifBlank { id }
        if (grammarId.isBlank()) return null

        return GrammarProgress(
            grammarId = grammarId,
            level = firstString("level").ifBlank { "N5" },
            completed = getBoolean("completed") == true,
            bestScore = numberValue("bestScore"),
            correctCount = numberValue("correctCount"),
            totalQuestions = numberValue("totalQuestions")
        )
    }

    private fun DocumentSnapshot.firstString(vararg fields: String): String =
        fields.firstNotNullOfOrNull { field ->
            getString(field)?.trim()?.takeIf { it.isNotBlank() }
        }.orEmpty()

    private fun DocumentSnapshot.stringList(field: String): List<String> =
        (get(field) as? List<*>)
            ?.mapNotNull { it?.toString()?.trim()?.takeIf(String::isNotBlank) }
            .orEmpty()

    private fun DocumentSnapshot.numberValue(vararg fields: String): Int {
        fields.forEach { field ->
            when (val value = get(field)) {
                is Number -> return value.toInt()
                is String -> value.toIntOrNull()?.let { return it }
            }
        }
        return 0
    }

    private fun DocumentSnapshot.isPublished(): Boolean {
        val status = firstString("status")
        return status.isBlank() || status == "published"
    }

    private fun Any?.asMap(): Map<String, Any?>? =
        (this as? Map<*, *>)?.mapNotNull { (key, value) ->
            val name = key as? String ?: return@mapNotNull null
            name to value
        }?.toMap()

    private fun Map<String, Any?>.firstString(vararg fields: String): String =
        fields.firstNotNullOfOrNull { field ->
            this[field]?.toString()?.trim()?.takeIf { it.isNotBlank() }
        }.orEmpty()

    private fun Any?.asInt(): Int? = when (this) {
        is Number -> toInt()
        is String -> toIntOrNull()
        else -> null
    }

    private fun levelRank(level: String): Int =
        when (level.uppercase()) {
            "N5" -> 0
            "N4" -> 1
            "N3" -> 2
            "N2" -> 3
            "N1" -> 4
            else -> 5
        }

    private fun grammarComparator(): Comparator<Grammar> =
        compareBy<Grammar> { levelRank(it.level) }
            .thenBy { it.sortOrder }
            .thenBy { it.title }

    private companion object {
        const val PASSING_SCORE = 80
        const val GRAMMAR_COLLECTION = "grammar"
        const val GRAMMAR_QUESTIONS_COLLECTION = "grammar_questions"
        const val GRAMMAR_PROGRESS_COLLECTION = "grammar_progress"
    }
}

internal const val GRAMMAR_QUIZ_QUESTION_LIMIT = 10

internal fun mergeGrammarQuizQuestions(
    embeddedQuestions: List<GrammarQuestion>,
    externalQuestions: List<GrammarQuestion>,
    limit: Int = GRAMMAR_QUIZ_QUESTION_LIMIT,
    shuffle: Boolean = true
): List<GrammarQuestion> {
    val distinctQuestions = (embeddedQuestions + externalQuestions)
        .distinctBy { it.quizDedupeKey() }

    return if (shuffle) {
        distinctQuestions.shuffled().take(limit)
    } else {
        distinctQuestions.take(limit)
    }
}

private fun GrammarQuestion.quizDedupeKey(): String {
    return listOf(lessonId, content, correctAnswer)
        .joinToString("|") { value -> value.trim().lowercase(Locale.US) }
}
