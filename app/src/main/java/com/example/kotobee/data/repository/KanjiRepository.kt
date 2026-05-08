package com.example.kotobee.data.repository

import android.content.Context
import com.example.kotobee.data.model.DemoKanjiDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

class KanjiRepository(private val context: Context) {

    private var cachedKanjiList: List<DemoKanjiDto> = emptyList()

    init {
        loadDataFromAssets()
    }

    private fun loadDataFromAssets() {
        try {
            val inputStream = context.assets.open("demo_kanji.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<DemoKanjiDto>>() {}.type
            cachedKanjiList = Gson().fromJson(reader, type)
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getKanjiDetail(character: String): DemoKanjiDto? {
        return cachedKanjiList.find { it.character == character }
    }
}