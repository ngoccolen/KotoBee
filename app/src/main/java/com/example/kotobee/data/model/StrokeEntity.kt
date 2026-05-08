package com.example.kotobee.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kanji_strokes")
data class StrokeEntity(

    @PrimaryKey
    val kanji: String,

    val svgPath: String
)