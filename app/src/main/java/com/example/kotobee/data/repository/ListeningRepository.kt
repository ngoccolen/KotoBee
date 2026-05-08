package com.example.kotobee.data.repository

import com.example.kotobee.data.model.ListeningUiState
import com.example.kotobee.data.model.QuizQuestion
import com.example.kotobee.data.model.TranscriptLine
import com.example.kotobee.data.model.WordDetail
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ListeningRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun fetchLessonDetail(lessonId: String): ListeningUiState = withContext(Dispatchers.IO) {
        try {
            // 1. Gọi lên Firestore lấy document theo ID bài học
            val snapshot = firestore.collection("lessons")
                .document(lessonId)
                .get()
                .await()

            if (snapshot.exists()) {
                // 2. Bóc tách dữ liệu cơ bản
                val title = snapshot.getString("lessonTitle") ?: snapshot.getString("title") ?: ""
                val level = snapshot.getString("level") ?: "N5"
                val audioUrl = snapshot.getString("audioUrl") ?: ""

                // --- QUAN TRỌNG: Lấy youtubeId để hiển thị video ---
                val youtubeId = snapshot.getString("youtubeId") ?: ""

                // 3. Parse mảng Transcript và Vocab lồng bên trong
                val rawTranscript = snapshot.get("transcript") as? List<Map<String, Any>> ?: emptyList()
                val transcript = rawTranscript.map { item ->

                    // Xử lý mảng từ vựng (vocab) của từng câu thoại
                    val rawVocab = item["vocab"] as? List<Map<String, Any>> ?: emptyList()
                    val mappedVocab = rawVocab.map { v ->
                        WordDetail(
                            word = v["word"] as? String ?: "",
                            reading = v["reading"] as? String ?: "",
                            meaning = v["meaning"] as? String ?: "",
                            isGrammar = v["isGrammar"] as? Boolean ?: false
                        )
                    }

                    TranscriptLine(
                        speaker = item["speaker"] as? String ?: "",
                        jpText = item["jpText"] as? String ?: "",
                        viText = item["viText"] as? String ?: "",
                        vocab = mappedVocab
                    )
                }

                // 4. Parse mảng Quiz
                val rawQuiz = snapshot.get("quiz") as? List<Map<String, Any>> ?: emptyList()
                val quizzes = rawQuiz.map { item ->
                    val optionsList = item["options"] as? List<*>
                    val safeOptions = optionsList?.filterIsInstance<String>() ?: emptyList()

                    QuizQuestion(
                        question = item["question"] as? String ?: "",
                        options = safeOptions,
                        correctAnswerIndex = (item["correctIndex"] as? Long)?.toInt() ?: 0
                    )
                }

                // 5. Trả về State cho ViewModel kèm theo youtubeId đã lấy
                return@withContext ListeningUiState(
                    isLoading = false,
                    lessonTitle = title,
                    level = level,
                    audioUrl = audioUrl,
                    youtubeId = youtubeId, // Gán ID video vào đây
                    transcript = transcript,
                    quizzes = quizzes
                )
            } else {
                throw Exception("Không tìm thấy dữ liệu bài học trên Firebase!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}