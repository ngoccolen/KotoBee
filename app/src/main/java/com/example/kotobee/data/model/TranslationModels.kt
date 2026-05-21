package com.example.kotobee.data.model

import com.google.gson.annotations.SerializedName

data class JaViTranslationRequest(
    @SerializedName("text") val text: String,
    @SerializedName("article_title") val articleTitle: String = "",
    @SerializedName("article_context") val articleContext: String = ""
)

data class JaViTranslationResponse(
    @SerializedName("translation_vi") val translationVi: String = "",
    @SerializedName("source") val source: String = ""
)
