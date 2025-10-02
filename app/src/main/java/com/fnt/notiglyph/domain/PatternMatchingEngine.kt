package com.fnt.notiglyph.domain

import com.fnt.notiglyph.domain.matcher.KeywordPatternMatcher
import com.fnt.notiglyph.domain.matcher.PatternMatcher
import com.fnt.notiglyph.domain.matcher.RegexPatternMatcher
import com.fnt.notiglyph.domain.matcher.TemplatePatternMatcher
import com.fnt.notiglyph.domain.model.MatchResult
import com.fnt.notiglyph.domain.model.NotificationPattern
import com.fnt.notiglyph.domain.model.PatternType

/**
 * Orchestrates pattern matching across different pattern types
 */
class PatternMatchingEngine {
    private val matchers: Map<PatternType, PatternMatcher> = mapOf(
        PatternType.TEMPLATE to TemplatePatternMatcher(),
        PatternType.REGEX to RegexPatternMatcher(),
        PatternType.KEYWORD to KeywordPatternMatcher()
    )

    /**
     * Try to match notification text against a list of patterns
     * Returns the first match based on priority order
     */
    fun findMatch(
        notificationText: String,
        patterns: List<NotificationPattern>
    ): Pair<NotificationPattern, MatchResult>? {
        // Patterns should already be sorted by priority (highest first)
        for (pattern in patterns) {
            val matcher = matchers[pattern.patternType] ?: continue
            val result = matcher.match(notificationText, pattern)

            if (result.matched) {
                return Pair(pattern, result)
            }
        }

        return null
    }

    /**
     * Match against a single pattern
     */
    fun matchPattern(
        notificationText: String,
        pattern: NotificationPattern
    ): MatchResult {
        val matcher = matchers[pattern.patternType] ?: return MatchResult(matched = false)
        return matcher.match(notificationText, pattern)
    }
}
