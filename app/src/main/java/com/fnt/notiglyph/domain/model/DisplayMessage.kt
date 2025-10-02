package com.fnt.notiglyph.domain.model

/**
 * Message to be displayed on the Glyph matrix
 */
data class DisplayMessage(
    val text: String,
    val icon: String? = null,
    val durationSeconds: Int = 30,
    val priority: Int = 5
)
