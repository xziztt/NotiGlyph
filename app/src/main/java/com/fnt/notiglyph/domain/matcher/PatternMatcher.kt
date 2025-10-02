package com.fnt.notiglyph.domain.matcher

import com.fnt.notiglyph.domain.model.MatchResult
import com.fnt.notiglyph.domain.model.NotificationPattern

/**
 * Interface for pattern matching implementations
 */
interface PatternMatcher {
    /**
     * Attempt to match notification text against a pattern
     * @param notificationText The text from the notification
     * @param pattern The pattern to match against
     * @return MatchResult containing match status and extracted data
     */
    fun match(notificationText: String, pattern: NotificationPattern): MatchResult
}
