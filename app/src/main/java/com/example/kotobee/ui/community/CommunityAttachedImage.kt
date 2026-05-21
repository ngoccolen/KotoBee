package com.example.kotobee.ui.community

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun CommunityAttachedImage(
    model: Any?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    cornerRadius: Dp = 18.dp,
    minHeight: Dp = 150.dp,
    maxHeight: Dp = 420.dp
) {
    var aspectRatio by remember(model) { mutableStateOf(16f / 9f) }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val imageHeight = (maxWidth / aspectRatio).coerceIn(minHeight, maxHeight)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(imageHeight)
                .clip(RoundedCornerShape(cornerRadius))
                .background(Color(0xFFF7F7F7))
        ) {
            AsyncImage(
                model = model,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                onSuccess = { state ->
                    val intrinsicSize = state.painter.intrinsicSize
                    if (
                        intrinsicSize.width.isFinite() &&
                        intrinsicSize.height.isFinite() &&
                        intrinsicSize.width > 0f &&
                        intrinsicSize.height > 0f
                    ) {
                        aspectRatio = (intrinsicSize.width / intrinsicSize.height)
                            .coerceIn(0.35f, 3.5f)
                    }
                }
            )
        }
    }
}
