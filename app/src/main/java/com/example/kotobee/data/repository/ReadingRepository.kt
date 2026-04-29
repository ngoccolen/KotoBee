package com.example.kotobee.data.repository

// Đảm bảo import đúng đường dẫn model của bạn
import com.example.kotobee.data.model.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class ReadingRepository {
    private val db = FirebaseFirestore.getInstance()

    // ==========================================
    // 1. Lấy danh sách bài đọc theo Level (N5, N4, N3...)
    // ==========================================
    suspend fun getLessonsByLevel(level: String): List<ReadingLesson> {
        return try {
            val snapshot = db.collection("reading_lessons")
                .whereEqualTo("level", level)
                // .orderBy("createdAt", Query.Direction.DESCENDING) // Bỏ comment nếu DB có trường createdAt
                .get().await()
            snapshot.toObjects(ReadingLesson::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // ==========================================
    // 2. Lấy nội dung chi tiết 1 bài đọc cụ thể
    // ==========================================
    suspend fun getLessonDetail(lessonId: String): ReadingLesson? {
        return try {
            val snapshot = db.collection("reading_lessons").document(lessonId).get().await()
            snapshot.toObject(ReadingLesson::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ==========================================
    // 3. Lấy thông tin Từ vựng (Vocabulary)
    // ==========================================
    suspend fun getVocabDetail(vocabId: String): VocabDetail? {
        return try {
            val snapshot = db.collection("vocabularies").document(vocabId).get().await()
            snapshot.toObject(VocabDetail::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ==========================================
    // 4. Lấy thông tin Ngữ pháp (Grammar)
    // ==========================================
    suspend fun getGrammarDetail(grammarId: String): GrammarDetail? {
        return try {
            val snapshot = db.collection("grammars").document(grammarId).get().await()
            snapshot.toObject(GrammarDetail::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ==========================================
    // 5. Tính năng Bookmark: Lưu từ vựng vào sổ tay
    // ==========================================
    suspend fun toggleBookmark(userId: String, vocab: VocabDetail, isSaving: Boolean) {
        val docRef = db.collection("users").document(userId)
            .collection("saved_vocabularies").document(vocab.vocabId)

        try {
            if (isSaving) {
                // Chỉ lưu những thông tin cần thiết vào collection user cho nhẹ
                val data = mapOf(
                    "word" to vocab.word,
                    "savedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(data).await()
            } else {
                docRef.delete().await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e // Ném lỗi ra để ViewModel bắt và hiển thị Toast cho user
        }
    }

    // ==========================================
    // 6. Theo dõi tiến độ (% đọc & điểm Quiz)
    // ==========================================
    suspend fun updateReadingProgress(userId: String, lessonId: String, progress: Int, quizScore: Int? = null) {
        val data = mutableMapOf<String, Any>(
            "progressPercent" to progress,
            "lastReadAt" to FieldValue.serverTimestamp()
        )
        // Nếu làm xong quiz mới đẩy điểm lên
        if (quizScore != null) {
            data["quizScore"] = quizScore
        }

        try {
            db.collection("users").document(userId)
                .collection("reading_progress").document(lessonId)
                .set(data, SetOptions.merge()) // SetOptions.merge() giúp ghi đè data cũ mà ko làm mất các trường khác
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}