package com.example.kotobee.data.model

data class Deck(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)