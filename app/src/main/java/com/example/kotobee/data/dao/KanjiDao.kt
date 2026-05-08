package com.example.kotobee.data.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kotobee.data.model.KanjiEntity
import com.example.kotobee.data.model.StrokeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KanjiDao {

    @Query("SELECT * FROM kanji ORDER BY jlptLevel ASC")
    fun getKanjiPaging(): PagingSource<Int, KanjiEntity>

    @Query("""
        SELECT * FROM kanji
        WHERE character LIKE '%' || :query || '%'
        OR meaning LIKE '%' || :query || '%'
        LIMIT 100
    """)
    fun searchKanji(query: String): Flow<List<KanjiEntity>>

    @Query("SELECT * FROM kanji WHERE character = :kanji")
    fun getKanjiDetail(kanji: String): Flow<KanjiEntity?>

    @Query("UPDATE kanji SET isFavorite = :favorite WHERE character = :kanji")
    suspend fun updateFavorite(kanji: String, favorite: Boolean)

    // Thêm hàm lấy Stroke data để vẽ
    @Query("SELECT * FROM kanji_strokes WHERE kanji = :kanji")
    fun getKanjiStrokes(kanji: String): Flow<StrokeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllKanji(kanjis: List<KanjiEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStrokes(strokes: List<StrokeEntity>)
}