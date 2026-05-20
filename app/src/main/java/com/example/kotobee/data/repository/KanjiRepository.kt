package com.example.kotobee.data.repository

import android.content.Context
import com.example.kotobee.data.model.DemoKanjiDto
import com.example.kotobee.data.model.KanjiEntity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

class KanjiRepository(private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getKanjiList(): List<KanjiEntity> = withContext(Dispatchers.IO) {
        val remoteKanji = runCatching {
            firestore.collection(KANJI_COLLECTION)
                .get()
                .await()
                .documents
                .filter { it.isPublished() }
                .sortedWith(compareBy<DocumentSnapshot> { it.levelSortOrder() }.thenBy { it.sortOrder() }.thenBy { it.character() })
                .mapNotNull { it.toKanjiEntity() }
        }.getOrDefault(emptyList())

        remoteKanji.ifEmpty {
            assetKanji.map { it.toKanjiEntity() }
        }
    }

    suspend fun getKanjiDetail(character: String): DemoKanjiDto? = withContext(Dispatchers.IO) {
        val remoteKanji = runCatching {
            val directDocument = firestore.collection(KANJI_COLLECTION)
                .document(character)
                .get()
                .await()

            if (directDocument.exists() && directDocument.isPublished()) {
                directDocument.toDemoKanjiDto()
            } else {
                firestore.collection(KANJI_COLLECTION)
                    .whereEqualTo("character", character)
                    .limit(1)
                    .get()
                    .await()
                    .documents
                    .firstOrNull { it.isPublished() }
                    ?.toDemoKanjiDto()
            }
        }.getOrNull()

        remoteKanji ?: assetKanji.firstOrNull { it.character == character }
    }

    private val assetKanji: List<DemoKanjiDto> by lazy {
        runCatching {
            context.assets.open("demo_kanji.json").use { inputStream ->
                InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                    val type = object : TypeToken<List<DemoKanjiDto>>() {}.type
                    Gson().fromJson<List<DemoKanjiDto>>(reader, type).orEmpty()
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun DemoKanjiDto.toKanjiEntity(): KanjiEntity =
        KanjiEntity(
            character = character,
            meaning = meaning,
            onyomi = onyomi,
            kunyomi = kunyomi,
            strokeCount = strokeCount,
            radical = radical,
            jlptLevel = jlptLevel,
            svgPath = svgPaths.joinToString("||").ifBlank { null }
        )

    private fun DocumentSnapshot.toKanjiEntity(): KanjiEntity? {
        val character = character()
        if (character.isBlank()) return null

        return KanjiEntity(
            character = character,
            meaning = firstString("meaning_vi", "meaning", "title"),
            onyomi = readingString("onyomi"),
            kunyomi = readingString("kunyomi"),
            strokeCount = intValue("strokes", "strokeCount"),
            radical = firstString("radical"),
            jlptLevel = levelNumber(),
            svgPath = svgPaths().joinToString("||").ifBlank { null }
        )
    }

    private fun DocumentSnapshot.toDemoKanjiDto(): DemoKanjiDto? {
        val character = character()
        if (character.isBlank()) return null

        return DemoKanjiDto(
            character = character,
            meaning = firstString("meaning_vi", "meaning", "title"),
            onyomi = readingString("onyomi"),
            kunyomi = readingString("kunyomi"),
            strokeCount = intValue("strokes", "strokeCount"),
            radical = firstString("radical"),
            jlptLevel = levelNumber(),
            svgPaths = svgPaths()
        )
    }

    private fun DocumentSnapshot.character(): String =
        firstString("character", "kanji").ifBlank { id.takeIf { it.length <= 4 }.orEmpty() }

    private fun DocumentSnapshot.readingString(field: String): String {
        val listValue = get(field) as? List<*>
        if (!listValue.isNullOrEmpty()) {
            return listValue.mapNotNull { it?.toString()?.trim()?.takeIf(String::isNotBlank) }
                .joinToString("、")
        }
        return firstString(field)
    }

    private fun DocumentSnapshot.svgPaths(): List<String> {
        val listValue = (get("svgPaths") ?: get("svg_paths")) as? List<*>
        if (!listValue.isNullOrEmpty()) {
            return listValue.mapNotNull { it?.toString()?.trim()?.takeIf(String::isNotBlank) }
        }

        return firstString("svgPath", "svg_path")
            .split("||", "\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    private fun DocumentSnapshot.firstString(vararg fields: String): String =
        fields.firstNotNullOfOrNull { field ->
            getString(field)?.trim()?.takeIf { it.isNotBlank() }
        }.orEmpty()

    private fun DocumentSnapshot.intValue(vararg fields: String): Int =
        fields.firstNotNullOfOrNull { field ->
            when (val value = get(field)) {
                is Number -> value.toInt()
                is String -> value.toIntOrNull()
                else -> null
            }
        } ?: 0

    private fun DocumentSnapshot.levelNumber(): Int {
        val numericLevel = intValue("jlptLevel", "jlpt_level")
        if (numericLevel > 0) return numericLevel

        return when (firstString("level").uppercase()) {
            "N5" -> 5
            "N4" -> 4
            "N3" -> 3
            "N2" -> 2
            "N1" -> 1
            else -> 99
        }
    }

    private fun DocumentSnapshot.levelSortOrder(): Int =
        when (levelNumber()) {
            5 -> 0
            4 -> 1
            3 -> 2
            2 -> 3
            1 -> 4
            else -> 5
        }

    private fun DocumentSnapshot.sortOrder(): Int =
        (get("sort_order") as? Number)?.toInt() ?: Int.MAX_VALUE

    private fun DocumentSnapshot.isPublished(): Boolean {
        val status = firstString("status")
        return status.isBlank() || status == "published"
    }

    private companion object {
        const val KANJI_COLLECTION = "kanji"
    }
}
