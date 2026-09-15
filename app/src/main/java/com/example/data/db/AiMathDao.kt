package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AiMathDao {
    @Query("SELECT * FROM ai_math_history ORDER BY timestamp DESC")
    fun getAllAiHistory(): Flow<List<AiMathHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAiHistory(item: AiMathHistory): Long

    @Query("DELETE FROM ai_math_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM ai_math_history")
    suspend fun clearAll()
}
