package com.example

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.os.Build
import android.view.accessibility.AccessibilityEvent

class FloatingAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Standard event processing (no inspection needed)
    }

    override fun onInterrupt() {
        // Unused
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        FloatingPanelState.addLog("Accessibility service CONNECTED.")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        FloatingPanelState.addLog("Accessibility service DISCONNECTED.")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    /**
     * Programmatically performs a tap gesture at a specific coordinate [x, y].
     * AccessibilityService must be enabled in settings.
     */
    fun dispatchTap(x: Float, y: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val path = Path().apply {
                moveTo(x, y)
            }
            val gestureBuilder = GestureDescription.Builder()
            val strokeDescription = GestureDescription.StrokeDescription(path, 0, 45)
            gestureBuilder.addStroke(strokeDescription)

            dispatchGesture(gestureBuilder.build(), object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    FloatingPanelState.addLog("Success: Automated tap executed at ($x, $y)")
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                    FloatingPanelState.addLog("Error: Tap gesture interrupted.")
                }
            }, null)
        } else {
            FloatingPanelState.addLog("Failed: Touch automation requires Android Nougat or above.")
        }
    }

    companion object {
        var instance: FloatingAccessibilityService? = null
            private set

        val isServiceRunning: Boolean
            get() = instance != null
    }
}
