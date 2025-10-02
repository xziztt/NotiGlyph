package com.fnt.notiglyph.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fnt.notiglyph.data.database.entity.NotificationHistoryEntity
import com.fnt.notiglyph.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the notification history screen
 */
class HistoryViewModel(private val notificationRepository: NotificationRepository) : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationHistoryEntity>>(emptyList())
    val notifications: StateFlow<List<NotificationHistoryEntity>> = _notifications.asStateFlow()

    private val _filterMatched = MutableStateFlow<Boolean?>(null) // null = all, true = matched, false = unmatched
    val filterMatched: StateFlow<Boolean?> = _filterMatched.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            when (_filterMatched.value) {
                null -> {
                    notificationRepository.getAllNotifications().collect { notifications ->
                        _notifications.value = notifications
                    }
                }
                true -> {
                    notificationRepository.getMatchedNotifications().collect { notifications ->
                        _notifications.value = notifications
                    }
                }
                false -> {
                    notificationRepository.getAllNotifications().collect { notifications ->
                        _notifications.value = notifications.filter { !it.wasMatched }
                    }
                }
            }
        }
    }

    fun setFilter(matched: Boolean?) {
        _filterMatched.value = matched
        loadNotifications()
    }

    fun clearHistory() {
        viewModelScope.launch {
            notificationRepository.deleteAllNotifications()
        }
    }
}
