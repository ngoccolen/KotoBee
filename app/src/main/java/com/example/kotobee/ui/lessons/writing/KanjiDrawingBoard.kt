package com.example.kotobee.ui.lessons.writing

import android.graphics.PathMeasure
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun KanjiDrawingBoard(
    modifier: Modifier = Modifier,
    paths: List<Path>, // Danh sách các nét đã vẽ xong
    currentPath: Path?, // Nét đang vẽ dở
    hintPath: Path? = null, // Nét gợi ý (SVG mờ)
    showHint: Boolean = false,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit
) {
    Canvas(
        modifier = modifier
            .aspectRatio(1f) // Ép khung vẽ luôn là hình vuông
            .background(Color.White)
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            .clipToBounds() // Ngăn vẽ lem ra ngoài khung
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset -> onDragStart(offset) },
                    onDrag = { change, _ ->
                        change.consume()
                        onDrag(change.position)
                    },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragEnd
                )
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)

        // 1. Vẽ Grid chữ thập nét đứt
        drawLine(
            color = Color.LightGray.copy(alpha = 0.6f),
            start = Offset(canvasWidth / 2, 0f),
            end = Offset(canvasWidth / 2, canvasHeight),
            strokeWidth = 2f,
            pathEffect = dashEffect
        )
        drawLine(
            color = Color.LightGray.copy(alpha = 0.6f),
            start = Offset(0f, canvasHeight / 2),
            end = Offset(canvasWidth, canvasHeight / 2),
            strokeWidth = 2f,
            pathEffect = dashEffect
        )

        // 2. Vẽ gợi ý nét (nếu user bật chức năng xem chi tiết)
        if (showHint && hintPath != null) {
            // Cần scale SVG từ 109x109 (size chuẩn của KanjiVG) lên size của màn hình
            val scaleMatrix = androidx.compose.ui.graphics.Matrix()
            scaleMatrix.scale(canvasWidth / 109f, canvasHeight / 109f)

            val scaledHintPath = Path().apply {
                addPath(hintPath)
                transform(scaleMatrix)
            }

            drawPath(
                path = scaledHintPath,
                color = Color.Red.copy(alpha = 0.2f),
                style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        val brushStyle = Stroke(width = 24f, cap = StrokeCap.Round, join = StrokeJoin.Round)

        // 3. Vẽ các nét đã hoàn thành
        paths.forEach { path ->
            drawPath(path = path, color = Color.Black, style = brushStyle)
        }

        // 4. Vẽ nét đang thao tác
        currentPath?.let { path ->
            drawPath(path = path, color = Color.Black, style = brushStyle)
        }
    }
}

@Composable
fun AnimatedKanjiStrokeOrder(
    strokePaths: List<Path>,
    modifier: Modifier = Modifier,
    strokeDurationMillis: Int = 520,
    strokePauseMillis: Long = 90L
) {
    var replayToken by remember(strokePaths) { mutableStateOf(0) }
    var activeStrokeIndex by remember(strokePaths) { mutableStateOf(0) }
    var completedStrokeCount by remember(strokePaths) { mutableStateOf(0) }
    val strokeProgress = remember(strokePaths) { Animatable(0f) }

    LaunchedEffect(strokePaths, replayToken) {
        completedStrokeCount = 0
        activeStrokeIndex = 0
        strokeProgress.snapTo(0f)

        if (strokePaths.isEmpty()) {
            return@LaunchedEffect
        }

        strokePaths.forEachIndexed { index, _ ->
            activeStrokeIndex = index
            strokeProgress.snapTo(0f)
            strokeProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = strokeDurationMillis,
                    easing = LinearEasing
                )
            )
            completedStrokeCount = index + 1
            delay(strokePauseMillis)
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            .clipToBounds()
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
            val scaleMatrix = Matrix().apply {
                scale(canvasWidth / KANJIVG_VIEW_BOX_SIZE, canvasHeight / KANJIVG_VIEW_BOX_SIZE)
            }
            val scaledStrokePaths = strokePaths.map { path ->
                Path().apply {
                    addPath(path)
                    transform(scaleMatrix)
                }
            }

            drawLine(
                color = Color.LightGray.copy(alpha = 0.6f),
                start = Offset(canvasWidth / 2, 0f),
                end = Offset(canvasWidth / 2, canvasHeight),
                strokeWidth = 2f,
                pathEffect = dashEffect
            )
            drawLine(
                color = Color.LightGray.copy(alpha = 0.6f),
                start = Offset(0f, canvasHeight / 2),
                end = Offset(canvasWidth, canvasHeight / 2),
                strokeWidth = 2f,
                pathEffect = dashEffect
            )

            val ghostStyle = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            val activeStyle = Stroke(width = 14f, cap = StrokeCap.Round, join = StrokeJoin.Round)

            scaledStrokePaths.forEach { path ->
                drawPath(
                    path = path,
                    color = Color(0xFF333333).copy(alpha = 0.12f),
                    style = ghostStyle
                )
            }

            scaledStrokePaths.take(completedStrokeCount).forEach { path ->
                drawPath(path = path, color = Color(0xFF333333), style = activeStyle)
            }

            if (completedStrokeCount <= activeStrokeIndex) {
                scaledStrokePaths.getOrNull(activeStrokeIndex)?.let { path ->
                    drawPath(
                        path = path.partialPath(strokeProgress.value),
                        color = Color(0xFFE53935),
                        style = activeStyle
                    )
                }
            }
        }

        IconButton(
            onClick = { replayToken += 1 },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Replay,
                contentDescription = "Phát lại",
                tint = Color(0xFFE53935)
            )
        }
    }
}

private fun Path.partialPath(progress: Float): Path {
    val measuredPath = asAndroidPath()
    val measure = PathMeasure(measuredPath, false)
    val destination = android.graphics.Path()
    val targetLength = totalLength() * progress.coerceIn(0f, 1f)
    var remainingLength = targetLength

    do {
        val contourLength = measure.length
        when {
            remainingLength <= 0f -> break
            remainingLength >= contourLength -> {
                measure.getSegment(0f, contourLength, destination, true)
                remainingLength -= contourLength
            }
            else -> {
                measure.getSegment(0f, remainingLength, destination, true)
                break
            }
        }
    } while (measure.nextContour())

    return destination.asComposePath()
}

private fun Path.totalLength(): Float {
    val measure = PathMeasure(asAndroidPath(), false)
    var length = 0f

    do {
        length += measure.length
    } while (measure.nextContour())

    return length
}

private const val KANJIVG_VIEW_BOX_SIZE = 109f
