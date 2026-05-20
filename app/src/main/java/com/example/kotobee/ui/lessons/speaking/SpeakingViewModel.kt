package com.example.kotobee.ui.lessons.speaking

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kotobee.data.model.ConversationTopic
import com.example.kotobee.data.model.SpeakingConversation
import com.example.kotobee.data.model.SpeakingMessage
import com.example.kotobee.data.repository.SpeakingConversationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class SpeakingChatState(
    val topic: ConversationTopic? = null,
    val conversation: SpeakingConversation? = null,
    val messages: List<SpeakingMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val errorMessage: String? = null,
    val pendingTtsText: String? = null
)

class SpeakingViewModel(
    private val repository: SpeakingConversationRepository
) : ViewModel() {

    private companion object {
        const val TAG = "SpeakingAI"
    }

    private val _topics = MutableStateFlow<List<ConversationTopic>>(emptyList())
    val topics: StateFlow<List<ConversationTopic>> = _topics.asStateFlow()

    private val _topicsLoading = MutableStateFlow(false)
    val topicsLoading: StateFlow<Boolean> = _topicsLoading.asStateFlow()

    private val _topicsError = MutableStateFlow<String?>(null)
    val topicsError: StateFlow<String?> = _topicsError.asStateFlow()

    private val _chatState = MutableStateFlow(SpeakingChatState())
    val chatState: StateFlow<SpeakingChatState> = _chatState.asStateFlow()

    init {
        loadTopics()
    }

    fun loadTopics() {
        if (_topicsLoading.value) return

        viewModelScope.launch {
            _topicsLoading.value = true
            _topicsError.value = null
            runCatching {
                repository.getTopics()
            }.onSuccess { data ->
                _topics.value = data
            }.onFailure { error ->
                Log.e(TAG, "Failed to load conversation topics", error)
                _topics.value = emptyList()
                _topicsError.value = error.message ?: "Không thể tải chủ đề luyện giao tiếp"
            }
            _topicsLoading.value = false
        }
    }

    fun openTopic(topicId: String, forceNew: Boolean = false) {
        viewModelScope.launch {
            _chatState.value = _chatState.value.copy(isLoading = true, errorMessage = null, pendingTtsText = null)
            runCatching {
                val topic = repository.getTopic(topicId) ?: error("Không tìm thấy chủ đề luyện giao tiếp.")
                val (conversation, messages) = if (forceNew) {
                    repository.createConversation(topic)
                } else {
                    repository.loadLatestOrCreateConversation(topic)
                }
                SpeakingChatState(
                    topic = topic,
                    conversation = conversation,
                    messages = messages,
                    isLoading = false
                )
            }.onSuccess { state ->
                _chatState.value = state
            }.onFailure { error ->
                Log.e(TAG, "Failed to open conversation topic", error)
                _chatState.value = _chatState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Không thể mở cuộc hội thoại"
                )
            }
        }
    }

    fun startNewConversation() {
        val topicId = _chatState.value.topic?.id ?: return
        openTopic(topicId, forceNew = true)
    }

    fun submitUserText(text: String, source: String = "speech_recognizer") {
        val cleanText = text.trim()
        if (cleanText.isBlank()) return

        val currentState = _chatState.value
        val topic = currentState.topic ?: return
        val conversation = currentState.conversation ?: return
        if (currentState.isSending) return

        viewModelScope.launch {
            _chatState.value = currentState.copy(isSending = true, errorMessage = null, pendingTtsText = null)
            val messagesBeforeUser = currentState.messages

            runCatching {
                val userMessage = repository.saveUserMessage(
                    conversationId = conversation.id,
                    textJa = cleanText,
                    turnIndex = messagesBeforeUser.size,
                    source = source
                )
                val withUser = messagesBeforeUser + userMessage
                _chatState.value = _chatState.value.copy(messages = withUser)

                val response = repository.requestAiResponse(
                    topic = topic,
                    userText = cleanText,
                    recentMessages = messagesBeforeUser
                )
                val aiMessage = repository.saveAiMessage(
                    conversationId = conversation.id,
                    response = response,
                    turnIndex = withUser.size
                )
                aiMessage
            }.onSuccess { aiMessage ->
                _chatState.value = _chatState.value.copy(
                    messages = _chatState.value.messages + aiMessage,
                    isSending = false,
                    pendingTtsText = aiMessage.textJa
                )
            }.onFailure { error ->
                Log.e(TAG, "Failed to get AI conversation response", error)
                _chatState.value = _chatState.value.copy(
                    isSending = false,
                    errorMessage = conversationErrorMessage(error)
                )
            }
        }
    }

    fun clearPendingTts() {
        _chatState.value = _chatState.value.copy(pendingTtsText = null)
    }

    private fun conversationErrorMessage(error: Throwable): String {
        return when (error) {
            is HttpException -> {
                val detail = runCatching { error.response()?.errorBody()?.string() }.getOrNull()
                if (!detail.isNullOrBlank()) {
                    Log.e(TAG, "Conversation HTTP ${error.code()}: ${detail.take(500)}")
                }
                "Backend AI trả lỗi ${error.code()}. Hãy kiểm tra Render hoặc GEMINI_API_KEY."
            }
            is IOException -> "Không kết nối được backend AI. Kiểm tra mạng hoặc Render đã bật."
            else -> error.message ?: "Không thể lấy phản hồi từ AI"
        }
    }

    class Factory(private val repository: SpeakingConversationRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SpeakingViewModel::class.java)) {
                return SpeakingViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
