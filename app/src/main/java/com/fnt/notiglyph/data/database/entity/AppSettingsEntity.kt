package com.fnt.notiglyph.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val notificationRetentionDays: Int = 7,
    val enableVoiceAlerts: Boolean = false,
    val lastSyncTimestamp: Long = 0L
)
