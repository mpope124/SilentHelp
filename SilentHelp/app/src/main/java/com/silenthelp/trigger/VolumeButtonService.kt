package com.silenthelp.trigger

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.silenthelp.ui.fakecall.FakeCallActivity

class VolumeButtonService : AccessibilityService() {
    private val combo = listOf(
        KeyEvent.KEYCODE_VOLUME_UP,
        KeyEvent.KEYCODE_VOLUME_UP,
        KeyEvent.KEYCODE_VOLUME_DOWN,
        KeyEvent.KEYCODE_VOLUME_DOWN,
        KeyEvent.KEYCODE_VOLUME_UP
    )
    private val buf = ArrayDeque<Int>(combo.size)

    override fun onServiceConnected() {
        Log.d("VolService", "AccessibilityService connected")
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        /* no-op */
    }

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

    private fun launchFakeCall() {
        Intent(this, FakeCallActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(this)
        }
    }

    override fun onInterrupt() { /* no-op */ }
}
