package com.example.kotobee.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "kanji",
    indices = [
        Index("meaning"),
        Index("jlptLevel")
    ]
)
data class KanjiEntity(

    @PrimaryKey
    val character: String,

    val meaning: String,

    val onyomi: String,

    val kunyomi: String,

    val strokeCount: Int,

    val radical: String,

    val jlptLevel: Int,

    val svgPath: String?,

    val isFavorite: Boolean = false
)