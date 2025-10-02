package com.fnt.notiglyph.glyph.animation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper

/**
 * Handles horizontal scrolling text animation for the Glyph matrix.
 * Text scrolls from left to right and wraps around continuously.
 */
class ScrollingTextAnimator(
    private val onFrameUpdate: (Bitmap) -> Unit,
    private val onClearDisplay: () -> Unit = {}
) {
    private val handler = Handler(Looper.getMainLooper())
    private var isAnimating = false

    private var currentX = 0f
    private var textWidth = 0f
    private var currentText = ""

    private val paint = Paint().apply {
        color = Color.WHITE
        textSize = 13f
        isAntiAlias = false
    }

    private val bitmap = Bitmap.createBitmap(25, 25, Bitmap.Config.ARGB_8888)
    private val canvas = Canvas(bitmap)

    // Animation parameters
    private val frameDelayMs = 33L // ~30 FPS
    private val scrollSpeed = 1f // pixels per frame

    private val animationRunnable = object : Runnable {
        override fun run() {
            if (!isAnimating) return

            // Clear canvas - use BLACK instead of TRANSPARENT for Glyph hardware
            canvas.drawColor(Color.BLACK)

            // Draw text at current position
            canvas.drawText(currentText, currentX, 15f, paint)

            // Update position
            currentX -= scrollSpeed

            // Wrap around when text scrolls off screen
            if (currentX < -textWidth) {
                currentX = 25f
            }

            // Send frame update
            onFrameUpdate(bitmap)

            // Schedule next frame
            handler.postDelayed(this, frameDelayMs)
        }
    }

    /**
     * Start scrolling animation with the given text
     */
    fun start(text: String) {
        // Check if text content has changed
        val textChanged = currentText != text

        if (isAnimating) stop()

        // Clear display only when text content changes (new API data)
        if (textChanged) {
            onClearDisplay()
        }

        currentText = text
        textWidth = paint.measureText(text)
        currentX = 25f // Start from right edge
        isAnimating = true

        handler.post(animationRunnable)
    }

    /**
     * Stop the animation
     */
    fun stop() {
        isAnimating = false
        handler.removeCallbacks(animationRunnable)
    }

    /**
     * Check if animation is currently running
     */
    fun isRunning() = isAnimating
}
