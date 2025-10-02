package com.fnt.notiglyph.domain.matcher

import com.fnt.notiglyph.domain.model.MatchResult
import com.fnt.notiglyph.domain.model.NotificationPattern

/**
 * Matcher for regex patterns
 * Example: "ETA: (\\d+):(\\d+)" extracts hour and minute
 */
class RegexPatternMatcher : PatternMatcher {
    override fun match(notificationText: String, pattern: NotificationPattern): MatchResult {
        try {
            val regex = Regex(pattern.patternString, RegexOption.IGNORE_CASE)
            val matchResult = regex.find(notificationText)

            if (matchResult != null) {
                // Extract captured groups as numbered variables
                val extractedData = mutableMapOf<String, String>()

                // Get all capture groups (excluding group 0 which is the full match)
                matchResult.groupValues.forEachIndexed { index, value ->
                    if (index > 0) {
                        // Support both numbered (var1, var2) and user-defined variable names
                        val varName = if (index - 1 < pattern.extractedVariables.size) {
                            pattern.extractedVariables[index - 1]
                        } else {
                            "var$index"
                        }
                        extractedData[varName] = value
                    }
                }

                // Format display text
                var displayText = pattern.displayTemplate
                extractedData.forEach { (key, value) ->
                    displayText = displayText.replace("{$key}", value)
                }

                return MatchResult(
                    matched = true,
                    extractedData = extractedData,
                    displayText = displayText
                )
            }

            return MatchResult(matched = false)
        } catch (e: Exception) {
            return MatchResult(matched = false)
        }
    }
}
