package com.fnt.notiglyph.data.database.dao

import androidx.room.*
import com.fnt.notiglyph.data.database.entity.PatternEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PatternDao {
    @Query("SELECT * FROM patterns WHERE isEnabled = 1 ORDER BY priority DESC")
    suspend fun getEnabledPatterns(): List<PatternEntity>

    @Query("SELECT * FROM patterns WHERE isEnabled = 1 ORDER BY priority DESC")
    fun getEnabledPatternsFlow(): Flow<List<PatternEntity>>

    @Query("SELECT * FROM patterns ORDER BY priority DESC")
    fun getAllPatternsFlow(): Flow<List<PatternEntity>>

    @Query("SELECT * FROM patterns WHERE appPackageName = :packageName AND isEnabled = 1 ORDER BY priority DESC")
    suspend fun getPatternsForApp(packageName: String): List<PatternEntity>

    @Query("SELECT * FROM patterns WHERE id = :id")
    suspend fun getPatternById(id: Long): PatternEntity?

    @Insert
    suspend fun insertPattern(pattern: PatternEntity): Long

    @Update
    suspend fun updatePattern(pattern: PatternEntity)

    @Delete
    suspend fun deletePattern(pattern: PatternEntity)

    @Query("UPDATE patterns SET isEnabled = :enabled WHERE id = :id")
    suspend fun setPatternEnabled(id: Long, enabled: Boolean)
}
