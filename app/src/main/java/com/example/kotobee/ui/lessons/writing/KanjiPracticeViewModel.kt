package com.example.kotobee.ui.lessons.writing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotobee.data.model.DemoKanjiDto
import com.example.kotobee.data.repository.KanjiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class KanjiPracticeState(
    val isLoading: Boolean = true,
    val data: DemoKanjiDto? = null,
    val error: String? = null
)

class KanjiPracticeViewModel(
    private val repository: KanjiRepository
) : ViewModel() {

    private val _state = MutableStateFlow(KanjiPracticeState())
    val state: StateFlow<KanjiPracticeState> = _state.asStateFlow()

    fun loadKanjiData(character: String) {
        viewModelScope.launch {
            _state.value = KanjiPracticeState(isLoading = true)
            try {
                // Lấy data trực tiếp thay vì dùng collect
                val kanjiDto = repository.getKanjiDetail(character)

                if (kanjiDto != null) {
                    _state.value = KanjiPracticeState(isLoading = false, data = kanjiDto)
                } else {
                    _state.value = KanjiPracticeState(isLoading = false, error = "Chưa tải xong hoặc không tìm thấy chữ '$character'")
                }
            } catch (e: Exception) {
                _state.value = KanjiPracticeState(isLoading = false, error = e.message)
            }
        }
    }
}