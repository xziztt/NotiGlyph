package com.fnt.notiglyph.domain.matcher

import com.fnt.notiglyph.domain.model.MatchResult
import com.fnt.notiglyph.domain.model.NotificationPattern

/**
 * Matcher for keyword patterns with boolean logic
 * Example: "delivered" OR "arrived"
 */
class KeywordPatternMatcher : PatternMatcher {
    override fun match(notificationText: String, pattern: NotificationPattern): MatchResult {
        try {
            val lowerText = notificationText.lowercase()
            val patternString = pattern.patternString.lowercase()

            val matched = when {
                patternString.contains(" or ", ignoreCase = true) -> {
                    // OR logic: any keyword must be present
                    val keywords = patternString.split(Regex("""\s+or\s+""", RegexOption.IGNORE_CASE))
                    keywords.any { keyword ->
                        lowerText.contains(keyword.trim())
                    }
                }
                patternString.contains(" and ", ignoreCase = true) -> {
                    // AND logic: all keywords must be present
                    val keywords = patternString.split(Regex("""\s+and\s+""", RegexOption.IGNORE_CASE))
                    keywords.all { keyword ->
                        val trimmed = keyword.trim()
                        if (trimmed.startsWith("not ", ignoreCase = true)) {
                            // NOT logic within AND
                            !lowerText.contains(trimmed.substring(4).trim())
                        } else {
                            lowerText.contains(trimmed)
                        }
                    }
                }
                patternString.startsWith("not ", ignoreCase = true) -> {
                    // NOT logic: keyword must not be present
                    val keyword = patternString.substring(4).trim()
                    !lowerText.contains(keyword)
                }
                else -> {
                    // Simple keyword match
                    lowerText.contains(patternString.trim())
                }
            }

            return if (matched) {
                MatchResult(
                    matched = true,
                    extractedData = emptyMap(),
                    displayText = pattern.displayTemplate
                )
            } else {
                MatchResult(matched = false)
            }
        } catch (e: Exception) {
            return MatchResult(matched = false)
        }
    }
}
