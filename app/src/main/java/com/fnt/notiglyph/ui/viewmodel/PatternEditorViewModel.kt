package com.fnt.notiglyph.ui.viewmodel

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fnt.notiglyph.data.repository.NotificationRepository
import com.fnt.notiglyph.data.repository.PatternRepository
import com.fnt.notiglyph.domain.PatternMatchingEngine
import com.fnt.notiglyph.domain.model.*
import com.fnt.notiglyph.glyph.GlyphManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class InstalledApp(
    val packageName: String,
    val appName: String
)

private val TAG = "PATTERN_EDITOR_VIEWMODEL"

/**
 * ViewModel for the pattern editor screen
 */
class PatternEditorViewModel(
    private val patternRepository: PatternRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val matchingEngine = PatternMatchingEngine()

    private val _pattern = MutableStateFlow<NotificationPattern?>(null)
    val pattern: StateFlow<NotificationPattern?> = _pattern.asStateFlow()

    private val _recentNotifications = MutableStateFlow<List<String>>(emptyList())
    val recentNotifications: StateFlow<List<String>> = _recentNotifications.asStateFlow()

    private val _testResults = MutableStateFlow<List<MatchResult>>(emptyList())
    val testResults: StateFlow<List<MatchResult>> = _testResults.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps.asStateFlow()

    fun loadPattern(patternId: Long?) {
        if (patternId == null || patternId == 0L) {
            // New pattern
            _pattern.value = createEmptyPattern()
        } else {
            viewModelScope.launch {
                _pattern.value = patternRepository.getPatternById(patternId)
                updatePattern()
                Log.d(TAG,"pattern details fetched: ${_pattern.value}")
            }
        }
    }

    fun updatePattern(
        appPackageName: String = _pattern.value?.appPackageName ?: "",
        appDisplayName: String = _pattern.value?.appDisplayName ?: "",
        patternType: PatternType = _pattern.value?.patternType ?: PatternType.TEMPLATE,
        patternString: String = _pattern.value?.patternString ?: "",
        extractedVariables: List<String> = _pattern.value?.extractedVariables ?: emptyList(),
        displayTemplate: String = _pattern.value?.displayTemplate ?: "",
        priority: Int = _pattern.value?.priority ?: 5,
        isEnabled: Boolean = _pattern.value?.isEnabled ?: true,
        iconType: IconType = _pattern.value?.iconType ?: IconType.EMOJI,
        displayDurationSeconds: Int = _pattern.value?.displayDurationSeconds ?: 30
    ) {
        Log.d(TAG,"Updated pattern setting with fetched values.")
        _pattern.value = _pattern.value?.copy(
            appPackageName = appPackageName,
            appDisplayName = appDisplayName,
            patternType = patternType,
            patternString = patternString,
            extractedVariables = extractedVariables,
            displayTemplate = displayTemplate,
            priority = priority,
            isEnabled = isEnabled,
            iconType = iconType,
            displayDurationSeconds = displayDurationSeconds
        ) ?: NotificationPattern(
            appPackageName = appPackageName,
            appDisplayName = appDisplayName,
            patternType = patternType,
            patternString = patternString,
            extractedVariables = extractedVariables,
            displayTemplate = displayTemplate,
            priority = priority,
            isEnabled = isEnabled,
            iconType = iconType,
            displayDurationSeconds = displayDurationSeconds,
            createdAt = System.currentTimeMillis()
        )
    }

    fun savePattern() {
        viewModelScope.launch {
            _pattern.value?.let { pattern ->
                if (pattern.id == 0L) {
                    patternRepository.insertPattern(pattern)
                } else {
                    patternRepository.updatePattern(pattern)
                }
            }
        }
    }

    fun loadRecentNotificationsForApp(packageName: String) {
        viewModelScope.launch {
            val notifications = notificationRepository.getNotificationsForApp(packageName, 10)
            _recentNotifications.value = notifications.map { it.text }
        }
    }

    fun testPattern() {
        val currentPattern = _pattern.value ?: return
        val results = _recentNotifications.value.map { notificationText ->
            matchingEngine.matchPattern(notificationText, currentPattern)
        }
        _testResults.value = results
    }

    fun loadInstalledApps(context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val packageManager = context.packageManager
                // Get ALL installed applications (both system and user apps)
                // Use PackageManager.GET_META_DATA flag to ensure we get all apps
                val allPackages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

                val apps = allPackages.mapNotNull { appInfo ->
                    try {
                        val appName = packageManager.getApplicationLabel(appInfo).toString()
                        InstalledApp(
                            packageName = appInfo.packageName,
                            appName = appName
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                .sortedBy { it.appName.lowercase() }

                _installedApps.value = apps
            }
        }
    }

    fun testGlyph(context: Context, testText: String) {
        Log.d(TAG, "Starting Glyph test with text: $testText")
        // Use GlyphManager singleton - it handles its own lifecycle
        // No JobCancellationException since it uses independent scope
        GlyphManager.displayText(context, testText, 5)
    }

    private fun createEmptyPattern(): NotificationPattern {
        return NotificationPattern(
            id = 0,
            appPackageName = "",
            appDisplayName = "",
            patternType = PatternType.TEMPLATE,
            patternString = "",
            extractedVariables = emptyList(),
            displayTemplate = "",
            priority = 5,
            isEnabled = true,
            iconType = IconType.EMOJI,
            displayDurationSeconds = 30,
            createdAt = System.currentTimeMillis()
        )
    }
}
