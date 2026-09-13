package com.killer.livecharacter.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.view.MotionEvent
import android.view.View
import com.killer.livecharacter.engine.CharacterEngine
import com.killer.livecharacter.render.CharacterRenderer

class CharacterCanvasView(context: Context, private val engine: CharacterEngine) : View(context) {
    private val renderer = CharacterRenderer()
    private var lastFrameNanos = System.nanoTime()
    init { setBackgroundColor(Color.rgb(11, 12, 16)) }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val now = System.nanoTime()
        val dt = ((now - lastFrameNanos) / 1_000_000_000f).coerceIn(0.001f, 0.033f)
        lastFrameNanos = now
        engine.update(width.toFloat(), height.toFloat(), dt)
        val pose = engine.pose(); val target = engine.target
        renderer.draw(canvas, pose, width.toFloat(), height.toFloat(), target?.x, target?.y)
        postInvalidateOnAnimation()
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!event.x.isFinite() || !event.y.isFinite()) return false
        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> { parent?.requestDisallowInterceptTouchEvent(true); engine.setTarget(event.x, event.y); true }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { parent?.requestDisallowInterceptTouchEvent(false); engine.clearTarget(); true }
            else -> true
        }
    }
}