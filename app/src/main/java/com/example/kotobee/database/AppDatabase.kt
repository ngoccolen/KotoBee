package com.example.kotobee.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.kotobee.data.dao.KanjiDao
import com.example.kotobee.data.local.KanjiDatabaseCallback
import com.example.kotobee.data.model.KanjiEntity
import com.example.kotobee.data.model.StrokeEntity
import com.example.kotobee.data.model.VocabularyEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Database(
    entities = [KanjiEntity::class, StrokeEntity::class, VocabularyEntity::class],
    version = 1, // Nếu app em đang chạy bị crash vì đổi DB schema, hãy tăng số này lên 2
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun kanjiDao(): KanjiDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Tạo một Scope riêng biệt không bị hủy khi tắt màn hình
                val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kotobee_database"
                )
                    // 👉 Gắn thằng Callback vừa viết ở bước 5 vào đây
                    .addCallback(KanjiDatabaseCallback(context, applicationScope))
                    .fallbackToDestructiveMigration() // Tự xóa data cũ nếu đổi version DB
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}