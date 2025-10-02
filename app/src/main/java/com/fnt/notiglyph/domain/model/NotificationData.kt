package com.fnt.notiglyph.domain.model

/**
 * Data extracted from an Android notification
 */
data class NotificationData(
    val appPackageName: String,
    val appDisplayName: String,
    val title: String,
    val text: String,
    val timestamp: Long
)
