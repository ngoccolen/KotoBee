package com.example.kotobee.model

data class Comment(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val content: String = "",
    val timestamp: Long = 0L
)