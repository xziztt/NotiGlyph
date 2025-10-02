package com.fnt.notiglyph.data.repository

import com.fnt.notiglyph.data.database.dao.NotificationHistoryDao
import com.fnt.notiglyph.data.database.entity.NotificationHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing notification history
 */
class NotificationRepository(private val notificationHistoryDao: NotificationHistoryDao) {

    fun getAllNotifications(): Flow<List<NotificationHistoryEntity>> {
        return notificationHistoryDao.getAllNotificationsFlow()
    }

    fun getMatchedNotifications(): Flow<List<NotificationHistoryEntity>> {
        return notificationHistoryDao.getMatchedNotificationsFlow()
    }

    suspend fun getRecentNotifications(limit: Int = 50): List<NotificationHistoryEntity> {
        return notificationHistoryDao.getRecentNotifications(limit)
    }

    suspend fun getNotificationsForApp(packageName: String, limit: Int = 10): List<NotificationHistoryEntity> {
        return notificationHistoryDao.getNotificationsForApp(packageName, limit)
    }

    suspend fun deleteOldNotifications(retentionDays: Int): Int {
        val cutoffTime = System.currentTimeMillis() - (retentionDays * 24 * 60 * 60 * 1000L)
        return notificationHistoryDao.deleteOldNotifications(cutoffTime)
    }

    suspend fun deleteAllNotifications(): Int {
        return notificationHistoryDao.deleteAllNotifications()
    }
}
