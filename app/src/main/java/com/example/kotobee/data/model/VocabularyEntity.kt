package com.example.kotobee.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "vocabulary",
    foreignKeys = [
        ForeignKey(
            entity = KanjiEntity::class,
            parentColumns = ["character"],
            childColumns = ["kanji"]
        )
    ]
)
data class VocabularyEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val kanji: String,

    val word: String,

    val reading: String,

    val meaning: String
)