package com.example.kotobee.ui.lessons.grammar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotobee.data.model.Grammar
import com.example.kotobee.data.model.GrammarQuestion
import com.example.kotobee.data.repository.GrammarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GrammarViewModel : ViewModel() {
    private val repository = GrammarRepository()

    // Danh sách bài học theo cấp độ (N5, N4...)
    private val _lessons = MutableStateFlow<List<Grammar>>(emptyList())
    val lessons: StateFlow<List<Grammar>> = _lessons

    // Chi tiết một bài học ngữ pháp đang chọn
    private val _currentGrammar = MutableStateFlow<Grammar?>(null)
    val currentGrammar: StateFlow<Grammar?> = _currentGrammar

    // Trạng thái loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Load danh sách bài học (Dùng cho màn hình Dashboard)
    fun loadLessonsByLevel(level: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _lessons.value = repository.getGrammarLessonsByLevel(level)
            _isLoading.value = false
        }
    }

    // Load chi tiết 1 bài học (Dùng cho màn hình Detail)
    fun loadGrammarDetail(grammarId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            // Trong thực tế bạn có thể tạo hàm getGrammarById trong repository
            // Tạm thời lấy từ list hiện tại nếu đã load
            _currentGrammar.value = _lessons.value.find { it.id == grammarId }
            _isLoading.value = false
        }
    }
}