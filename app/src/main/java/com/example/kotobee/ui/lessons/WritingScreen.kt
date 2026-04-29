package com.example.kotobee.ui.lessons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandwritingPracticeScreen(
    viewModel: HandwritingViewModel = viewModel()
) {
    val kanjiDetail by viewModel.kanjiDetail.collectAsState()
    val paths by viewModel.paths.collectAsState()
    val showGuide by viewModel.showGuide.collectAsState()
    val score by viewModel.score.collectAsState()

    var currentPath by remember { mutableStateOf<Path?>(null) }

    Scaffold(
        containerColor = Color(0xFFF4F6F8),
        topBar = { HandwritingTopBar() },
        bottomBar = { HandwritingBottomBar(onCheckClick = { viewModel.checkScore() }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            KanjiInfoHeader(kanjiDetail)

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(320.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(2.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
            ) {
                CanvasGrid()

                if (showGuide) {
                    Text(
                        text = kanjiDetail.character,
                        fontSize = 220.sp,
                        color = Color.LightGray.copy(alpha = 0.3f),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                DrawingCanvas(
                    paths = paths,
                    currentPath = currentPath,
                    onPathStart = { offset ->
                        currentPath = Path().apply { moveTo(offset.x, offset.y) }
                    },
                    onPathDrag = { offset ->
                        currentPath?.lineTo(offset.x, offset.y)
                        currentPath = currentPath
                    },
                    onPathEnd = {
                        currentPath?.let { viewModel.addPath(it) }
                        currentPath = null
                    }
                )

                score?.let { currentScore ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Độ chính xác", color = Color.Gray, fontSize = 16.sp)
                            Text(
                                text = "$currentScore%",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentScore > 80) Color(0xFF4CAF50) else Color(0xFFFF9800)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = { viewModel.undo() },
                    modifier = Modifier.background(Color.White, RoundedCornerShape(50)).size(50.dp)
                ) {
                    Icon(Icons.Filled.Undo, contentDescription = null, tint = Color(0xFF374151))
                }

                IconButton(
                    onClick = { viewModel.clear() },
                    modifier = Modifier.background(Color.White, RoundedCornerShape(50)).size(50.dp)
                ) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = null, tint = Color(0xFFEF4444))
                }

                IconButton(
                    onClick = { viewModel.toggleGuide() },
                    modifier = Modifier.background(if (showGuide) Color(0xFFE0E7FF) else Color.White, RoundedCornerShape(50)).size(50.dp)
                ) {
                    Icon(
                        imageVector = if (showGuide) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = null,
                        tint = Color(0xFF4F46E5)
                    )
                }
            }
        }
    }
}

@Composable
fun DrawingCanvas(
    paths: List<Path>,
    currentPath: Path?,
    onPathStart: (Offset) -> Unit,
    onPathDrag: (Offset) -> Unit,
    onPathEnd: () -> Unit
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset -> onPathStart(offset) },
                    onDragEnd = { onPathEnd() },
                    onDragCancel = { onPathEnd() },
                    onDrag = { change, _ ->
                        change.consume()
                        onPathDrag(change.position)
                    }
                )
            }
    ) {
        val stroke = Stroke(
            width = 16f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        paths.forEach { path ->
            drawPath(path = path, color = Color(0xFF1E293B), style = stroke)
        }

        currentPath?.let { path ->
            drawPath(path = path, color = Color(0xFF1E293B), style = stroke)
        }
    }
}

@Composable
fun CanvasGrid() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))

        drawLine(
            color = Color.LightGray.copy(alpha = 0.5f),
            start = Offset(0f, 0f),
            end = Offset(canvasWidth, canvasHeight),
            strokeWidth = 2f,
            pathEffect = dashEffect
        )
        drawLine(
            color = Color.LightGray.copy(alpha = 0.5f),
            start = Offset(0f, canvasHeight),
            end = Offset(canvasWidth, 0f),
            strokeWidth = 2f,
            pathEffect = dashEffect
        )

        drawLine(color = Color.LightGray, start = Offset(canvasWidth / 2, 0f), end = Offset(canvasWidth / 2, canvasHeight), strokeWidth = 2f)
        drawLine(color = Color.LightGray, start = Offset(0f, canvasHeight / 2), end = Offset(canvasWidth, canvasHeight / 2), strokeWidth = 2f)
    }
}

@Composable
fun KanjiInfoHeader(detail: KanjiDetail) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(detail.character, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Text(detail.meaning, color = Color.Gray, fontSize = 14.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("Onyomi: ${detail.onyomi}", fontSize = 14.sp, color = Color(0xFF4F46E5), fontWeight = FontWeight.Medium)
            Text("Kunyomi: ${detail.kunyomi}", fontSize = 14.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Medium)
            Text("Số nét: ${detail.strokeCount}", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
fun HandwritingTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { }) { Icon(Icons.Filled.ArrowBack, contentDescription = null) }
        Text("Luyện viết Kanji", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        IconButton(onClick = { }) {
            Icon(Icons.Filled.PlayCircleOutline, contentDescription = null, tint = Color(0xFF4F46E5))
        }
    }
}

@Composable
fun HandwritingBottomBar(onCheckClick: () -> Unit) {
    Surface(shadowElevation = 8.dp, color = Color.White) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = { },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(54.dp)
            ) {
                Text("BỎ QUA", color = Color.Gray)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = onCheckClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier.weight(1f).height(54.dp)
            ) {
                Text("KIỂM TRA", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}