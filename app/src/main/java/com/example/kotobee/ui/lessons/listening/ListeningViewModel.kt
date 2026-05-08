package com.example.kotobee.ui.lessons.listening

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.kotobee.data.model.ListeningUiState
import com.example.kotobee.data.repository.ListeningRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ListeningViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ListeningRepository()
    private val _uiState = MutableStateFlow(ListeningUiState())
    val uiState = _uiState.asStateFlow()

    // 👉 ĐÃ ĐỔI: Dùng trực tiếp ExoPlayer thay vì MediaController/Service phức tạp
    private var player: ExoPlayer? = null

    init {
        startProgressTracker()
    }

    fun loadLessonData(lessonId: String) {
        viewModelScope.launch {
            try {
                // 1. Fetch dữ liệu từ Firebase thông qua Repository
                val data = repository.fetchLessonDetail(lessonId)
                _uiState.update { data }

                // 2. Khởi tạo Player chạy trực tiếp trên app
                if (data.audioUrl.isNotEmpty()) {
                    initializePlayer(getApplication(), data.audioUrl)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initializePlayer(context: Application, audioUrl: String) {
        // Khởi tạo ExoPlayer
        player = ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _uiState.update { it.copy(isPlaying = isPlaying) }
                }
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        _uiState.update { it.copy(totalDuration = duration) }
                    }
                }
            })
            // Load URL nhạc vào player
            setMediaItem(MediaItem.fromUri(audioUrl))
            prepare()
        }
    }

    private fun startProgressTracker() {
        viewModelScope.launch {
            while (true) {
                player?.let { p ->
                    if (p.isPlaying) {
                        _uiState.update { it.copy(currentPosition = p.currentPosition) }
                    }
                }
                delay(500L) // Cập nhật mỗi 0.5s để thanh progress mượt hơn
            }
        }
    }

    // --- Các hàm tương tác cho UI ---
    fun togglePlayPause() {
        player?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    fun seekForward() {
        player?.let { it.seekTo(it.currentPosition + 10000L) }
    }

    fun seekBackward() {
        player?.let { it.seekTo(it.currentPosition - 10000L) }
    }

    fun toggleTranslation() {
        _uiState.update { it.copy(showTranslation = !it.showTranslation) }
    }

    override fun onCleared() {
        // 👉 ĐÃ ĐỔI: Giải phóng ExoPlayer sòng phẳng khi thoát màn hình
        player?.release()
        player = null
        super.onCleared()
    }
}