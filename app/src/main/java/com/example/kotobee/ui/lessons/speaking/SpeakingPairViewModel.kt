package com.example.kotobee.ui.lessons.speaking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kotobee.data.model.SpeakingPairHistory
import com.example.kotobee.data.model.SpeakingPairMessage
import com.example.kotobee.data.model.SpeakingPairParticipant
import com.example.kotobee.data.model.SpeakingPairRoom
import com.example.kotobee.data.repository.SpeakingPairRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class SpeakingPairState(
    val history: List<SpeakingPairHistory> = emptyList(),
    val room: SpeakingPairRoom? = null,
    val participants: List<SpeakingPairParticipant> = emptyList(),
    val messages: List<SpeakingPairMessage> = emptyList(),
    val currentUserId: String = "",
    val isLoading: Boolean = false,
    val isBusy: Boolean = false,
    val isSending: Boolean = false,
    val errorMessage: String? = null
)

class SpeakingPairViewModel(
    private val repository: SpeakingPairRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SpeakingPairState())
    val state: StateFlow<SpeakingPairState> = _state.asStateFlow()

    private val registrations = mutableListOf<ListenerRegistration>()

    init {
        refreshHome()
    }

    fun refreshHome() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                val user = repository.getCurrentUser()
                user.userDocId to repository.getHistory()
            }.onSuccess { (userId, history) ->
                _state.value = _state.value.copy(
                    currentUserId = userId,
                    history = history,
                    isLoading = false
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Không thể tải phòng giao tiếp."
                )
            }
        }
    }

    fun createRoom() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isBusy = true, errorMessage = null)
            runCatching {
                repository.createRoom()
            }.onSuccess { code ->
                _state.value = _state.value.copy(isBusy = false)
                enterRoom(code)
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isBusy = false,
                    errorMessage = error.message ?: "Không thể tạo phòng."
                )
            }
        }
    }

    fun joinRoom(code: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isBusy = true, errorMessage = null)
            runCatching {
                repository.joinRoom(code)
            }.onSuccess { roomCode ->
                _state.value = _state.value.copy(isBusy = false)
                enterRoom(roomCode)
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isBusy = false,
                    errorMessage = error.message ?: "Không thể vào phòng."
                )
            }
        }
    }

    fun openRoom(code: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isBusy = true, errorMessage = null)
            runCatching {
                repository.openRoom(code)
            }.onSuccess { roomCode ->
                _state.value = _state.value.copy(isBusy = false)
                enterRoom(roomCode)
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isBusy = false,
                    errorMessage = error.message ?: "Không thể mở lịch sử phòng."
                )
            }
        }
    }

    fun submitTurn(audioFile: File, transcriptJa: String, durationMs: Long) {
        val room = _state.value.room ?: return
        if (_state.value.isSending) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isSending = true, errorMessage = null)
            runCatching {
                repository.submitMessage(
                    code = room.code,
                    audioFile = audioFile,
                    transcriptJa = transcriptJa,
                    durationMs = durationMs
                )
            }.onSuccess {
                _state.value = _state.value.copy(isSending = false)
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isSending = false,
                    errorMessage = error.message ?: "Không thể gửi lượt nói."
                )
            }
        }
    }

    fun finishRoom() {
        val room = _state.value.room ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isBusy = true, errorMessage = null)
            runCatching {
                repository.finishRoom(room.code)
            }.onSuccess {
                _state.value = _state.value.copy(isBusy = false)
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isBusy = false,
                    errorMessage = error.message ?: "Không thể kết thúc phòng."
                )
            }
        }
    }

    fun leaveRoom() {
        val room = _state.value.room
        clearRoomListeners()
        _state.value = _state.value.copy(
            room = null,
            participants = emptyList(),
            messages = emptyList(),
            errorMessage = null
        )
        if (room == null) {
            refreshHome()
            return
        }

        viewModelScope.launch {
            runCatching { repository.leaveRoom(room.code) }
            refreshHome()
        }
    }

    private suspend fun enterRoom(code: String) {
        clearRoomListeners()
        val currentUser = repository.getCurrentUser()
        _state.value = _state.value.copy(
            currentUserId = currentUser.userDocId,
            room = null,
            participants = emptyList(),
            messages = emptyList(),
            errorMessage = null
        )

        registrations += repository.observeRoom(
            code = code,
            onChange = { room ->
                _state.value = _state.value.copy(room = room)
            },
            onError = { error ->
                _state.value = _state.value.copy(errorMessage = error.message)
            }
        )
        registrations += repository.observeParticipants(
            code = code,
            onChange = { participants ->
                _state.value = _state.value.copy(participants = participants)
            },
            onError = { error ->
                _state.value = _state.value.copy(errorMessage = error.message)
            }
        )
        registrations += repository.observeMessages(
            code = code,
            onChange = { messages ->
                _state.value = _state.value.copy(messages = messages)
            },
            onError = { error ->
                _state.value = _state.value.copy(errorMessage = error.message)
            }
        )
    }

    private fun clearRoomListeners() {
        registrations.forEach { it.remove() }
        registrations.clear()
    }

    override fun onCleared() {
        clearRoomListeners()
        super.onCleared()
    }

    class Factory(private val repository: SpeakingPairRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SpeakingPairViewModel::class.java)) {
                return SpeakingPairViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
