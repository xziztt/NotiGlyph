package com.fnt.notiglyph.service

import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.fnt.notiglyph.data.database.NotiGlyphDatabase
import com.fnt.notiglyph.data.database.entity.NotificationHistoryEntity
import com.fnt.notiglyph.domain.PatternMatchingEngine
import com.fnt.notiglyph.domain.model.NotificationData
import com.fnt.notiglyph.domain.model.NotificationPattern
import com.fnt.notiglyph.glyph.GlyphManager
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Service to listen for notifications and parse them using defined patterns
 */
class NotificationParserService : NotificationListenerService() {
    private val TAG = "NotificationParser"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val matchingEngine = PatternMatchingEngine()
    private val gson = Gson()

    private lateinit var database: NotiGlyphDatabase

    // Track active Glyph displays by notification key (for cancellation on removal)
    private val activeNotificationKeys = mutableSetOf<String>()

    companion object {
        // Shared flow for matched notifications that should be displayed
        private val _matchedNotifications = MutableSharedFlow<String>(replay = 0)
        val matchedNotifications = _matchedNotifications.asSharedFlow()

        /**
         * Stop any active Glyph display immediately
         */
        fun stopGlyphDisplay() {
            GlyphManager.stopDisplay()
        }
    }

    override fun onCreate() {
        super.onCreate()
        database = NotiGlyphDatabase.getInstance(applicationContext)
        Log.d(TAG, "NotificationParserService created")
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        GlyphManager.cleanup()
        Log.d(TAG, "NotificationParserService destroyed")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        scope.launch {
            try {
                // Extract notification data
                val notificationData = extractNotificationData(sbn) ?: return@launch

                // Ignore our own notifications EXCEPT test notifications
                if (notificationData.appPackageName == packageName) {
                    // Allow test notifications through
                    if (notificationData.title != "NotiGlyph Test") {
                        return@launch
                    }
                    Log.d(TAG, "Processing test notification")
                }

                Log.d(TAG, "Notification from ${notificationData.appDisplayName}: ${notificationData.text}")

                // Get patterns for this app
                val patterns = database.patternDao()
                    .getPatternsForApp(notificationData.appPackageName)
                    .map { NotificationPattern.fromEntity(it) }

                if (patterns.isEmpty()) {
                    // Save to history as unmatched
                    saveNotificationHistory(notificationData, matched = false, null, null)
                    return@launch
                }

                // Try to find a match
                val matchResult = matchingEngine.findMatch(notificationData.text, patterns)

                if (matchResult != null) {
                    val (pattern, result) = matchResult
                    Log.d(TAG, "Matched pattern: ${pattern.displayTemplate} -> ${result.displayText}")

                    // Save to history as matched
                    saveNotificationHistory(
                        notificationData,
                        matched = true,
                        pattern.id,
                        result.extractedData
                    )

                    // Display directly on Glyph
                    displayOnGlyph(sbn.key, result.displayText, pattern.displayDurationSeconds, pattern.delaySeconds)

                    // Emit the display text to be shown on Glyph (for GlyphToyService compatibility)
                    _matchedNotifications.emit(result.displayText)
                } else {
                    // Save to history as unmatched
                    saveNotificationHistory(notificationData, matched = false, null, null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing notification", e)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        scope.launch {
            try {
                val notificationKey = sbn.key
                Log.d(TAG, "Notification removed: ${sbn.packageName} [key: $notificationKey]")

                // Check if this notification is currently being displayed on Glyph
                if (activeNotificationKeys.contains(notificationKey)) {
                    Log.d(TAG, "Stopping Glyph display for removed notification: $notificationKey")

                    // Stop the display
                    GlyphManager.stopDisplay()
                    activeNotificationKeys.remove(notificationKey)

                    Log.d(TAG, "Glyph display stopped due to notification removal")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling notification removal", e)
            }
        }
    }

    private fun displayOnGlyph(notificationKey: String, displayText: String, durationSeconds: Int, delaySeconds: Int) {
        if (delaySeconds > 0) {
            Log.d(TAG, "Will display on Glyph after ${delaySeconds}s delay: $displayText for ${durationSeconds}s [key: $notificationKey]")
        } else {
            Log.d(TAG, "Displaying on Glyph: $displayText for ${durationSeconds}s [key: $notificationKey]")
        }

        // Track this notification as active
        activeNotificationKeys.add(notificationKey)

        // Use GlyphManager singleton to display (with delay if specified)
        GlyphManager.displayText(applicationContext, displayText, durationSeconds, delaySeconds)

    }

    private fun extractNotificationData(sbn: StatusBarNotification): NotificationData? {
        return try {
            val notification = sbn.notification
            val extras = notification.extras

            val title = extras.getCharSequence("android.title")?.toString() ?: ""
            val text = extras.getCharSequence("android.text")?.toString() ?: ""

            if (text.isEmpty()) {
                return null
            }

            // Get app name
            val packageManager = applicationContext.packageManager
            val appInfo = try {
                packageManager.getApplicationInfo(sbn.packageName, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
            val appName = appInfo?.let { packageManager.getApplicationLabel(it).toString() }
                ?: sbn.packageName

            NotificationData(
                appPackageName = sbn.packageName,
                appDisplayName = appName,
                title = title,
                text = text,
                timestamp = sbn.postTime
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting notification data", e)
            null
        }
    }

    private suspend fun saveNotificationHistory(
        notificationData: NotificationData,
        matched: Boolean,
        patternId: Long?,
        extractedData: Map<String, String>?
    ) {
        try {
            val entity = NotificationHistoryEntity(
                appPackageName = notificationData.appPackageName,
                appDisplayName = notificationData.appDisplayName,
                title = notificationData.title,
                text = notificationData.text,
                timestamp = notificationData.timestamp,
                wasMatched = matched,
                matchedPatternId = patternId,
                extractedData = extractedData?.let { gson.toJson(it) }
            )
            database.notificationHistoryDao().insertNotification(entity)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving notification history", e)
        }
    }
}
