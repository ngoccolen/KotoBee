package com.example.kotobee.ui.lessons.reading

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kotobee.data.model.JaViTranslationRequest
import com.example.kotobee.data.model.VocabDetail
import com.example.kotobee.data.service.SpeakingApiService
import com.example.kotobee.util.TranslatorHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

data class NhkArticle(
    val newsId: String,
    val title: String,
    val titleWithRuby: String,
    val difficulty: String,
    val htmlContent: String,
    val rawText: String,
    val audioUrl: String,
    val imageUrl: String,
    val date: String,
    var isFavorite: Boolean = false,
    var isRead: Boolean = false
)

enum class ReadingDifficulty(val label: String) {
    EASY("Dễ"),
    MEDIUM("Trung bình"),
    HARD("Khó")
}

fun normalizeReadingDifficulty(vararg rawValues: String?): ReadingDifficulty {
    val token = rawValues
        .firstOrNull { !it.isNullOrBlank() }
        ?.toDifficultyToken()
        ?: return ReadingDifficulty.MEDIUM
    val parts = token.split("_").filter(String::isNotBlank)

    return when {
        "N5" in parts || token == "DE" || token.contains("EASY") -> ReadingDifficulty.EASY
        "N4" in parts || token == "TRUNG_BINH" || token.contains("MEDIUM") -> ReadingDifficulty.MEDIUM
        parts.any { it in listOf("N3", "N2", "N1") } || token == "KHO" || token.contains("HARD") -> ReadingDifficulty.HARD
        else -> ReadingDifficulty.MEDIUM
    }
}

