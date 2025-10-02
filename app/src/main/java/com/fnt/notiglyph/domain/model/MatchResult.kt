package com.fnt.notiglyph.domain.model

/**
 * Result of pattern matching operation
 */
data class MatchResult(
    val matched: Boolean,
    val extractedData: Map<String, String> = emptyMap(),
    val displayText: String = ""
)
