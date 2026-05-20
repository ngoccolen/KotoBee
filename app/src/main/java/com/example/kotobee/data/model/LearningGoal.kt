package com.example.kotobee.data.model

data class LearningGoal(
    val id: String = "",
    val title: String = "",           // VD: "Chinh phục N4"
    val milestones: List<GoalMilestone> = emptyList(),
    val isCompleted: Boolean = false,
    val createdAt: Long = 0L,
    val completedAt: Long? = null
)

data class GoalMilestone(
    val id: String = "",
    val title: String = "",           // VD: "Học 20 từ vựng mới"
    val isCompleted: Boolean = false,
    val order: Int = 0
)
