package com.fnt.notiglyph.glyph

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import com.fnt.notiglyph.glyph.animation.ScrollingTextAnimator
import com.fnt.notiglyph.glyph.runtime.ReflectiveGlyphController
import com.fnt.notiglyph.service.NotificationParserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Service to register the app as a Glyph Toy in Nothing Phone's system settings.
 * Displays parsed notifications on the Glyph matrix.
 */
class GlyphToyService : Service() {
    private val TAG = "GlyphToyService"

    private lateinit var glyph: ReflectiveGlyphController
    private lateinit var animator: ScrollingTextAnimator

    private var serviceScope: CoroutineScope? = null

    /**
     * Handler for system events from Nothing OS
     */
    private val serviceHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                GlyphToy.MSG_GLYPH_TOY -> {
                    val bundle = msg.data
                    val event = bundle.getString(GlyphToy.MSG_GLYPH_TOY_DATA)

                    Log.d(TAG, "Received event: $event")

                    when (event) {
                        GlyphToy.EVENT_AOD -> {
                            // AOD event triggered every minute
                            Log.d(TAG, "AOD event rceived")
                        }
                        GlyphToy.EVENT_CHANGE -> {
                            // Button press event (long press to cycle toys)
                            Log.d(TAG, "Glyph button pressed")
                        }
                    }
                }
            }
            super.handleMessage(msg)
        }
    }

    private val serviceMessenger = Messenger(serviceHandler)

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "Service bound - initializing Glyph Toy")
        init()
        return serviceMessenger.binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "Service unbound - cleaning up")
        uninit()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        // Ensure cleanup happens if not already done
        if (serviceScope != null) {
            uninit()
        }
    }

    /**
     * Initialize Glyph components and start listening for matched notifications
     */
    private fun init() {
        // Create a new coroutine scope for this bind session
        serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        // Initialize Glyph controller
        glyph = ReflectiveGlyphController()

        // Initialize animator
        animator = ScrollingTextAnimator(
            onFrameUpdate = { bitmap -> glyph.updateFrame(bitmap) },
            onClearDisplay = { glyph.clearDisplay() }
        )

        // Observe matched notifications from NotificationParserService
        serviceScope?.launch {
            NotificationParserService.matchedNotifications.collectLatest { displayText ->
                Log.d(TAG, "Displaying on Glyph: $displayText")
                if (!animator.isRunning()) {
                    // First time initialization
                    glyph.initAndShowSample(this@GlyphToyService, displayText)
                    Handler(Looper.getMainLooper()).postDelayed({
                        animator.start(displayText)
                    }, 500)
                } else {
                    // Update existing animation
                    animator.start(displayText)
                }
            }
        }

        Log.d(TAG, "Initialization complete")
    }

    /**
     * Clean up resources and stop Glyph display
     */
    private fun uninit() {
        Log.d(TAG, "Cleaning up - stopping Glyph")

        // Cancel coroutine scope first to stop all async operations
        serviceScope?.cancel()
        serviceScope = null

        // Stop animation
        animator.stop()

        // Turn off Glyph display
        glyph.turnOff()

        Log.d(TAG, "Cleanup complete")
    }
}
