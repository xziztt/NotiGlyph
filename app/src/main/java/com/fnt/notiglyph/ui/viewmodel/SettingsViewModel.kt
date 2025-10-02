package com.fnt.notiglyph.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fnt.notiglyph.data.database.entity.AppSettingsEntity
import com.fnt.notiglyph.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the settings screen
 */
class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    private val _settings = MutableStateFlow(AppSettingsEntity())
    val settings: StateFlow<AppSettingsEntity> = _settings.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                _settings.value = settings ?: AppSettingsEntity()
            }
        }
    }

    fun updateRetentionDays(days: Int) {
        viewModelScope.launch {
            val updated = _settings.value.copy(notificationRetentionDays = days)
            settingsRepository.updateSettings(updated)
        }
    }

    fun updateVoiceAlerts(enabled: Boolean) {
        viewModelScope.launch {
            val updated = _settings.value.copy(enableVoiceAlerts = enabled)
            settingsRepository.updateSettings(updated)
        }
    }
}
