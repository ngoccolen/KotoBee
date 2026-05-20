package com.example.kotobee.ui.lessons.shadowing

import android.media.MediaRecorder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kotobee.data.model.ShadowingLesson
import com.example.kotobee.data.model.ShadowingResponse
import com.example.kotobee.data.repository.ShadowingRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class ShadowingViewModel(
    private val repository: ShadowingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ShadowingUiState>(ShadowingUiState.Idle)
    val uiState: StateFlow<ShadowingUiState> = _uiState.asStateFlow()

    private val _lessons = MutableStateFlow<List<ShadowingLesson>>(emptyList())
    val lessons: StateFlow<List<ShadowingLesson>> = _lessons.asStateFlow()

    private val _lessonsLoading = MutableStateFlow(false)
    val lessonsLoading: StateFlow<Boolean> = _lessonsLoading.asStateFlow()

    private val _lessonsError = MutableStateFlow<String?>(null)
    val lessonsError: StateFlow<String?> = _lessonsError.asStateFlow()

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    init {
        loadLessons()
    }

    fun loadLessons() {
        if (_lessonsLoading.value) return

        viewModelScope.launch {
            _lessonsLoading.value = true
            _lessonsError.value = null
            runCatching {
                repository.getShadowingLessons()
            }.onSuccess { data ->
                _lessons.value = data
            }.onFailure { error ->
                _lessons.value = emptyList()
                _lessonsError.value = error.message ?: "Không thể tải bài shadowing"
            }
            _lessonsLoading.value = false
        }
    }

    fun startRecording(outputFile: File) {
        audioFile = outputFile
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile.absolutePath)

            try {
                prepare()
                start()
                _uiState.value = ShadowingUiState.Recording
            } catch (e: IOException) {
                Log.e("ShadowingViewModel", "prepare() failed", e)
                _uiState.value = ShadowingUiState.Error("Không thể bắt đầu ghi âm")
            }
        }
    }

    fun stopRecordingAndAnalyze(lesson: ShadowingLesson) {
        mediaRecorder?.apply {
            try {
                stop()
                release()
                _uiState.value = ShadowingUiState.Analyzing
                
                val currentAudioFile = audioFile
                if (currentAudioFile != null && currentAudioFile.exists()) {
                    analyzeAudio(currentAudioFile, lesson)
                } else {
                    _uiState.value = ShadowingUiState.Error("Không tìm thấy file ghi âm")
                }
            } catch (e: RuntimeException) {
                // stop() can throw RuntimeException if called immediately after start()
                release()
                _uiState.value = ShadowingUiState.Error("Lỗi khi dừng ghi âm. Hãy thử lại.")
            }
        }
        mediaRecorder = null
    }

    private fun analyzeAudio(file: File, lesson: ShadowingLesson) {
        viewModelScope.launch {
            val unitsJson = Gson().toJson(lesson.expectedUnits)
            val result = repository.analyzeShadowing(
                audioFile = file,
                expectedText = lesson.japanese,
                expectedUnitsJson = unitsJson,
                level = lesson.level,
                checkGrammar = true
            )

            result.onSuccess { response ->
                _uiState.value = ShadowingUiState.Result(response)
            }.onFailure { error ->
                Log.e("ShadowingViewModel", "API Error", error)
                val userMessage = error.message ?: "Lỗi kết nối đến server. Vui lòng kiểm tra mạng và thử lại."
                _uiState.value = ShadowingUiState.Error(userMessage)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaRecorder?.release()
        mediaRecorder = null
    }

    class Factory(private val repository: ShadowingRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ShadowingViewModel::class.java)) {
                return ShadowingViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed class ShadowingUiState {
    object Idle : ShadowingUiState()
    object Recording : ShadowingUiState()
    object Analyzing : ShadowingUiState()
    data class Result(val response: ShadowingResponse) : ShadowingUiState()
    data class Error(val message: String) : ShadowingUiState()
}
