package com.fnt.notiglyph.data.database.dao

import androidx.room.*
import com.fnt.notiglyph.data.database.entity.NotificationHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationHistoryDao {
    @Query("SELECT * FROM notification_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentNotifications(limit: Int): List<NotificationHistoryEntity>

    @Query("SELECT * FROM notification_history ORDER BY timestamp DESC")
    fun getAllNotificationsFlow(): Flow<List<NotificationHistoryEntity>>

    @Query("SELECT * FROM notification_history WHERE wasMatched = 1 ORDER BY timestamp DESC")
    fun getMatchedNotificationsFlow(): Flow<List<NotificationHistoryEntity>>

    @Query("SELECT * FROM notification_history WHERE appPackageName = :packageName ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getNotificationsForApp(packageName: String, limit: Int = 10): List<NotificationHistoryEntity>

    @Insert
    suspend fun insertNotification(notification: NotificationHistoryEntity): Long

    @Query("DELETE FROM notification_history WHERE timestamp < :cutoffTime")
    suspend fun deleteOldNotifications(cutoffTime: Long): Int

    @Query("DELETE FROM notification_history")
    suspend fun deleteAllNotifications(): Int
}
