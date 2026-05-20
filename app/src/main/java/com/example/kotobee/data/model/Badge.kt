package com.example.kotobee.data.model

data class Badge(
    val id: String = "",
    val name: String = "",           // VD: "Chinh phục mục tiêu đầu tiên"
    val iconName: String = "",       // Mapped to drawable/icon
    val earnedAt: Long = 0L,
    val goalId: String = "",         // Link to completed goal
    val goalTitle: String = ""       // Title of the goal completed
)
