package com.example.kotobee.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotobee.R
import com.example.kotobee.data.model.GoalMilestone
import com.example.kotobee.data.model.LearningGoal

// Colors
private val PathRed = Color(0xFFD32F2F)
private val PathRedLight = Color(0xFFE53935)
private val PathGreen = Color(0xFF2E7D32)
private val PathGreenLight = Color(0xFF4CAF50)
private val PathGold = Color(0xFFFFC107)
private val PathGray = Color(0xFFBDBDBD)
private val PathBg = Color(0xFFFFF5F5)
private val PathTextDark = Color(0xFF333333)
private val PathTextGray = Color(0xFF757575)

@Composable
fun GoalPathSection(
    goal: LearningGoal?,
    onCreateGoal: () -> Unit,
    onMilestoneClick: (GoalMilestone) -> Unit,
    onDeleteGoal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Section title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🗺️ Con đường mục tiêu",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PathTextDark
            )
            if (goal != null && !goal.isCompleted) {
                IconButton(
                    onClick = onDeleteGoal,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Xóa mục tiêu",
                        tint = PathTextGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (goal == null) {
            // Empty state - CTA to create goal
            EmptyGoalCard(onCreateGoal = onCreateGoal)
        } else {
            // Goal path map
            GoalRoadMap(
                goal = goal,
                onMilestoneClick = onMilestoneClick
            )
        }
    }
}

@Composable
private fun EmptyGoalCard(onCreateGoal: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFF5F5),
                            Color(0xFFFFEBEE),
                            Color(0xFFFCE4EC)
                        )
                    )
                )
                .padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Mascot
                Image(
                    painter = painterResource(id = R.drawable.logo_4),
                    contentDescription = "KotoBee",
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Bắt đầu hành trình!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PathTextDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tạo mục tiêu học tập và theo dõi\ntiến trình trên con đường chinh phục!",
                    fontSize = 13.sp,
                    color = PathTextGray,
                    lineHeight = 19.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onCreateGoal,
                    colors = ButtonDefaults.buttonColors(containerColor = PathRed),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.scale(pulseScale)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Tạo mục tiêu đầu tiên",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalRoadMap(
    goal: LearningGoal,
    onMilestoneClick: (GoalMilestone) -> Unit
) {
    val milestones = goal.milestones
    val completedCount = milestones.count { it.isCompleted }
    val progress = if (milestones.isEmpty()) 0f else completedCount.toFloat() / milestones.size.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFF1F8E9),
                            Color(0xFFFFF8E1),
                            Color(0xFFFFF5F5),
                            Color(0xFFFCE4EC)
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Goal title header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = PathRed.copy(alpha = 0.12f),
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Flag,
                                contentDescription = null,
                                tint = PathRed,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = goal.title,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = PathTextDark
                        )
                        Text(
                            text = "$completedCount/${milestones.size} cột mốc hoàn thành",
                            fontSize = 12.sp,
                            color = PathTextGray
                        )
                    }
                    // Progress percentage
                    Surface(
                        color = if (goal.isCompleted) PathGreen.copy(alpha = 0.12f) else PathRed.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (goal.isCompleted) PathGreen else PathRed,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Road map with milestones
                val logoDrawables = listOf(
                    R.drawable.logo_1, R.drawable.logo_2, R.drawable.logo_3,
                    R.drawable.logo_4, R.drawable.logo_5, R.drawable.logo_6,
                    R.drawable.logo_7, R.drawable.logo_8
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((milestones.size * 110 + 60).dp)
                ) {
                    // Draw the winding road path
                    RoadPathCanvas(
                        milestoneCount = milestones.size,
                        completedCount = completedCount,
                        animatedProgress = animatedProgress,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Place milestone nodes
                    milestones.forEachIndexed { index, milestone ->
                        val isCompleted = milestone.isCompleted
                        val isCurrent = !isCompleted && (index == 0 || milestones[index - 1].isCompleted)
                        val isLocked = !isCompleted && !isCurrent

                        val yOffset = (index * 110 + 20).dp
                        val xOffset = if (index % 2 == 0) 40.dp else 180.dp

                        MilestoneNode(
                            milestone = milestone,
                            index = index,
                            isCompleted = isCompleted,
                            isCurrent = isCurrent,
                            isLocked = isLocked,
                            onClick = { if (isCurrent) onMilestoneClick(milestone) },
                            modifier = Modifier.offset(x = xOffset, y = yOffset)
                        )

                        // Cute mascot beside each milestone (alternating side)
                        val mascotXOffset = if (index % 2 == 0) 210.dp else 40.dp
                        val mascotYOffset = (index * 110 + 15).dp
                        val mascotRes = logoDrawables[index % logoDrawables.size]

                        // Mascot float/breath animation for each individual milestone
                        val mascotInfiniteTransition = rememberInfiniteTransition(label = "mascot_float_$index")
                        val mascotBounceY by mascotInfiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = -5f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000 + (index * 150) % 500, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "mascotBounceY_$index"
                        )

                        Image(
                            painter = painterResource(id = mascotRes),
                            contentDescription = "Mascot Goal Path",
                            modifier = Modifier
                                .offset(x = mascotXOffset, y = mascotYOffset + mascotBounceY.dp)
                                .size(64.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    // Destination flag at the end
                    val finalY = (milestones.size * 110 + 10).dp
                    val finalX = if (milestones.size % 2 == 0) 110.dp else 130.dp
                    DestinationFlag(
                        isReached = goal.isCompleted,
                        modifier = Modifier.offset(x = finalX, y = finalY)
                    )
                }
            }
        }
    }
}

