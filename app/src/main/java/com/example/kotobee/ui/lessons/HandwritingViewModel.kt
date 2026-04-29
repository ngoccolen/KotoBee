package com.example.kotobee.ui.lessons

import androidx.compose.ui.graphics.Path
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class KanjiDetail(
    val character: String,
    val meaning: String,
    val onyomi: String,
    val kunyomi: String,
    val strokeCount: Int
)

class HandwritingViewModel : ViewModel() {

    private val _kanjiDetail = MutableStateFlow(
        KanjiDetail("水", "Thủy (Nước)", "スイ (sui)", "みず (mizu)", 4)
    )
    val kanjiDetail: StateFlow<KanjiDetail> = _kanjiDetail.asStateFlow()

    private val _paths = MutableStateFlow<List<Path>>(emptyList())
    val paths: StateFlow<List<Path>> = _paths.asStateFlow()

    private val _showGuide = MutableStateFlow(true)
    val showGuide: StateFlow<Boolean> = _showGuide.asStateFlow()

    private val _score = MutableStateFlow<Int?>(null)
    val score: StateFlow<Int?> = _score.asStateFlow()

    fun addPath(path: Path) {
        _paths.value = _paths.value + path
    }

    fun undo() {
        if (_paths.value.isNotEmpty()) {
            _paths.value = _paths.value.dropLast(1)
        }
    }

    fun clear() {
        _paths.value = emptyList()
        _score.value = null
    }

    fun toggleGuide() {
        _showGuide.value = !_showGuide.value
    }

    fun checkScore() {
        _score.value = (70..100).random()
    }
}
