package com.example.kotobee.ui.lessons.speaking

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.kotobee.data.model.SpeakingMessage
import com.example.kotobee.ui.home.CardBorderColor
import com.example.kotobee.ui.home.ProgressPrimary
import com.example.kotobee.ui.home.TextDark
import com.example.kotobee.ui.home.TextGray
import com.example.kotobee.ui.home.ThemeBackground
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakingChatScreen(
    topicId: String,
    viewModel: SpeakingViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val chatState by viewModel.chatState.collectAsState()
    val listState = rememberLazyListState()

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    var isListening by remember { mutableStateOf(false) }
    var partialText by remember { mutableStateOf("") }
    var manualText by remember { mutableStateOf("") }
    var speechError by remember { mutableStateOf<String?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    var ttsMessage by remember { mutableStateOf<String?>(null) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

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
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.JAPAN.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.JAPAN.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 2_500L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1_200L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 800L)
        }
    }

    DisposableEffect(speechRecognizer) {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                speechError = null
                partialText = ""
            }

            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() {
                isListening = false
            }

            override fun onError(error: Int) {
                isListening = false
                partialText = ""
                speechError = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "Mình chưa nghe rõ câu tiếng Nhật."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Chưa nhận được giọng nói."
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Không thể nhận diện giọng nói vì lỗi mạng."
                    else -> "Không thể nhận diện giọng nói. Hãy thử lại hoặc nhập text"
                }
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                val text = bestJapaneseRecognitionResult(results)
                partialText = ""
                if (text.isNotBlank()) {
                    viewModel.submitUserText(text)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                partialText = bestJapaneseRecognitionResult(partialResults)
            }

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })

        onDispose {
            speechRecognizer?.destroy()
        }
    }

    DisposableEffect(context) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = engine?.setLanguage(Locale.JAPANESE)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    ttsMessage = "Thiết bị chưa hỗ trợ giọng đọc tiếng Nhật."
                    isTtsReady = false
                } else {
                    isTtsReady = true
                }
            } else {
                ttsMessage = "Không thể khởi động TextToSpeech."
            }
        }
        tts = engine

        onDispose {
            engine?.stop()
            engine?.shutdown()
            tts = null
        }
    }

    LaunchedEffect(topicId) {
        viewModel.openTopic(topicId)
    }

    LaunchedEffect(chatState.messages.size) {
        if (chatState.messages.isNotEmpty()) {
            listState.animateScrollToItem(chatState.messages.lastIndex)
        }
    }

    LaunchedEffect(chatState.pendingTtsText, isTtsReady) {
        val text = chatState.pendingTtsText
        if (!text.isNullOrBlank() && isTtsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "speaking-${System.currentTimeMillis()}")
            viewModel.clearPendingTts()
        }
    }

    fun startListening() {
        if (!hasAudioPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        if (speechRecognizer == null) {
            speechError = "Thiết bị không hỗ trợ SpeechRecognizer. Bạn vẫn có thể nhập text để test ."
            return
        }
        if (chatState.isSending || chatState.isLoading) return

        speechError = null
        partialText = ""
        isListening = true
        runCatching {
            speechRecognizer.cancel()
            speechRecognizer.startListening(recognitionIntent)
        }.onFailure {
            isListening = false
            speechError = "Không thể mở micro. Hãy thử lại hoặc nhập text để test."
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
    }

    fun sendManualText() {
        val text = manualText.trim()
        if (text.isBlank() || chatState.isSending || chatState.isLoading) return
        manualText = ""
        speechError = null
        partialText = ""
        viewModel.submitUserText(text, source = "manual_text")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(chatState.topic?.title ?: "Luyện giao tiếp", fontWeight = FontWeight.Bold, color = TextDark)
                        chatState.topic?.level?.let { level ->
                            Text(level, color = TextGray, fontSize = 12.sp)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.startNewConversation() }) {
                        Icon(Icons.Default.Add, contentDescription = "Tạo phiên mới", tint = TextDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ThemeBackground)
            )
        },
        bottomBar = {
            SpeakingMicBar(
                isListening = isListening,
                isSending = chatState.isSending,
                partialText = partialText,
                manualText = manualText,
                errorMessage = speechError ?: chatState.errorMessage ?: ttsMessage,
                onManualTextChange = { manualText = it },
                onSendManualText = ::sendManualText,
                onMicClick = {
                    if (isListening) {
                        stopListening()
                    } else {
                        startListening()
                    }
                }
            )
        },
        containerColor = ThemeBackground
    ) { paddingValues ->
        when {
            chatState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ProgressPrimary)
                }
            }
            chatState.messages.isEmpty() && chatState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(chatState.errorMessage.orEmpty(), color = ProgressPrimary, fontWeight = FontWeight.Bold)
                }
            }
            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(chatState.messages, key = { it.id }) { message ->
                        SpeakingMessageBubble(message = message)
                    }

                    if (chatState.isSending) {
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, CardBorderColor)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(color = ProgressPrimary, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
                                        Text("AI đang trả lời...", color = TextGray, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeakingMessageBubble(message: SpeakingMessage) {
    val isUser = message.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            colors = CardDefaults.cardColors(containerColor = if (isUser) ProgressPrimary else Color.White),
            border = if (isUser) null else BorderStroke(1.dp, CardBorderColor)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = message.textJa,
                        color = if (isUser) Color.White else TextDark,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (!isUser && message.source != "topic_starter") {
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
                    }
                }
                if (!isUser && message.translationVi.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Nghĩa: ${message.translationVi}",
                        color = TextGray,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
                if (!isUser && message.feedbackVi.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(message.feedbackVi, color = TextGray, fontSize = 12.sp, lineHeight = 17.sp)
                }
                if (!isUser && message.correctionJa.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Sửa: ${message.correctionJa}", color = ProgressPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SpeakingMicBar(
    isListening: Boolean,
    isSending: Boolean,
    partialText: String,
    manualText: String,
    errorMessage: String?,
    onManualTextChange: (String) -> Unit,
    onSendManualText: () -> Unit,
    onMicClick: () -> Unit
) {
    val canSendText = manualText.trim().isNotEmpty() && !isSending

    Surface(
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = manualText,
                    onValueChange = onManualTextChange,
                    modifier = Modifier.weight(1f),
                    enabled = !isSending,
                    minLines = 1,
                    maxLines = 3,
                    placeholder = { Text("Nhập text", color = TextGray, fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSendManualText() })
                )
                IconButton(
                    onClick = onSendManualText,
                    enabled = canSendText,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (canSendText) ProgressPrimary else TextGray.copy(alpha = 0.16f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Gửi",
                        tint = if (canSendText) Color.White else TextGray,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (partialText.isNotBlank()) {
                Text(partialText, color = TextDark, fontSize = 15.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
            } else if (isListening) {
                Text("Đang nghe... chạm lại để dừng", color = Color(0xFFC62828), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
            } else if (errorMessage != null) {
                Text(errorMessage, color = ProgressPrimary, fontSize = 12.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                Text("Chạm mic để nói", color = TextGray, fontSize = 12.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        color = when {
                            isSending -> TextGray.copy(alpha = 0.18f)
                            isListening -> Color(0xFFE53935)
                            else -> ProgressPrimary
                        },
                        shape = CircleShape
                    )
                    .pointerInput(isSending) {
                        detectTapGestures(
                            onTap = {
                                if (!isSending) onMicClick()
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isListening) "Dừng ghi âm" else "Mic",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    }
}

private fun bestJapaneseRecognitionResult(results: Bundle?): String {
    val candidates = results
        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        .orEmpty()
        .map { it.trim() }
        .filter { it.isNotBlank() }

    return candidates.maxByOrNull(::japaneseTextScore).orEmpty()
}

private fun japaneseTextScore(text: String): Int {
    return text.fold(0) { score, char ->
        score + when (char) {
            in '\u3040'..'\u30ff' -> 3
            in '\u4e00'..'\u9faf' -> 3
            in '\uff66'..'\uff9f' -> 2
            else -> 0
        }
    }
}
