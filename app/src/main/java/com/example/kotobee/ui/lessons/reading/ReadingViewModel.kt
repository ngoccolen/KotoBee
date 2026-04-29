package com.example.kotobee.ui.lessons.reading

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotobee.data.model.ReadingLesson
import com.example.kotobee.data.model.VocabDetail
import com.example.kotobee.data.repository.ReadingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Trạng thái giao diện
sealed class ReadingUiState {
    object Loading : ReadingUiState()
    data class Success(val lesson: ReadingLesson, val annotatedContent: AnnotatedString) : ReadingUiState()
    data class Error(val message: String) : ReadingUiState()
}

class ReadingViewModel(
    private val repository: ReadingRepository = ReadingRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReadingUiState>(ReadingUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _selectedVocab = MutableStateFlow<VocabDetail?>(null)
    val selectedVocab = _selectedVocab.asStateFlow()

    private val _saveStatus = MutableStateFlow<String?>(null)
    val saveStatus = _saveStatus.asStateFlow()

    private val _quizResult = MutableStateFlow<Boolean?>(null)
    val quizResult = _quizResult.asStateFlow()

    private val _aiTranslation = MutableStateFlow<String?>(null)
    val aiTranslation = _aiTranslation.asStateFlow()

    private var tts: TextToSpeech? = null
    private val translatorHelper = TranslatorHelper()

    // Khởi tạo TTS
    fun initTTS(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.JAPANESE
        }
    }

    fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun loadLesson(lessonId: String, highlightColor: Color, grammarColor: Color) {
        viewModelScope.launch {
            _uiState.value = ReadingUiState.Loading
            try {
                val lesson = repository.getLessonDetail(lessonId)
                lesson?.let {
                    val annotated = parseReadingContent(it.content, highlightColor, grammarColor)
                    _uiState.value = ReadingUiState.Success(it, annotated)
                } ?: run {
                    _uiState.value = ReadingUiState.Error("Không tìm thấy bài học.")
                }
            } catch (e: Exception) {
                _uiState.value = ReadingUiState.Error(e.message ?: "Lỗi kết nối")
            }
        }
    }

    fun translateSelectedText(text: String) {
        viewModelScope.launch {
            val isReady = translatorHelper.downloadModelIfNeeded()
            if (isReady) {
                val result = translatorHelper.translateText(text)
                _aiTranslation.value = result
                // Khi dịch xong thì giả lập một VocabDetail để hiện lên BottomSheet
                _selectedVocab.value = VocabDetail(
                    word = text,
                    meaning = result,
                    furigana = "AI Translation",
                    hanViet = ""
                )
                speak(text) // Tự động phát âm khi dịch
            }
        }
    }

    fun onWordClicked(vocabId: String) {
        viewModelScope.launch {
            val vocab = repository.getVocabDetail(vocabId)
            _selectedVocab.value = vocab
            vocab?.let { speak(it.word) }
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

    fun saveVocab(userId: String, vocab: VocabDetail) {
        viewModelScope.launch {
            try {
                repository.toggleBookmark(userId, vocab, isSaving = true)
                _saveStatus.value = "Đã lưu '${vocab.word}' vào sổ tay!"
            } catch (e: Exception) {
                _saveStatus.value = "Lỗi khi lưu: ${e.message}"
            }
        }
    }

    fun clearSaveStatus() {
        _saveStatus.value = null
    }

    fun checkQuizAnswer(userId: String, lessonId: String, selectedOption: Int, correctOption: Int) {
        val isCorrect = selectedOption == correctOption
        _quizResult.value = isCorrect

        if (isCorrect) {
            viewModelScope.launch {
                try {
                    repository.updateReadingProgress(userId, lessonId, 100, 10)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun resetQuiz() {
        _quizResult.value = null
    }
}

// Hàm Parse nội dung chuẩn
fun parseReadingContent(rawText: String, vocabColor: Color, grammarColor: Color): AnnotatedString {
    return buildAnnotatedString {
        val regex = """\[(vocab|grammar):([^\]]+)\](.*?)\[/\1\]""".toRegex()
        var lastIndex = 0

        regex.findAll(rawText).forEach { matchResult ->
            val type = matchResult.groupValues[1]
            val id = matchResult.groupValues[2]
            val text = matchResult.groupValues[3]

            append(rawText.substring(lastIndex, matchResult.range.first))

            pushStringAnnotation(tag = type, annotation = id)
            withStyle(style = when(type) {
                "vocab" -> SpanStyle(background = Color(0xFFFFF9C4), color = vocabColor, fontWeight = FontWeight.Bold)
                else -> SpanStyle(color = grammarColor, textDecoration = TextDecoration.Underline, fontWeight = FontWeight.Medium)
            }) {
                append(text)
            }
            pop()
            lastIndex = matchResult.range.last + 1
        }

        if (lastIndex < rawText.length) {
            append(rawText.substring(lastIndex))
        }
    }
}