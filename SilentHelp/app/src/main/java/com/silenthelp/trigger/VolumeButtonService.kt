// Listens for volume button combo to launch FakeCallActivity via Accessibility Service
// Created by Kelley Rosa
package com.silenthelp.trigger

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.silenthelp.ui.fakecall.FakeCallActivity

class VolumeButtonService : AccessibilityService() {

    // =========================================================================
    // KEY COMBO DEFINITION & BUFFER
    // =========================================================================
    /** The target sequence of volume key codes (Up, Up, Down, Down, Up). */
    private val combo = listOf(
        KeyEvent.KEYCODE_VOLUME_UP,
        KeyEvent.KEYCODE_VOLUME_UP,
        KeyEvent.KEYCODE_VOLUME_DOWN,
        KeyEvent.KEYCODE_VOLUME_DOWN,
        KeyEvent.KEYCODE_VOLUME_UP
    )

    /** Rolling buffer holding the most recent key-up events. */
    private val buf = ArrayDeque<Int>(combo.size)

    // =========================================================================
    // SERVICE LIFECYCLE & CONFIGURATION
    // =========================================================================
    /** Sends Log when AccessibilityService for SilentHelp is bound */
    override fun onServiceConnected() {
        Log.d("VolService", "AccessibilityService connected")
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        }
    }

    /** Delivers accessibility events to service */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    // =========================================================================
    // KEY EVENT HANDLING
    // =========================================================================
    /** Logs evey key event */
    override fun onKeyEvent(event: KeyEvent): Boolean {
        Log.d("VolService", "KeyEvent code=${event.keyCode} action=${event.action}")
        if (event.action == KeyEvent.ACTION_UP) {
            buf.addLast(event.keyCode)
            if (buf.size > combo.size) buf.removeFirst()
            if (buf.size == combo.size && buf.toList() == combo) {
                launchFakeCall()
                buf.clear()
            }
        }
        return true
    }

    // =========================================================================
    // Helpers
    // =========================================================================
    /** Launches FakeCallActivity as a new task */
    private fun launchFakeCall() {
        Intent(this, FakeCallActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(this)
        }
    }

    /** Called if service is interrupted */
    override fun onInterrupt() = Unit
}