@Composable
private fun RoadPathCanvas(
    milestoneCount: Int,
    completedCount: Int,
    animatedProgress: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dash")
    val dashOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dashOffset"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val nodePositions = (0 until milestoneCount).map { index ->
            val y = (index * 110 + 45).dp.toPx()
            val x = if (index % 2 == 0) 65.dp.toPx() else 205.dp.toPx()
            Offset(x, y)
        }

        // Draw background road (gray dashed)
        if (nodePositions.size >= 2) {
            val bgPath = Path()
            bgPath.moveTo(nodePositions[0].x, nodePositions[0].y)
            for (i in 1 until nodePositions.size) {
                val prev = nodePositions[i - 1]
                val curr = nodePositions[i]
                val midY = (prev.y + curr.y) / 2f
                bgPath.cubicTo(
                    prev.x, midY,
                    curr.x, midY,
                    curr.x, curr.y
                )
            }
            // Add path to destination
            val lastNode = nodePositions.last()
            val destX = if (milestoneCount % 2 == 0) 135.dp.toPx() else 155.dp.toPx()
            val destY = (milestoneCount * 110 + 35).dp.toPx()
            val midY2 = (lastNode.y + destY) / 2f
            bgPath.cubicTo(
                lastNode.x, midY2,
                destX, midY2,
                destX, destY
            )

            drawPath(
                path = bgPath,
                color = Color(0xFFE0E0E0),
                style = Stroke(
                    width = 8.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(16.dp.toPx(), 10.dp.toPx()),
                        dashOffset.dp.toPx()
                    )
                )
            )

            // Draw completed road (red solid)
            if (completedCount > 0 && nodePositions.size >= 2) {
                val completedPath = Path()
                val endIndex = completedCount.coerceAtMost(nodePositions.size)
                completedPath.moveTo(nodePositions[0].x, nodePositions[0].y)
                for (i in 1 until endIndex) {
                    val prev = nodePositions[i - 1]
                    val curr = nodePositions[i]
                    val mY = (prev.y + curr.y) / 2f
                    completedPath.cubicTo(
                        prev.x, mY,
                        curr.x, mY,
                        curr.x, curr.y
                    )
                }

                drawPath(
                    path = completedPath,
                    brush = Brush.verticalGradient(
                        listOf(PathGreenLight, PathGreen)
                    ),
                    style = Stroke(
                        width = 8.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}

@Composable
private fun MilestoneNode(
    milestone: GoalMilestone,
    index: Int,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow_$index")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha_$index"
    )
    val bounceY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounceY_$index"
    )

    Column(
        modifier = modifier
            .offset(y = if (isCurrent) bounceY.dp else 0.dp)
            .width(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Circle node
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .then(
                    if (isCurrent) {
                        Modifier.shadow(
                            elevation = (8 * glowAlpha).dp,
                            shape = CircleShape,
                            ambientColor = PathRed.copy(alpha = glowAlpha * 0.5f),
                            spotColor = PathRed.copy(alpha = glowAlpha * 0.5f)
                        )
                    } else Modifier
                )
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> Brush.radialGradient(listOf(PathGreenLight, PathGreen))
                        isCurrent -> Brush.radialGradient(listOf(PathRedLight, PathRed))
                        else -> Brush.radialGradient(listOf(Color(0xFFE0E0E0), Color(0xFFBDBDBD)))
                    }
                )
                .border(
                    width = 3.dp,
                    brush = when {
                        isCompleted -> Brush.linearGradient(listOf(Color(0xFF81C784), PathGreen))
                        isCurrent -> Brush.linearGradient(listOf(Color(0xFFEF5350), PathRed))
                        else -> Brush.linearGradient(listOf(Color(0xFFE0E0E0), Color(0xFFBDBDBD)))
                    },
                    shape = CircleShape
                )
                .clickable(enabled = isCurrent) { onClick() }
        ) {
            Icon(
                imageVector = when {
                    isCompleted -> Icons.Default.CheckCircle
                    isCurrent -> Icons.Default.PlayArrow
                    else -> Icons.Default.Lock
                },
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Label
        Text(
            text = milestone.title,
            fontSize = 11.sp,
            fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium,
            color = when {
                isCompleted -> PathGreen
                isCurrent -> PathRed
                else -> PathGray
            },
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun DestinationFlag(
    isReached: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flag")
    val flagScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isReached) 1.15f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flagScale"
    )

    Column(
        modifier = modifier.scale(flagScale),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    if (isReached)
                        Brush.radialGradient(listOf(Color(0xFFFFD54F), Color(0xFFF9A825)))
                    else
                        Brush.radialGradient(listOf(Color(0xFFE0E0E0), Color(0xFFBDBDBD)))
                )
                .border(
                    3.dp,
                    if (isReached) Brush.linearGradient(listOf(Color(0xFFFFE082), Color(0xFFF9A825)))
                    else Brush.linearGradient(listOf(Color(0xFFE0E0E0), Color(0xFFBDBDBD))),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = if (isReached) Icons.Default.EmojiEvents else Icons.Default.Star,
                contentDescription = "Đích đến",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isReached) "🎉 Đã đến đích!" else "🏁 Đích đến",
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isReached) Color(0xFFF9A825) else PathGray
        )
    }
}
