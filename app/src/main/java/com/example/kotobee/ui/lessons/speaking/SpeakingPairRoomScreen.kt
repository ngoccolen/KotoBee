package com.example.kotobee.ui.lessons.speaking

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.kotobee.data.model.SpeakingPairHistory
import com.example.kotobee.data.model.SpeakingPairMessage
import com.example.kotobee.data.model.SpeakingPairParticipant
import com.example.kotobee.data.model.SpeakingPairRoom
import com.example.kotobee.data.model.SpeakingPairTurnFeedback
import com.example.kotobee.ui.home.CardBorderColor
import com.example.kotobee.ui.home.ProgressPrimary
import com.example.kotobee.ui.home.ProgressTrack
import com.example.kotobee.ui.home.TextDark
import com.example.kotobee.ui.home.TextGray
import com.example.kotobee.ui.home.ThemeBackground
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakingPairRoomScreen(
    viewModel: SpeakingPairViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val room = state.room

    if (room == null) {
        SpeakingPairHomeContent(
            state = state,
            onCreateRoom = viewModel::createRoom,
            onJoinRoom = viewModel::joinRoom,
            onOpenHistory = viewModel::openRoom,
            onRefresh = viewModel::refreshHome,
            onBackClick = onBackClick
        )
        return
    }

    when (room.status) {
        "waiting" -> SpeakingPairLobbyContent(
            room = room,
            participants = state.participants,
            errorMessage = state.errorMessage,
            onLeave = viewModel::leaveRoom
        )
        "cancelled" -> SpeakingPairClosedContent(
            title = "Phòng đã bị hủy",
            message = "Host đã rời phòng trước khi hội thoại bắt đầu.",
            onBack = viewModel::leaveRoom
        )
        else -> SpeakingPairChatContent(
            state = state,
            onSubmitTurn = viewModel::submitTurn,
            onFinishRoom = viewModel::finishRoom,
            onLeave = viewModel::leaveRoom
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpeakingPairHomeContent(
    state: SpeakingPairState,
    onCreateRoom: () -> Unit,
    onJoinRoom: (String) -> Unit,
    onOpenHistory: (String) -> Unit,
    onRefresh: () -> Unit,
    onBackClick: () -> Unit
) {
    var roomCode by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Giao tiếp 2 người", fontWeight = FontWeight.Bold, color = TextDark) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Tải lại", tint = TextDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ThemeBackground)
            )
        },
        containerColor = ThemeBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    "Tạo phòng riêng để hai người tự nói chuyện, ghi âm theo lượt và lưu lại transcript tiếng Nhật.",
                    color = TextGray,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            state.errorMessage?.let { error ->
                item { SpeakingPairNotice(text = error, color = Color(0xFFC62828)) }
            }

            item {
                SpeakingPairPanel {
                    Text("Vào phòng", color = TextDark, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = roomCode,
                        onValueChange = { value ->
                            roomCode = value.filter { it.isLetterOrDigit() }.take(6).uppercase()
                        },
                        label = { Text("Mã phòng") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        modifier = Modifier.fillMaxWidth(),
                        colors = speakingPairTextFieldColors()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onJoinRoom(roomCode) },
                        enabled = !state.isBusy && roomCode.length == 6,
                        colors = ButtonDefaults.buttonColors(containerColor = ProgressPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Vào phòng", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                SpeakingPairPanel {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        PairIconBox()
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tạo phòng mới", color = TextDark, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Không cần chủ đề. Bạn và người còn lại tự chọn nội dung để luyện nói.",
                                color = TextGray,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onCreateRoom,
                        enabled = !state.isBusy,
                        colors = ButtonDefaults.buttonColors(containerColor = ProgressPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (state.isBusy) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tạo phòng tự do", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Text("Lịch sử phòng", color = TextDark, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            }

            if (state.history.isEmpty()) {
                item {
                    SpeakingPairPanel {
                        Text("Chưa có phòng giao tiếp nào.", color = TextGray, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
            } else {
                items(state.history, key = { it.roomCode }) { history ->
                    PairHistoryCard(history = history, onClick = { onOpenHistory(history.roomCode) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpeakingPairLobbyContent(
    room: SpeakingPairRoom,
    participants: List<SpeakingPairParticipant>,
    errorMessage: String?,
    onLeave: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lobby giao tiếp", fontWeight = FontWeight.Bold, color = TextDark) },
                navigationIcon = {
                    IconButton(onClick = onLeave) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ThemeBackground)
            )
        },
        containerColor = ThemeBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SpeakingPairPanel {
                Text("Mã phòng", color = TextGray, fontSize = 13.sp)
                Text(room.code, color = ProgressPrimary, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(room.topicTitle, color = TextDark, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(room.scenario, color = TextGray, fontSize = 13.sp, lineHeight = 18.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { (participants.size / 2f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = ProgressPrimary,
                    trackColor = ProgressTrack
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Đang chờ người thứ 2 vào phòng", color = TextGray, fontSize = 12.sp)
            }

            errorMessage?.let { SpeakingPairNotice(text = it, color = Color(0xFFC62828)) }

            SpeakingPairPanel {
                Text("Người tham gia", color = TextDark, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(12.dp))
                participants.forEachIndexed { index, participant ->
                    PairParticipantRow(participant = participant)
                    if (index < participants.lastIndex) HorizontalDivider(color = ProgressTrack, modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            OutlinedButton(onClick = onLeave, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Rời phòng", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpeakingPairChatContent(
    state: SpeakingPairState,
    onSubmitTurn: (File, String, Long) -> Unit,
    onFinishRoom: () -> Unit,
    onLeave: () -> Unit
) {
    val context = LocalContext.current
    val room = state.room ?: return
    val listState = rememberLazyListState()
    val currentUserId = state.currentUserId
    val isActive = room.status == "active"
    val isCurrentTurn = isActive && room.currentTurnUserId == currentUserId
    val currentTurnName = state.participants.firstOrNull { it.userId == room.currentTurnUserId }?.username.orEmpty()

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var recordingFile by remember { mutableStateOf<File?>(null) }
    var recordingStartedAt by remember { mutableStateOf(0L) }
    var isRecording by remember { mutableStateOf(false) }
    var pendingAudioFile by remember { mutableStateOf<File?>(null) }
    var pendingDurationMs by remember { mutableStateOf(0L) }
    var transcriptText by remember { mutableStateOf("") }
    var partialText by remember { mutableStateOf("") }
    var recorderError by remember { mutableStateOf<String?>(null) }
    var playingUrl by remember { mutableStateOf<String?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasAudioPermission = granted }
    )

    val speechRecognizer = remember(context) {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
    }
    val recognitionIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ja-JP")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
    }

    fun playAudio(url: String) {
        if (url.isBlank()) return
        mediaPlayer?.release()
        playingUrl = url
        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            setOnPreparedListener { it.start() }
            setOnCompletionListener {
                it.release()
                if (playingUrl == url) playingUrl = null
                if (mediaPlayer == it) mediaPlayer = null
            }
            setOnErrorListener { player, _, _ ->
                player.release()
                if (playingUrl == url) playingUrl = null
                if (mediaPlayer == player) mediaPlayer = null
                true
            }
            prepareAsync()
        }
    }

    fun stopRecording() {
        if (!isRecording) return
        val file = recordingFile
        val duration = (System.currentTimeMillis() - recordingStartedAt).coerceAtLeast(0L)

        runCatching { recorder?.stop() }.onFailure {
            recorderError = "Bản ghi quá ngắn hoặc không thể lưu. Hãy thử lại."
            file?.delete()
        }
        recorder?.release()
        recorder = null
        isRecording = false
        recordingFile = null
        pendingDurationMs = duration
        if (file != null && file.exists()) {
            pendingAudioFile = file
            if (transcriptText.isBlank() && partialText.isNotBlank()) transcriptText = partialText
        }
        speechRecognizer?.stopListening()
    }

    fun startRecording() {
        if (!isCurrentTurn || state.isSending) return
        if (!hasAudioPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        val file = File(context.cacheDir, "pair_voice_${room.code}_${System.currentTimeMillis()}.m4a")
        transcriptText = ""
        partialText = ""
        recorderError = null
        pendingAudioFile = null
        pendingDurationMs = 0L

        @Suppress("DEPRECATION")
        val mediaRecorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }

        recorder = mediaRecorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            runCatching {
                prepare()
                start()
            }.onSuccess {
                recordingFile = file
                recordingStartedAt = System.currentTimeMillis()
                isRecording = true
                if (speechRecognizer == null) {
                    recorderError = "Thiết bị không hỗ trợ nhận diện giọng nói, audio vẫn sẽ được lưu."
                } else {
                    speechRecognizer.startListening(recognitionIntent)
                }
            }.onFailure {
                release()
                recorder = null
                recorderError = "Không thể bắt đầu ghi âm."
            }
        }
    }

    DisposableEffect(speechRecognizer) {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) = Unit
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit

            override fun onError(error: Int) {
                recorderError = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "Chưa nhận diện được transcript, bạn vẫn có thể gửi audio."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Chưa nghe rõ transcript, bạn vẫn có thể gửi audio."
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Lỗi mạng khi nhận diện giọng nói."
                    else -> "Không thể nhận diện transcript."
                }
            }

            override fun onResults(results: Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                if (text.isNotBlank()) transcriptText = text
                partialText = ""
            }

            override fun onPartialResults(partialResults: Bundle?) {
                partialText = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
            }

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })

        onDispose {
            speechRecognizer?.destroy()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { recorder?.stop() }
            recorder?.release()
            mediaPlayer?.release()
        }
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(room.topicTitle, fontWeight = FontWeight.Bold, color = TextDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            when {
                                room.status == "finished" -> "Đã kết thúc"
                                isCurrentTurn -> "Đến lượt bạn"
                                else -> "Lượt của ${currentTurnName.ifBlank { "người còn lại" }}"
                            },
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onLeave) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
                    }
                },
                actions = {
                    if (room.status == "active") {
                        TextButton(onClick = onFinishRoom) {
                            Text("Kết thúc", color = ProgressPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ThemeBackground)
            )
        },
        bottomBar = {
            PairVoiceInputBar(
                isCurrentTurn = isCurrentTurn,
                isRecording = isRecording,
                isSending = state.isSending,
                partialText = partialText,
                transcriptText = transcriptText,
                errorMessage = recorderError ?: state.errorMessage,
                pendingAudioFile = pendingAudioFile,
                statusText = when {
                    room.status == "finished" -> "Phòng đã kết thúc. Bạn vẫn có thể nghe lại lịch sử."
                    isCurrentTurn -> "Giữ mic để ghi lượt nói của bạn"
                    else -> "Đợi ${currentTurnName.ifBlank { "người còn lại" }} ghi âm"
                },
                onPressStart = ::startRecording,
                onPressEnd = ::stopRecording,
                onSend = {
                    val file = pendingAudioFile ?: return@PairVoiceInputBar
                    onSubmitTurn(file, transcriptText, pendingDurationMs)
                    pendingAudioFile = null
                    pendingDurationMs = 0L
                    transcriptText = ""
                    partialText = ""
                },
                onDiscard = {
                    pendingAudioFile?.delete()
                    pendingAudioFile = null
                    pendingDurationMs = 0L
                    transcriptText = ""
                    partialText = ""
                    recorderError = null
                }
            )
        },
        containerColor = ThemeBackground
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                SpeakingPairScenarioCard(room = room, participants = state.participants)
            }
            items(state.messages, key = { it.id }) { message ->
                PairMessageBubble(
                    message = message,
                    isMine = message.senderUserId == currentUserId,
                    feedback = if (message.senderUserId == currentUserId) state.turnFeedback[message.id] else null,
                    isPlaying = playingUrl == message.audioUrl,
                    onPlayAudio = { playAudio(message.audioUrl) }
                )
            }
            if (state.messages.isEmpty()) {
                item {
                    Text(
                        "Chưa có lượt nói nào. Người host sẽ nói trước.",
                        color = TextGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeakingPairClosedContent(title: String, message: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Close, contentDescription = null, tint = ProgressPrimary, modifier = Modifier.size(42.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, color = TextDark, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
        Text(message, color = TextGray, fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 18.sp)
        Spacer(modifier = Modifier.height(18.dp))
        Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = ProgressPrimary)) {
            Text("Quay lại", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PairVoiceInputBar(
    isCurrentTurn: Boolean,
    isRecording: Boolean,
    isSending: Boolean,
    partialText: String,
    transcriptText: String,
    errorMessage: String?,
    pendingAudioFile: File?,
    statusText: String,
    onPressStart: () -> Unit,
    onPressEnd: () -> Unit,
    onSend: () -> Unit,
    onDiscard: () -> Unit
) {
    Surface(color = Color.White, shadowElevation = 8.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (pendingAudioFile != null) {
                Text(
                    transcriptText.ifBlank { "Không có transcript, audio vẫn sẽ được gửi." },
                    color = TextDark,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onDiscard, modifier = Modifier.weight(1f), enabled = !isSending) {
                        Text("Ghi lại")
                    }
                    Button(
                        onClick = onSend,
                        modifier = Modifier.weight(1f),
                        enabled = !isSending,
                        colors = ButtonDefaults.buttonColors(containerColor = ProgressPrimary)
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Gửi lượt", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                when {
                    partialText.isNotBlank() -> Text(partialText, color = TextDark, fontSize = 14.sp, textAlign = TextAlign.Center)
                    errorMessage != null -> Text(errorMessage, color = ProgressPrimary, fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 17.sp)
                    else -> Text(statusText, color = TextGray, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            color = when {
                                !isCurrentTurn || isSending -> TextGray.copy(alpha = 0.18f)
                                isRecording -> Color(0xFFC62828)
                                else -> ProgressPrimary
                            },
                            shape = CircleShape
                        )
                        .pointerInput(isCurrentTurn, isSending) {
                            detectTapGestures(
                                onPress = {
                                    if (isCurrentTurn && !isSending) {
                                        onPressStart()
                                        tryAwaitRelease()
                                        onPressEnd()
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Mic",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PairHistoryCard(history: SpeakingPairHistory, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).border(1.dp, CardBorderColor, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onClick() })
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            PairIconBox()
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(history.topicTitle.ifBlank { history.roomCode }, color = TextDark, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    listOfNotNull(
                        history.partnerName.takeIf { it.isNotBlank() }?.let { "với $it" },
                        "${history.messageCount} lượt",
                        formatPairDate(history.updatedAt)
                    ).joinToString(" · "),
                    color = TextGray,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextGray)
        }
    }
}

@Composable
private fun SpeakingPairScenarioCard(room: SpeakingPairRoom, participants: List<SpeakingPairParticipant>) {
    SpeakingPairPanel {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(room.topicTitle, color = TextDark, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                Text(room.level, color = ProgressPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Text("${participants.size}/2", color = TextGray, fontSize = 12.sp)
        }
        if (room.scenario.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(room.scenario, color = TextGray, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun PairMessageBubble(
    message: SpeakingPairMessage,
    isMine: Boolean,
    feedback: SpeakingPairTurnFeedback?,
    isPlaying: Boolean,
    onPlayAudio: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 310.dp),
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isMine) 18.dp else 4.dp,
                bottomEnd = if (isMine) 4.dp else 18.dp
            ),
            colors = CardDefaults.cardColors(containerColor = if (isMine) ProgressPrimary else Color.White),
            border = if (isMine) null else BorderStroke(1.dp, CardBorderColor)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    message.senderName.ifBlank { "Người học" },
                    color = if (isMine) Color.White.copy(alpha = 0.82f) else TextGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPlayAudio,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            if (isPlaying) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.PlayArrow,
                            contentDescription = "Nghe lại",
                            tint = if (isMine) Color.White else ProgressPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (message.durationMs > 0L) "${message.durationMs / 1000}s" else "Audio",
                            color = if (isMine) Color.White else TextDark,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Lượt ${message.turnIndex + 1}",
                            color = if (isMine) Color.White.copy(alpha = 0.75f) else TextGray,
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    message.transcriptJa.ifBlank { "Không có transcript." },
                    color = if (isMine) Color.White else TextDark,
                    fontSize = 15.sp,
                    lineHeight = 21.sp
                )
                if (feedback != null) {
                    PairTurnFeedbackBlock(feedback = feedback, isMine = isMine)
                }
            }
        }
    }
}

@Composable
private fun PairTurnFeedbackBlock(feedback: SpeakingPairTurnFeedback, isMine: Boolean) {
    Spacer(modifier = Modifier.height(10.dp))
    HorizontalDivider(color = if (isMine) Color.White.copy(alpha = 0.28f) else CardBorderColor)
    Spacer(modifier = Modifier.height(8.dp))

    when (feedback.status) {
        "pending" -> Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(
                color = if (isMine) Color.White else ProgressPrimary,
                modifier = Modifier.size(14.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "AI đang kiểm tra...",
                color = if (isMine) Color.White.copy(alpha = 0.82f) else TextGray,
                fontSize = 12.sp
            )
        }
        "error" -> Text(
            feedback.errorMessage.ifBlank { "AI chưa thể kiểm tra lượt nói này." },
            color = if (isMine) Color.White.copy(alpha = 0.82f) else ProgressPrimary,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
        else -> {
            if (feedback.summaryVi.isNotBlank()) {
                Text(
                    feedback.summaryVi,
                    color = if (isMine) Color.White.copy(alpha = 0.9f) else TextDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 17.sp
                )
            }
            if (feedback.correctedSentenceJa.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Sửa: ${feedback.correctedSentenceJa}",
                    color = if (isMine) Color.White else ProgressPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 17.sp
                )
            }
            if (feedback.naturalSentenceJa.isNotBlank() && feedback.naturalSentenceJa != feedback.correctedSentenceJa) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Tự nhiên hơn: ${feedback.naturalSentenceJa}",
                    color = if (isMine) Color.White.copy(alpha = 0.88f) else TextGray,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
            if (feedback.grammarFeedbackVi.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Ngữ pháp: ${feedback.grammarFeedbackVi}",
                    color = if (isMine) Color.White.copy(alpha = 0.82f) else TextGray,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
            if (feedback.pronunciationFeedbackVi.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Phát âm: ${feedback.pronunciationFeedbackVi}",
                    color = if (isMine) Color.White.copy(alpha = 0.82f) else TextGray,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun PairParticipantRow(participant: SpeakingPairParticipant) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(Color.White, CircleShape)
                .border(1.dp, CardBorderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(participant.username.trim().take(1).ifBlank { "?" }.uppercase(), color = ProgressPrimary, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(participant.username.ifBlank { "Người học" }, color = TextDark, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(if (participant.isHost) "Host" else "Người tham gia", color = TextGray, fontSize = 12.sp)
        }
        Icon(
            if (participant.isOnline) Icons.Default.CheckCircle else Icons.Default.Close,
            contentDescription = null,
            tint = if (participant.isOnline) Color(0xFF2E7D32) else TextGray,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SpeakingPairPanel(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun SpeakingPairNotice(text: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Info, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, color = color, fontSize = 13.sp, lineHeight = 18.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PairIconBox() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(Color.White, RoundedCornerShape(14.dp))
            .border(1.dp, CardBorderColor, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Groups, contentDescription = null, tint = ProgressPrimary, modifier = Modifier.size(26.dp))
    }
}

@Composable
private fun speakingPairTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    disabledContainerColor = Color.White,
    focusedBorderColor = ProgressPrimary,
    unfocusedBorderColor = CardBorderColor,
    focusedLabelColor = ProgressPrimary,
    unfocusedLabelColor = TextGray,
    cursorColor = ProgressPrimary
)

private fun formatPairDate(timestamp: Long): String {
    if (timestamp <= 0L) return "Mới đây"
    return SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(timestamp))
}
