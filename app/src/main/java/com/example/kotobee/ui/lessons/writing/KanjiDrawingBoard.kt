package com.example.kotobee.ui.lessons.writing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

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