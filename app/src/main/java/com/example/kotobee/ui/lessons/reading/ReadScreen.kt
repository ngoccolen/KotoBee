package com.example.kotobee.ui.lessons.reading

import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImage
import com.example.kotobee.data.model.VocabDetail

val KotoBeeOrange = Color(0xFFE53935)
val KotoBeeSurface = Color.White
val TextDark = Color(0xFF333333)

// ==========================================
// 1. MÀN HÌNH DANH SÁCH BÀI BÁO
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsListScreen(
    viewModel: ReadingViewModel,
    onArticleClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val filteredNewsList by viewModel.filteredNewsList.collectAsState()
    val selectedLevel by viewModel.selectedLevel.collectAsState()

    val levels = listOf(
        "Tất cả",
        "Yêu thích",
        ReadingDifficulty.EASY.label,
        ReadingDifficulty.MEDIUM.label,
        ReadingDifficulty.HARD.label
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tin Tức NHK", fontWeight = FontWeight.ExtraBold, color = TextDark) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KotoBeeSurface),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Back", tint = TextDark)
                    }
                }
            )
        },
        containerColor = KotoBeeSurface
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(levels) { level ->
                    FilterChip(
                        selected = selectedLevel == level,
                        onClick = { viewModel.setFilterLevel(level) },
                        label = { Text(level, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        leadingIcon = if (level == "Yêu thích") {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = "Yêu thích",
                                    tint = if (selectedLevel == level) KotoBeeOrange else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KotoBeeOrange.copy(alpha = 0.15f),
                            selectedLabelColor = KotoBeeOrange
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (selectedLevel == level) KotoBeeOrange else Color.LightGray,
                            enabled = true,
                            selected = selectedLevel == level
                        )
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (filteredNewsList.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (selectedLevel == "Yêu thích") "Bạn chưa có bài báo yêu thích nào." else "Chưa có bài báo nào ở cấp độ này.",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                items(filteredNewsList) { article ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onArticleClick(article.newsId) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, KotoBeeOrange),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = KotoBeeOrange.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = article.difficulty,
                                            color = KotoBeeOrange,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }

                                    if (article.isFavorite) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.Filled.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(13.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Box(modifier = Modifier.height(48.dp)) {
                                    FuriganaText(htmlContent = article.titleWithRuby, isTitleMode = true, onTextSelected = {})
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = article.date.take(10), fontSize = 11.sp, color = Color.Gray)
                            }

                            AsyncImage(
                                model = article.imageUrl.ifEmpty { "https://via.placeholder.com/150x150.png?text=No+Image" },
                                contentDescription = "Cover Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(Color.LightGray)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. MÀN HÌNH ĐỌC CHI TIẾT
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingPracticeScreen(newsId: String, viewModel: ReadingViewModel, onBackClick: () -> Unit) {
    val article by viewModel.currentArticle.collectAsState()
    val selectedVocab by viewModel.selectedVocab.collectAsState()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var highlightedText by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    // Lắng nghe kết quả lưu sổ tay
    LaunchedEffect(Unit) {
        viewModel.saveToNotebookResult.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(newsId) {
        viewModel.initTTS(context)
        viewModel.loadArticleDetail(newsId)
    }

    if (article == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = KotoBeeOrange)
        }
        return
    }

    Scaffold(
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            AnimatedVisibility(visible = highlightedText.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            viewModel.translateSelectedText(highlightedText)
                            highlightedText = ""
                        },
                        icon = { Icon(Icons.Outlined.Translate, contentDescription = "Dịch") },
                        text = { Text("Dịch AI") },
                        containerColor = KotoBeeOrange,
                        contentColor = Color.White
                    )
                    FloatingActionButton(
                        onClick = { highlightedText = "" },
                        containerColor = Color.LightGray,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Hủy", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = article!!.imageUrl.ifEmpty { "https://via.placeholder.com/800x400.png?text=No+Image" },
                    contentDescription = "Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                )

                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(top = 16.dp, start = 16.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                IconButton(
                    onClick = { viewModel.toggleFavorite(article!!.newsId) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 16.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (article!!.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Yêu thích",
                        tint = if (article!!.isFavorite) Color.Red else Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AccessTime, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = article!!.date, fontSize = 13.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(12.dp))

                FuriganaText(
                    htmlContent = "<h2>${article!!.titleWithRuby}</h2>",
                    isTitleMode = true,
                    onTextSelected = { text ->
                        val trimmed = text.trim()
                        if (trimmed.isNotEmpty()) highlightedText = trimmed
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(24.dp))

                FuriganaText(
                    htmlContent = article!!.htmlContent,
                    onTextSelected = { text ->
                        val trimmed = text.trim()
                        if (trimmed.isNotEmpty()) highlightedText = trimmed
                    }
                )

                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        if (selectedVocab != null) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.clearSelectedVocab() },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                VocabDetailSheet(
                    vocab = selectedVocab!!,
                    onSaveClick = {
                        viewModel.saveVocabToNotebook(
                            word = selectedVocab!!.word,
                            meaning = selectedVocab!!.meaning
                        )
                        viewModel.clearSelectedVocab()
                    },
                    onSpeakClick = { viewModel.speak(selectedVocab!!.word) }
                )
            }
        }
    }
}

// ==========================================
// 3. WEBVIEW HIỂN THỊ FURIGANA
// ==========================================
@Composable
fun FuriganaText(
    htmlContent: String,
    modifier: Modifier = Modifier,
    isTitleMode: Boolean = false,
    onTextSelected: (String) -> Unit
) {
    val fontSize = if (isTitleMode) "18px" else "16px"
    val fontWeight = if (isTitleMode) "bold" else "normal"
    val lineHeight = if (isTitleMode) "1.5" else "2.2"
    val textColor = "#333333"
    val rubyColor = "#F57C00"

    val styledHtml = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
            <style>
                body {
                    font-family: sans-serif; 
                    font-size: $fontSize; 
                    line-height: $lineHeight; 
                    font-weight: $fontWeight;
                    color: $textColor; 
                    background-color: transparent; 
                    padding: 0; margin: 0; 
                    word-wrap: break-word;
                    user-select: text; 
                    -webkit-user-select: text;
                }
                ruby { ruby-align: center; }
                rt { font-size: 10px; color: $rubyColor; font-weight: bold; }
                h2 { margin: 0; font-size: 20px; font-weight: 900; line-height: 1.5;}
                a { color: $rubyColor; text-decoration: none; }
            </style>
        </head>
        <body>
            $htmlContent
            <script type="text/javascript">
                document.addEventListener("selectionchange", function() {
                    var selectedText = window.getSelection().toString();
                    AndroidInterface.onSelectionChanged(selectedText);
                });
            </script>
        </body>
        </html>
    """.trimIndent()

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            WebView(context).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                settings.apply {
                    javaScriptEnabled = true
                    defaultTextEncodingName = "utf-8"
                }
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onSelectionChanged(text: String) {
                        post {
                            onTextSelected(text)
                        }
                    }
                }, "AndroidInterface")
                webViewClient = WebViewClient()
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(null, styledHtml, "text/html", "utf-8", null)
        }
    )
}

// ==========================================
// 4. CÁC COMPONENT PHỤ TRỢ
// ==========================================
@Composable
fun AudioPlayerBar(isPlaying: Boolean, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = KotoBeeOrange.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, KotoBeeOrange.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                contentDescription = null,
                tint = KotoBeeOrange,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (isPlaying) "Đang phát audio..." else "Nghe bài báo",
                color = KotoBeeOrange,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun VocabDetailSheet(vocab: VocabDetail, onSaveClick: () -> Unit, onSpeakClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 40.dp, top = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(vocab.furigana, fontSize = 14.sp, color = Color.Gray)
                Text(vocab.word, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
            }
            IconButton(
                onClick = onSpeakClick,
                modifier = Modifier.size(56.dp).background(KotoBeeOrange, CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Nghe", tint = Color.White)
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = Color.LightGray.copy(alpha = 0.3f))

        Text("Ý nghĩa:", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        Text(
            vocab.meaning,
            fontSize = 18.sp,
            color = TextDark,
            lineHeight = 1.5.em,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        Button(
            onClick = { onSaveClick() },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = KotoBeeOrange),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Outlined.BookmarkAdd, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Lưu vào sổ tay", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
