package com.fnt.notiglyph.glyph.runtime

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.fnt.notiglyph.glyph.api.GlyphController

// Direct imports from Glyph Matrix SDK AAR
// Try ketchum package first, if build fails switch to: com.nothing.glyph.matrix.*
import com.nothing.ketchum.GlyphMatrixManager
import com.nothing.ketchum.GlyphMatrixFrame
import com.nothing.ketchum.GlyphMatrixObject
import com.nothing.ketchum.Glyph

/**
 * Direct AAR implementation of GlyphController.
 * Uses compile-time binding to Glyph Matrix SDK instead of reflection.
 */
class ReflectiveGlyphController : GlyphController {
    private var manager: GlyphMatrixManager? = null
    private var callback: GlyphMatrixManager.Callback? = null
    private var pendingText: String? = null
    private var context: Context? = null

    companion object {
        private const val TAG = "ReflectiveGlyphCtrl"
    }

    override fun initAndShowSample(context: Context, text: String) {
        try {
            this.context = context
            pendingText = text

            manager = GlyphMatrixManager.getInstance(context.applicationContext)

            // Create callback for service connection
            callback = object : GlyphMatrixManager.Callback {
                override fun onServiceConnected(name: ComponentName?) {
                    this@ReflectiveGlyphController.onServiceConnected()
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    Log.d(TAG, "Service disconnected")
                }
            }

            manager?.init(callback)
        } catch (e: Throwable) {
            e.printStackTrace()
            Log.e(TAG, "Init error: ${e.javaClass.simpleName}: ${e.message}", e)
        }
    }

    private fun onServiceConnected() {
        try {
            val mgr = manager ?: return

            // Register device using official API: register(String target)
            // For Phone 3, use "DEVICE_23112" constant from Glyph class
            val deviceTarget = try {
                Glyph.DEVICE_23112
            } catch (_: Throwable) {
                "DEVICE_23112" // Fallback if constant not found
            }

            try {
                mgr.register(deviceTarget)
                Log.d(TAG, "Device registered successfully, ready for animation")
            } catch (e: Throwable) {
                e.printStackTrace()
                Log.e(TAG, "Could not register device: ${e.message}", e)
                return
            }

            // Don't render anything here - let the animator handle all rendering
            // This prevents race conditions between init and animator's clear/render cycle
        } catch (e: Throwable) {
            e.printStackTrace()
            Log.e(TAG, "Service connection error: ${e.javaClass.simpleName}", e)
        }
    }

    override fun clearDisplay() {
        try {
            val ctx = context ?: return
            val mgr = manager ?: return

            // Create a blank black bitmap
            val blankBitmap = Bitmap.createBitmap(25, 25, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(blankBitmap)
            canvas.drawColor(android.graphics.Color.BLACK)

            // Send multiple blank frames to fully overwrite the hardware buffer
            repeat(5) {
                try {
                    val blankBuilder = GlyphMatrixObject.Builder()
                    blankBuilder.setImageSource(blankBitmap)
                    blankBuilder.setScale(100)
                    blankBuilder.setOrientation(0)
                    blankBuilder.setPosition(0, 0)
                    val blankObj = blankBuilder.build()

                    val blankFrameBuilder = GlyphMatrixFrame.Builder()
                    blankFrameBuilder.addTop(blankObj)
                    val blankFrame = blankFrameBuilder.build(ctx.applicationContext)
                    val blankRendered = blankFrame.render()
                    mgr.setMatrixFrame(blankRendered)

                    // Small delay between frames to ensure hardware processes each one
                    Thread.sleep(30)
                } catch (e: Throwable) {
                    Log.w(TAG, "Failed to send blank frame ${it + 1}: ${e.message}")
                }
            }

            blankBitmap.recycle()
            Log.d(TAG, "Display cleared with 5 blank frames")
        } catch (e: Throwable) {
            Log.w(TAG, "Clear display failed: ${e.message}")
        }
    }

    override fun updateFrame(bitmap: Bitmap) {
        try {
            val ctx = context ?: return
            val mgr = manager ?: return

            val objBuilder = GlyphMatrixObject.Builder()

            // Set bitmap as image source
            objBuilder.setImageSource(bitmap)

            // Configure object properties
            objBuilder.setScale(100)
            objBuilder.setOrientation(0)
            objBuilder.setPosition(0, 0)
            val glyphObj = objBuilder.build()

            // Build frame
            val frameBuilder = GlyphMatrixFrame.Builder()
            frameBuilder.addTop(glyphObj)
            val frame = frameBuilder.build(ctx.applicationContext)

            // Render and send to matrix
            val rendered = frame.render()
            mgr.setMatrixFrame(rendered)
        } catch (e: Throwable) {
            e.printStackTrace()
            Log.e(TAG, "Update frame error: ${e.message}", e)
        }
    }

    override fun turnOff() {
        try {
            manager?.let { mgr ->
                // Turn off the Glyph display
                try {
                    mgr.turnOff()
                    Log.d(TAG, "Glyph turned off successfully")
                } catch (e: Throwable) {
                    Log.e(TAG, "Error calling turnOff()", e)
                }

                // Uninitialize the manager
                try {
                    mgr.unInit()
                    Log.d(TAG, "Glyph manager uninitialized successfully")
                } catch (e: Throwable) {
                    Log.e(TAG, "Error calling unInit()", e)
                }

                // Clear references
                manager = null
                callback = null
                context = null
            } ?: Log.d(TAG, "turnOff() called but manager is null")
        } catch (e: Throwable) {
            Log.e(TAG, "Unexpected error in turnOff()", e)
        }
    }
}
