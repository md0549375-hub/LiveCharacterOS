package com.killer.livecharacter.interaction

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent

class LiveInteractionService : AccessibilityService() {
    companion object {
        @Volatile var instance: LiveInteractionService? = null
            private set
    }
    override fun onServiceConnected() { super.onServiceConnected(); instance = this }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit
    override fun onDestroy() { instance = null; super.onDestroy() }
    fun tap(x: Float, y: Float, durationMs: Long = 80L): Boolean {
        if (!x.isFinite() || !y.isFinite()) return false
        val duration = durationMs.coerceIn(1L, 10_000L)
        val path = Path().apply { moveTo(x, y) }
        return dispatchGesture(GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(path, 0L, duration)).build(), null, null)
    }
}