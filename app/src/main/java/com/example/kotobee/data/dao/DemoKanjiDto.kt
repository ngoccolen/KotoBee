package com.example.kotobee.data.model

import com.google.gson.annotations.SerializedName

// Tên biến phải khớp với key trong file JSON
data class DemoKanjiDto(
    @SerializedName("character") val character: String,
    @SerializedName("meaning") val meaning: String,
    @SerializedName("onyomi") val onyomi: String,
    @SerializedName("kunyomi") val kunyomi: String,
    @SerializedName("strokeCount") val strokeCount: Int,
    @SerializedName("radical") val radical: String,
    @SerializedName("jlptLevel") val jlptLevel: Int,
    @SerializedName("svgPaths") val svgPaths: List<String>
)