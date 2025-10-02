package com.fnt.notiglyph.domain.matcher

import com.fnt.notiglyph.domain.model.MatchResult
import com.fnt.notiglyph.domain.model.NotificationPattern
import java.util.regex.Pattern

/**
 * Matcher for template patterns using {variable} syntax
 * Example: "arriving in {minutes} min" matches "arriving in 15 min"
 */
class TemplatePatternMatcher : PatternMatcher {
    override fun match(notificationText: String, pattern: NotificationPattern): MatchResult {
        try {
            // Convert template pattern to regex
            // Replace {variable} with named capture groups
            val variables = mutableListOf<String>()
            var regexPattern = Pattern.quote(pattern.patternString)

            // Find all {variable} placeholders
            val variableRegex = Regex("""\\\{(\w+)\\\}""")
            val matches = variableRegex.findAll(regexPattern)

            for (match in matches) {
                val variable = match.groupValues[1]
                variables.add(variable)
                // Replace quoted {variable} with a non-greedy capture group
                regexPattern = regexPattern.replaceFirst("""\\\{$variable\\\}""", """\\E(.+?)\\Q""")
            }

            // Remove the quotes we added with Pattern.quote at the ends
            regexPattern = "\\Q$regexPattern\\E"
                .replace("\\Q\\E", "")  // Remove empty quotes
                .replace("\\E(.+?)\\Q", "(.+?)")  // Simplify capture groups

            // Try to match
            val regex = Regex(regexPattern, RegexOption.IGNORE_CASE)
            val matchResult = regex.find(notificationText)

            if (matchResult != null) {
                // Extract variables
                val extractedData = mutableMapOf<String, String>()
                variables.forEachIndexed { index, varName ->
                    if (index + 1 <= matchResult.groupValues.size) {
                        extractedData[varName] = matchResult.groupValues[index + 1]
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
