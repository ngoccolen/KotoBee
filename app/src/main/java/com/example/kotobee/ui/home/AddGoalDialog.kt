package com.example.kotobee.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val GoalRed = Color(0xFFD32F2F)
private val GoalRedLight = Color(0xFFE53935)
private val GoalBorder = Color(0xFFE0E0E0)
private val GoalTextDark = Color(0xFF333333)
private val GoalTextGray = Color(0xFF757575)

@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, milestones: List<String>) -> Unit
) {
    var goalTitle by remember { mutableStateOf("") }
    val milestones = remember { mutableStateListOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.border(1.5.dp, GoalRed.copy(alpha = 0.3f), RoundedCornerShape(28.dp)),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFFFFF5F5),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Flag,
                            contentDescription = null,
                            tint = GoalRed,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Tạo mục tiêu học tập",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = GoalTextDark
                    )
                    Text(
                        "Đặt mục tiêu và các cột mốc cần đạt",
                        fontSize = 12.sp,
                        color = GoalTextGray
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Goal title
                OutlinedTextField(
                    value = goalTitle,
                    onValueChange = { goalTitle = it },
                    label = { Text("Tên mục tiêu") },
                    placeholder = { Text("VD: Chinh phục N4 trong 3 tháng") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = goalTextFieldColors()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Milestones header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Cột mốc (${milestones.size}/7)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = GoalTextDark
                    )
                    if (milestones.size < 7) {
                        Surface(
                            color = Color(0xFFFFF5F5),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.clickable { milestones.add("") }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Thêm cột mốc",
                                    tint = GoalRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Thêm", color = GoalRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Milestone inputs
                milestones.forEachIndexed { index, milestone ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Number badge
                            Surface(
                                color = if (milestone.isNotBlank()) GoalRed else Color(0xFFE0E0E0),
                                shape = CircleShape,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "${index + 1}",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            OutlinedTextField(
                                value = milestone,
                                onValueChange = { milestones[index] = it },
                                placeholder = { Text("VD: Học 50 từ vựng mới", fontSize = 13.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = goalTextFieldColors(),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                            )
                            if (milestones.size > 1) {
                                IconButton(
                                    onClick = { milestones.removeAt(index) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Xóa",
                                        tint = GoalTextGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Hint
                Surface(
                    color = Color(0xFFFFF8E1),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "💡 Mẹo: Chia nhỏ mục tiêu thành 3-5 cột mốc để dễ hoàn thành hơn!",
                        color = Color(0xFF795548),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp),
                        lineHeight = 17.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validMilestones = milestones.filter { it.isNotBlank() }
                    if (goalTitle.isNotBlank() && validMilestones.isNotEmpty()) {
                        onConfirm(goalTitle.trim(), validMilestones.map { it.trim() })
                    }
                },
                enabled = goalTitle.isNotBlank() && milestones.any { it.isNotBlank() },
                colors = ButtonDefaults.buttonColors(containerColor = GoalRed),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Tạo mục tiêu", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = GoalTextGray)
            }
        }
    )
}

@Composable
private fun goalTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedBorderColor = GoalRed,
    unfocusedBorderColor = GoalBorder,
    focusedLabelColor = GoalRed,
    unfocusedLabelColor = GoalTextGray,
    cursorColor = GoalRed
)
