package com.example.kotobee.data.local

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.kotobee.data.database.AppDatabase
import com.example.kotobee.data.model.DemoKanjiDto
import com.example.kotobee.data.model.KanjiEntity
import com.example.kotobee.data.model.StrokeEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStreamReader

class KanjiDatabaseCallback(
    private val context: Context,
    private val scope: CoroutineScope
) : RoomDatabase.Callback() {

    // Hàm này CHỈ CHẠY 1 LẦN DUY NHẤT khi người dùng cài app và mở lên lần đầu
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        // Đẩy công việc đọc file nặng nhọc sang luồng IO (Background thread)
        scope.launch(Dispatchers.IO) {
            prepopulateDatabase()
        }
    }

    private suspend fun prepopulateDatabase() {
        try {
            // 1. Mở file JSON từ thư mục assets
            val inputStream = context.assets.open("demo_kanji.json")
            val reader = InputStreamReader(inputStream)

            // 2. Ép kiểu JSON text thành danh sách Object DemoKanjiDto
            val type = object : TypeToken<List<DemoKanjiDto>>() {}.type
            val demoData: List<DemoKanjiDto> = Gson().fromJson(reader, type)
            reader.close()

            // 3. Tách data ra thành 2 danh sách riêng cho 2 bảng (Kanji và Stroke)
            val kanjiEntities = demoData.map { dto ->
                KanjiEntity(
                    character = dto.character,
                    meaning = dto.meaning,
                    onyomi = dto.onyomi,
                    kunyomi = dto.kunyomi,
                    strokeCount = dto.strokeCount,
                    radical = dto.radical,
                    jlptLevel = dto.jlptLevel,
                    svgPath = null // Ta đã lưu path sang bảng StrokeEntity nên đây cứ để null
                )
            }

            val strokeEntities = demoData.map { dto ->
                StrokeEntity(
                    kanji = dto.character,
                    // Quan trọng: Nối các nét thành 1 chuỗi duy nhất, ngăn cách bởi ký tự "||"
                    // Lúc lấy lên UI để vẽ, em chỉ cần gọi: svgPath.split("||") là ra lại List
                    svgPath = dto.svgPaths.joinToString("||")
                )
            }

            // 4. Lấy Database Instance và gọi hàm Insert
            val database = AppDatabase.getInstance(context)
            database.kanjiDao().insertAllKanji(kanjiEntities)
            database.kanjiDao().insertAllStrokes(strokeEntities)

        } catch (e: Exception) {
            // Nếu có lỗi parse file hoặc tên biến ko khớp, nó sẽ in ra Logcat
            e.printStackTrace()
        }
    }
}