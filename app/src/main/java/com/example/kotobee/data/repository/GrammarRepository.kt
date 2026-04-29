package com.example.kotobee.data.repository

import com.example.kotobee.data.model.Grammar
import com.example.kotobee.data.model.GrammarQuestion
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class GrammarRepository {
    private val db = FirebaseFirestore.getInstance()

    // Kéo danh sách bài học theo Level (VD: N4, N5)
    suspend fun getGrammarLessonsByLevel(level: String): List<Grammar> {
        return try {
            val snapshot = db.collection("grammar_lessons")
                .whereEqualTo("level", level)
                .get()
                .await()
            snapshot.toObjects(Grammar::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Kéo câu hỏi Quiz cho một bài học cụ thể
    suspend fun getQuestionsForLesson(lessonId: String): List<GrammarQuestion> {
        return try {
            val snapshot = db.collection("grammar_questions")
                .whereEqualTo("lessonId", lessonId)
                .get()
                .await()
            snapshot.toObjects(GrammarQuestion::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}