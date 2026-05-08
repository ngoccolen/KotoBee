package com.example.kotobee.ui.lessons.reading

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotobee.data.model.VocabDetail
import com.example.kotobee.util.TranslatorHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

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

class ReadingViewModel : ViewModel(), TextToSpeech.OnInitListener {

    private val _newsList = MutableStateFlow<List<NhkArticle>>(emptyList())
    val newsList = _newsList.asStateFlow()

    // TRẠNG THÁI FILTER: Tất cả, Yêu thích, Dễ, Trung bình, Khó
    private val _selectedLevel = MutableStateFlow("Tất cả")
    val selectedLevel = _selectedLevel.asStateFlow()

    // DANH SÁCH ĐÃ LỌC
    val filteredNewsList = combine(_newsList, _selectedLevel) { news, level ->
        when (level) {
            "Tất cả" -> news
            "Yêu thích" -> news.filter { it.isFavorite }
            "Dễ" -> news.filter { it.difficulty in listOf("N5", "N4", "DỄ") }
            "Trung bình" -> news.filter { it.difficulty in listOf("N3", "TRUNG BÌNH") }
            "Khó" -> news.filter { it.difficulty in listOf("N2", "N1", "KHÓ") }
            else -> news
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // --- SỬA LỖI ĐÂY: LOGIC TÌM BÀI BÁO TỰ ĐỘNG ---
    private val _currentNewsId = MutableStateFlow<String?>(null)

    val currentArticle = combine(_newsList, _currentNewsId) { list, id ->
        if (id == null) null else list.find { it.newsId == id }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)
    // ----------------------------------------------

    private val _selectedVocab = MutableStateFlow<VocabDetail?>(null)
    val selectedVocab = _selectedVocab.asStateFlow()

    private val _aiTranslation = MutableStateFlow<String?>(null)
    val aiTranslation = _aiTranslation.asStateFlow()

    private var tts: TextToSpeech? = null
    private val translatorHelper = TranslatorHelper()
    private val db = FirebaseFirestore.getInstance()

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
                            difficulty = doc.getString("level")?.uppercase() ?: "N4",
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

    fun translateSelectedText(text: String) {
        viewModelScope.launch {
            if (translatorHelper.downloadModelIfNeeded()) {
                val result = translatorHelper.translateText(text)
                _aiTranslation.value = result
                _selectedVocab.value = VocabDetail(word = text, meaning = result, furigana = "Dịch", hanViet = "")
                speak(text)
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
}