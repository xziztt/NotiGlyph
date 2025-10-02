package com.fnt.notiglyph.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_history")
data class NotificationHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val appPackageName: String,
    val appDisplayName: String,
    val title: String,
    val text: String,
    val timestamp: Long,
    val wasMatched: Boolean,
    val matchedPatternId: Long?,
    val extractedData: String? // JSON map of extracted variables
)
