package com.fnt.notiglyph.domain.model

enum class PatternType {
    TEMPLATE,  // Uses {variable} placeholders
    REGEX,     // Uses regex patterns
    KEYWORD    // Uses keyword matching with boolean logic
}