private fun String.toDifficultyToken(): String {
    return Normalizer.normalize(trim(), Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")
        .uppercase(Locale.US)
        .replace("[^A-Z0-9]+".toRegex(), "_")
        .trim('_')
}

class ReadingViewModel(
    private val apiService: SpeakingApiService? = null
) : ViewModel(), TextToSpeech.OnInitListener {

    private val _newsList = MutableStateFlow<List<NhkArticle>>(emptyList())
    val newsList = _newsList.asStateFlow()

    private val _selectedLevel = MutableStateFlow("Tất cả")
    val selectedLevel = _selectedLevel.asStateFlow()

    val filteredNewsList = combine(_newsList, _selectedLevel) { news, level ->
        when (level) {
            "Tất cả" -> news
            "Yêu thích" -> news.filter { it.isFavorite }
            ReadingDifficulty.EASY.label -> news.filter { normalizeReadingDifficulty(it.difficulty) == ReadingDifficulty.EASY }
            ReadingDifficulty.MEDIUM.label -> news.filter { normalizeReadingDifficulty(it.difficulty) == ReadingDifficulty.MEDIUM }
            ReadingDifficulty.HARD.label -> news.filter { normalizeReadingDifficulty(it.difficulty) == ReadingDifficulty.HARD }
            else -> news
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _currentNewsId = MutableStateFlow<String?>(null)

    val currentArticle = combine(_newsList, _currentNewsId) { list, id ->
        if (id == null) null else list.find { it.newsId == id }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _selectedVocab = MutableStateFlow<VocabDetail?>(null)
    val selectedVocab = _selectedVocab.asStateFlow()

    private val _aiTranslation = MutableStateFlow<String?>(null)
    val aiTranslation = _aiTranslation.asStateFlow()

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating = _isTranslating.asStateFlow()

    // Thông báo kết quả lưu sổ tay
    private val _saveToNotebookResult = MutableSharedFlow<String>()
    val saveToNotebookResult = _saveToNotebookResult.asSharedFlow()

    private var tts: TextToSpeech? = null
    private val translatorHelper = TranslatorHelper()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        fetchNewsFromFirebase()
    }

    private fun fetchNewsFromFirebase() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("news")
                    .orderBy("created_at", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                val articles = snapshot.documents.mapNotNull { doc ->
                    try {
                        val timestamp = doc.getTimestamp("created_at")
                        val dateString = if (timestamp != null) dateFormat.format(timestamp.toDate()) else "N/A"

                        NhkArticle(
                            newsId = doc.getString("news_id") ?: doc.id,
                            title = doc.getString("title") ?: "Chưa có tiêu đề",
                            titleWithRuby = doc.getString("title") ?: "",
                            difficulty = normalizeReadingDifficulty(
                                doc.getString("difficulty"),
                                doc.getString("level")
                            ).label,
                            htmlContent = doc.getString("body") ?: "",
                            rawText = doc.getString("body_plain") ?: "",
                            audioUrl = doc.getString("audio_url") ?: "",
                            imageUrl = doc.getString("image_url") ?: "",
                            date = dateString,
                            isFavorite = false,
                            isRead = false
                        )
                    } catch (e: Exception) { null }
                }
                _newsList.value = articles
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun setFilterLevel(level: String) {
        _selectedLevel.value = level
    }

    fun loadArticleDetail(newsId: String) {
        _currentNewsId.value = newsId
    }

    fun toggleFavorite(newsId: String) {
        _newsList.update { currentList ->
            currentList.map {
                if (it.newsId == newsId) it.copy(isFavorite = !it.isFavorite) else it
            }
        }
    }

    fun initTTS(context: Context) {
        if (tts == null) tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) tts?.language = Locale.JAPANESE
    }

    fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun translateSelectedText(
        text: String,
        articleTitle: String = "",
        articleContext: String = ""
    ) {
        val cleanText = text.trim()
        if (cleanText.isBlank() || _isTranslating.value) return

        viewModelScope.launch {
            _isTranslating.value = true
            val aiResult = runCatching {
                val service = apiService ?: error("AI translation backend is not configured.")
                service.translateJaVi(
                    JaViTranslationRequest(
                        text = cleanText,
                        articleTitle = articleTitle,
                        articleContext = compactArticleContext(articleContext)
                    )
                ).translationVi.trim()
            }.getOrNull()

            if (!aiResult.isNullOrBlank()) {
                _aiTranslation.value = aiResult
                _selectedVocab.value = VocabDetail(word = cleanText, meaning = aiResult, furigana = "Dịch AI", hanViet = "")
                speak(cleanText)
                _isTranslating.value = false
                return@launch
            }

            if (translatorHelper.downloadModelIfNeeded()) {
                val result = translatorHelper.translateText(cleanText)
                val fallbackResult = if (result.startsWith("Lỗi dịch:")) {
                    result
                } else {
                    "$result\n\n(Bản dịch offline, có thể chưa sát ngữ cảnh.)"
                }
                _aiTranslation.value = fallbackResult
                _selectedVocab.value = VocabDetail(word = cleanText, meaning = fallbackResult, furigana = "Dịch offline", hanViet = "")
                speak(cleanText)
            } else {
                _selectedVocab.value = VocabDetail(
                    word = cleanText,
                    meaning = "Không thể dịch lúc này. Vui lòng kiểm tra kết nối mạng.",
                    furigana = "Lỗi dịch",
                    hanViet = ""
                )
            }
            _isTranslating.value = false
        }
    }

    /**
     * Lưu từ vựng vào sổ tay (Firestore).
     * Tự tạo deck "Sổ tay từ vựng" nếu chưa có, sau đó thêm từ vào deck đó.
     */
    fun saveVocabToNotebook(word: String, meaning: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            viewModelScope.launch {
                _saveToNotebookResult.emit("Bạn cần đăng nhập để lưu từ vựng")
            }
            return
        }

        viewModelScope.launch {
            try {
                val decksRef = db.collection("decks")
                val notebookName = "📒 Sổ tay từ vựng"

                // Tìm hoặc tạo deck "Sổ tay từ vựng"
                val existingDeck = decksRef
                    .whereEqualTo("ownerId", userId)
                    .whereEqualTo("name", notebookName)
                    .get()
                    .await()

                val deckId = if (existingDeck.isEmpty) {
                    // Tạo deck mới
                    val newDeckId = UUID.randomUUID().toString()
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    decksRef.document(newDeckId).set(
                        mapOf(
                            "id" to newDeckId,
                            "name" to notebookName,
                            "description" to "Từ vựng lưu từ bài đọc",
                            "userId" to userId,
                            "ownerId" to userId,
                            "createdAt" to System.currentTimeMillis(),
                            "sharedWith" to emptyList<String>()
                        )
                    ).await()
                    newDeckId
                } else {
                    existingDeck.documents.first().id
                }

                // Thêm từ vựng vào deck
                val vocabId = UUID.randomUUID().toString()
                db.collection("decks").document(deckId)
                    .collection("vocabs").document(vocabId)
                    .set(
                        mapOf(
                            "id" to vocabId,
                            "deckId" to deckId,
                            "kanji" to word,
                            "kana" to word,
                            "meaning" to meaning,
                            "example" to "",
                            "exampleMeaning" to "",
                            "level" to 0,
                            "nextReviewTime" to System.currentTimeMillis()
                        )
                    ).await()

                _saveToNotebookResult.emit("✅ Đã lưu \"$word\" vào sổ tay!")
            } catch (e: Exception) {
                Log.e("ReadingViewModel", "Lỗi lưu sổ tay: ${e.message}")
                _saveToNotebookResult.emit("❌ Lỗi lưu từ vựng: ${e.message}")
            }
        }
    }

    fun clearSelectedVocab() {
        _selectedVocab.value = null
        _aiTranslation.value = null
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
        translatorHelper.close()
    }

    private fun compactArticleContext(context: String): String {
        return context
            .replace(Regex("<[^>]+>"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .take(4_000)
    }

    class Factory(private val apiService: SpeakingApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReadingViewModel::class.java)) {
                return ReadingViewModel(apiService) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
