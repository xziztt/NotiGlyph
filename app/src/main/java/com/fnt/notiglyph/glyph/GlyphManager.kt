package com.fnt.notiglyph.glyph

import android.content.Context
import android.util.Log
import com.fnt.notiglyph.glyph.animation.ScrollingTextAnimator
import com.fnt.notiglyph.glyph.runtime.ReflectiveGlyphController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Singleton manager for Glyph display to prevent multiple instances
 */
object GlyphManager {
    private const val TAG = "GlyphManager"

    private var glyph: ReflectiveGlyphController? = null
    private var animator: ScrollingTextAnimator? = null
    private var glyphInitialized = false
    private var currentDisplayJob: Job? = null

    // Use a separate scope independent of any ViewModel
    private val glyphScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /**
     * Display text on Glyph for specified duration with optional delay
     */
    fun displayText(context: Context, text: String, durationSeconds: Int, delaySeconds: Int = 0) {

        synchronized(this) {
            // Cancel any existing display
            stopDisplay()


            currentDisplayJob = glyphScope.launch {
                try {
                    // Wait before displaying if delay is set
                    if (delaySeconds > 0) {
                        Log.d(TAG, "Waiting ${delaySeconds}s before display")
                        delay(delaySeconds * 1000L)
                    }

                    Log.d(TAG, "Displaying: $text for ${durationSeconds}s")

                    // Initialize if needed
                    if (glyph == null) {
                        glyph = ReflectiveGlyphController()
                        animator = ScrollingTextAnimator(
                            onFrameUpdate = { bitmap -> glyph?.updateFrame(bitmap) },
                            onClearDisplay = { glyph?.clearDisplay() }
                        )
                    }

                    val currentGlyph = glyph ?: return@launch
                    val currentAnimator = animator ?: return@launch

                    // Initialize Glyph if first time
                    if (!glyphInitialized) {
                        currentGlyph.initAndShowSample(context, text)
                        glyphInitialized = true
                        delay(500)
                    }

                    // Start animation
                    currentAnimator.start(text)

                    // Run for specified duration
                    delay(durationSeconds * 1000L)

                    // Stop and cleanup
                    currentAnimator.stop()
                    currentGlyph.turnOff()
                    glyphInitialized = false

                    Log.d(TAG, "Display completed")
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) {
                        Log.d(TAG, "Display cancelled")
                    } else {
                        Log.e(TAG, "Error during display", e)
                    }
                } finally {
                    currentDisplayJob = null
                }
            }
        }
    }

    /**
     * Stop any active Glyph display immediately
     */
    fun stopDisplay() {
        synchronized(this) {
            try {
                Log.d(TAG, "Stopping display")

                // Cancel current job
                currentDisplayJob?.cancel()
                currentDisplayJob = null

                // Stop animation and turn off
                animator?.stop()
                glyph?.clearDisplay()
                glyph?.turnOff()
                glyphInitialized = false

                Log.d(TAG, "Display stopped")
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping display", e)
            }
        }
    }

    /**
     * Check if Glyph is currently displaying
     */
    fun isDisplaying(): Boolean {
        return currentDisplayJob?.isActive == true
    }

    /**
     * Cleanup all resources (call when app is shutting down)
     */
    fun cleanup() {
        synchronized(this) {
            stopDisplay()
            glyph = null
            animator = null
            Log.d(TAG, "Cleanup completed")
        }
    }
}
