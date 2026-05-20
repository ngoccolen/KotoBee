package com.example.kotobee.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.kotobee.data.model.Badge
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun GoalCompletionDialog(
    badge: Badge?,
    onDismiss: () -> Unit,
    onViewProfile: () -> Unit,
    onCreateNewGoal: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        showContent = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            // Confetti particles
            ConfettiOverlay()

            AnimatedVisibility(
                visible = showContent,
                enter = scaleIn(
                    initialScale = 0.3f,
                    animationSpec = tween(600, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(400))
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.White,
                    shadowElevation = 16.dp
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Trophy badge with animation
                        BadgeTrophy(badge = badge)

                        Spacer(modifier = Modifier.height(24.dp))

                        // Celebration text
                        Text(
                            text = "🎉 Chúc mừng!",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFD32F2F)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Bạn đã hoàn thành mục tiêu\nvà nhận được huy hiệu mới!",
                            fontSize = 15.sp,
                            color = Color(0xFF757575),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Badge card
                        if (badge != null) {
                            BadgePreviewCard(badge = badge)
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Action buttons
                        Button(
                            onClick = onCreateNewGoal,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                "Tạo mục tiêu mới",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onViewProfile,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                "Xem hồ sơ",
                                color = Color(0xFFD32F2F),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeTrophy(badge: Badge?) {
    val infiniteTransition = rememberInfiniteTransition(label = "trophy")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "trophyRotation"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "trophyScale"
    )
    val sparkleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkleAlpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(120.dp)
    ) {
        // Sparkle ring
        Canvas(modifier = Modifier.fillMaxSize().alpha(sparkleAlpha)) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2 - 8.dp.toPx()
            for (i in 0 until 8) {
                val angle = (i * 45.0) * (Math.PI / 180.0)
                val sparkleX = center.x + (radius * cos(angle)).toFloat()
                val sparkleY = center.y + (radius * sin(angle)).toFloat()
                drawCircle(
                    color = Color(0xFFFFC107),
                    radius = 4.dp.toPx(),
                    center = Offset(sparkleX, sparkleY)
                )
            }
        }

        // Trophy circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(90.dp)
                .scale(scale)
                .rotate(rotation)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFFFFD54F),
                            Color(0xFFF9A825),
                            Color(0xFFFF8F00)
                        )
                    ),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "Trophy",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
private fun BadgePreviewCard(badge: Badge) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFFF8E1)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge icon
            Surface(
                color = Color(0xFFF9A825).copy(alpha = 0.15f),
                shape = CircleShape,
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getBadgeIcon(badge.iconName),
                        contentDescription = null,
                        tint = Color(0xFFF9A825),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = badge.name,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color(0xFF333333)
                )
                Text(
                    text = "Mục tiêu: ${badge.goalTitle}",
                    fontSize = 12.sp,
                    color = Color(0xFF795548)
                )
            }
        }
    }
}

@Composable
private fun ConfettiOverlay() {
    val particles = remember {
        List(40) {
            ConfettiParticle(
                x = Random.nextFloat(),
                startY = Random.nextFloat() * -0.3f,
                speed = 0.3f + Random.nextFloat() * 0.7f,
                size = 4f + Random.nextFloat() * 8f,
                color = listOf(
                    Color(0xFFE53935),
                    Color(0xFFFFC107),
                    Color(0xFF4CAF50),
                    Color(0xFF2196F3),
                    Color(0xFFFF9800),
                    Color(0xFFE91E63)
                ).random(),
                rotationSpeed = Random.nextFloat() * 360f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiTime"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val y = ((particle.startY + time * particle.speed) % 1.2f) * size.height
            val x = particle.x * size.width + sin((time * particle.rotationSpeed).toDouble()).toFloat() * 30.dp.toPx()
            val rotation = time * particle.rotationSpeed

            drawCircle(
                color = particle.color,
                radius = particle.size.dp.toPx() / 2f,
                center = Offset(x, y),
                alpha = (1f - (y / size.height)).coerceIn(0f, 1f)
            )
        }
    }
}

private data class ConfettiParticle(
    val x: Float,
    val startY: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val rotationSpeed: Float
)

fun getBadgeIcon(iconName: String) = when (iconName) {
    "first_goal" -> Icons.Default.Star
    "rising_star" -> Icons.Default.Star
    "champion" -> Icons.Default.EmojiEvents
    "master" -> Icons.Default.EmojiEvents
    "legend" -> Icons.Default.EmojiEvents
    else -> Icons.Default.Star
}
