package com.fnt.notiglyph.glyph

/**
 * Constants for Glyph Toy system events and messaging.
 * These are used by Nothing OS to communicate with the Glyph Toy service.
 */
object GlyphToy {
    /**
     * Message type for Glyph Toy events
     */
    const val MSG_GLYPH_TOY = 1

    /**
     * Bundle key for event data
     */
    const val MSG_GLYPH_TOY_DATA = "GlyphToyData"

    /**
     * AOD (Always-On Display) event - triggered every minute
     */
    const val EVENT_AOD = "EVENT_AOD"

    /**
     * Change event - triggered on long press of Glyph button
     */
    const val EVENT_CHANGE = "EVENT_CHANGE"
}
