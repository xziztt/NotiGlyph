package com.fnt.notiglyph.data.repository

import com.fnt.notiglyph.data.database.dao.PatternDao
import com.fnt.notiglyph.domain.model.NotificationPattern
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for managing notification patterns
 */
class PatternRepository(private val patternDao: PatternDao) {

    fun getAllPatterns(): Flow<List<NotificationPattern>> {
        return patternDao.getAllPatternsFlow().map { entities ->
            entities.map { NotificationPattern.fromEntity(it) }
        }
    }

    fun getEnabledPatterns(): Flow<List<NotificationPattern>> {
        return patternDao.getEnabledPatternsFlow().map { entities ->
            entities.map { NotificationPattern.fromEntity(it) }
        }
    }

    suspend fun getPatternById(id: Long): NotificationPattern? {
        return patternDao.getPatternById(id)?.let { NotificationPattern.fromEntity(it) }
    }

    suspend fun insertPattern(pattern: NotificationPattern): Long {
        return patternDao.insertPattern(pattern.toEntity())
    }

    suspend fun updatePattern(pattern: NotificationPattern) {
        patternDao.updatePattern(pattern.toEntity())
    }

    suspend fun deletePattern(pattern: NotificationPattern) {
        patternDao.deletePattern(pattern.toEntity())
    }

    suspend fun setPatternEnabled(id: Long, enabled: Boolean) {
        patternDao.setPatternEnabled(id, enabled)
    }
}
