package com.fnt.notiglyph.glyph.api

import android.content.Context
import android.graphics.Bitmap

/**
 * Facade for interacting with the device Glyph matrix capabilities.
 * Implementations may use reflection or a compiled SDK.
 */
interface GlyphController {
    /** Initialize the service and render provided text when ready. */
    fun initAndShowSample(context: Context, text: String)

    /** Clear the display by sending a blank frame. */
    fun clearDisplay()

    /** Update the Glyph matrix with a new bitmap frame. */
    fun updateFrame(bitmap: Bitmap)

    /** Turn off any active glyph rendering and uninitialize resources. */
    fun turnOff()
}
