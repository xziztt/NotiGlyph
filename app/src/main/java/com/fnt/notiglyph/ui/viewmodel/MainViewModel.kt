package com.fnt.notiglyph.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fnt.notiglyph.data.repository.PatternRepository
import com.fnt.notiglyph.domain.model.NotificationPattern
import com.fnt.notiglyph.service.NotificationParserService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the main screen showing pattern list
 */
class MainViewModel(private val patternRepository: PatternRepository) : ViewModel() {

    private val _patterns = MutableStateFlow<List<NotificationPattern>>(emptyList())
    val patterns: StateFlow<List<NotificationPattern>> = _patterns.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadPatterns()
    }

    private fun loadPatterns() {
        viewModelScope.launch {
            _isLoading.value = true
            patternRepository.getAllPatterns().collect { patterns ->
                _patterns.value = patterns
                _isLoading.value = false
            }
        }
    }

    fun togglePatternEnabled(patternId: Long, enabled: Boolean) {
        viewModelScope.launch {
            patternRepository.setPatternEnabled(patternId, enabled)
        }
    }

    fun deletePattern(pattern: NotificationPattern) {
        viewModelScope.launch {
            patternRepository.deletePattern(pattern)
        }
    }

    fun installPattern(pattern: NotificationPattern) {
        viewModelScope.launch {
            patternRepository.insertPattern(pattern)
        }
    }

    fun stopGlyph() {
        NotificationParserService.stopGlyphDisplay()
    }
}
