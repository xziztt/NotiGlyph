package com.fnt.notiglyph.data.repository

import com.fnt.notiglyph.data.database.dao.SettingsDao
import com.fnt.notiglyph.data.database.entity.AppSettingsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing app settings
 */
class SettingsRepository(private val settingsDao: SettingsDao) {

    fun getSettings(): Flow<AppSettingsEntity?> {
        return settingsDao.getSettingsFlow()
    }

    suspend fun getSettingsOnce(): AppSettingsEntity {
        return settingsDao.getSettings() ?: AppSettingsEntity()
    }

    suspend fun updateSettings(settings: AppSettingsEntity) {
        settingsDao.updateSettings(settings)
    }

    suspend fun initializeDefaultSettings() {
        val existing = settingsDao.getSettings()
        if (existing == null) {
            settingsDao.insertSettings(AppSettingsEntity())
        }
    }
}
