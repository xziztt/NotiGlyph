package com.fnt.notiglyph.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fnt.notiglyph.data.repository.NotificationRepository
import com.fnt.notiglyph.data.repository.PatternRepository
import com.fnt.notiglyph.data.repository.SettingsRepository

class ViewModelFactory(
    private val patternRepository: PatternRepository,
    private val notificationRepository: NotificationRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(patternRepository) as T
            }
            modelClass.isAssignableFrom(PatternEditorViewModel::class.java) -> {
                PatternEditorViewModel(patternRepository, notificationRepository) as T
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(notificationRepository) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(settingsRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
