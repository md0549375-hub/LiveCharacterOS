package com.killer.livecharacter.render

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import com.killer.livecharacter.model.CharacterAssetSet
import com.killer.livecharacter.model.CharacterLayer
import com.killer.livecharacter.model.CharacterPose
import com.killer.livecharacter.model.LayerType
import kotlin.math.max

class CharacterRenderer {
    private val body = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND }
    private val limb = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeCap = Paint.Cap.ROUND }
    private val face = Paint(Paint.ANTI_ALIAS_FLAG)
    private val outline = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 4f }
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.DEFAULT_BOLD }
    private val assets = CharacterAssetSet("default", listOf(
        CharacterLayer("shadow", LayerType.SHADOW, zIndex = 0), CharacterLayer("body", LayerType.BODY, zIndex = 10),
        CharacterLayer("outfit", LayerType.OUTFIT, zIndex = 20), CharacterLayer("head", LayerType.HEAD, zIndex = 30),
        CharacterLayer("face", LayerType.FACE, zIndex = 40), CharacterLayer("hair", LayerType.HAIR, zIndex = 50),
        CharacterLayer("accessory", LayerType.ACCESSORY, zIndex = 60), CharacterLayer("effect", LayerType.EFFECT, zIndex = 70)
    ))
    fun draw(canvas: Canvas, pose: CharacterPose, width: Float, height: Float, targetX: Float?, targetY: Float?) {
        canvas.save(); canvas.rotate(pose.lean * 57.3f, pose.shoulderX, pose.shoulderY)
        for (layer in assets.sortedVisible()) drawLayer(canvas, layer, pose, width, height)
        canvas.restore()
        if (targetX != null && targetY != null) drawTarget(canvas, targetX, targetY)
        text.textSize = 14f; text.color = 0xE6FFFFFF.toInt(); canvas.drawText("LIVE CHARACTER OS", 18f, 26f, text)
        text.textSize = 11f; text.color = 0x99FFFFFF.toInt(); canvas.drawText("drag • IK • physics • layered renderer", 18f, 44f, text)
    }
    private fun drawLayer(canvas: Canvas, layer: CharacterLayer, p: CharacterPose, w: Float, h: Float) {
        val alpha = (layer.opacity.coerceIn(0f, 1f) * 255).toInt()
        when (layer.type) {
            LayerType.SHADOW -> { face.color = 0x55000000; canvas.drawOval(p.shoulderX - 62f, p.shoulderY + 92f, p.shoulderX + 62f, p.shoulderY + 112f, face) }
            LayerType.BODY -> {
                body.color = withAlpha(0xFFE8E8F0.toInt(), alpha); body.strokeWidth = 34f; canvas.drawLine(p.shoulderX, p.shoulderY - 10f, p.shoulderX, p.shoulderY + 112f, body)
                limb.color = withAlpha(0xFFD0D1DD.toInt(), alpha); limb.strokeWidth = 17f; canvas.drawLine(p.shoulderX, p.shoulderY - 8f, p.elbowX, p.elbowY, limb); canvas.drawLine(p.elbowX, p.elbowY, p.handX, p.handY, limb)
                face.color = withAlpha(0xFFC2C4D1.toInt(), alpha); canvas.drawCircle(p.elbowX, p.elbowY, 10f, face); canvas.drawCircle(p.handX, p.handY, 14f, face)
            }
            LayerType.OUTFIT -> { face.color = withAlpha(0xFF7D5CFF.toInt(), alpha); val path = Path().apply { moveTo(p.shoulderX - 30f, p.shoulderY + 5f); lineTo(p.shoulderX + 30f, p.shoulderY + 5f); lineTo(p.shoulderX + 38f, p.shoulderY + 100f); lineTo(p.shoulderX - 38f, p.shoulderY + 100f); close() }; canvas.drawPath(path, face) }
            LayerType.HEAD -> { face.color = withAlpha(0xFFFFD9D0.toInt(), alpha); canvas.drawCircle(p.headX, p.headY, 48f, face); outline.color = withAlpha(0xAA3C2C38.toInt(), alpha); canvas.drawCircle(p.headX, p.headY, 48f, outline) }
            LayerType.FACE -> { face.color = withAlpha(0xFF302C3D.toInt(), alpha); canvas.drawCircle(p.headX - 16f, p.headY - 4f, 4f, face); canvas.drawCircle(p.headX + 16f, p.headY - 4f, 4f, face); outline.color = withAlpha(0xAA6C3B50.toInt(), alpha); outline.strokeWidth = 3f; canvas.drawArc(p.headX - 16f, p.headY + 2f, p.headX + 16f, p.headY + 22f, 10f, 160f, false, outline) }
            LayerType.HAIR -> { face.color = withAlpha(0xFF2B2039.toInt(), alpha); val path = Path().apply { moveTo(p.headX - 44f, p.headY - 20f); cubicTo(p.headX - 42f, p.headY - 62f, p.headX + 38f, p.headY - 66f, p.headX + 48f, p.headY - 18f); lineTo(p.headX + 58f, p.headY + 56f); lineTo(p.headX + 24f, p.headY + 25f); lineTo(p.headX + 4f, p.headY + 62f); lineTo(p.headX - 18f, p.headY + 24f); lineTo(p.headX - 54f, p.headY + 52f); close() }; canvas.drawPath(path, face) }
            LayerType.ACCESSORY -> { face.color = withAlpha(0xFFBFA5FF.toInt(), alpha); canvas.drawCircle(p.headX + 39f, p.headY - 26f, 7f, face); canvas.drawCircle(p.headX - 39f, p.headY - 26f, 7f, face) }
            LayerType.EFFECT -> { val pulse = (1f + kotlin.math.sin(p.idleOffsetY * 0.4f) * 0.06f).coerceIn(0.92f, 1.08f); outline.color = withAlpha(0x667D5CFF, alpha); outline.strokeWidth = 3f; canvas.drawCircle(p.headX, p.headY, 58f * pulse, outline) }
        }
    }
    private fun drawTarget(canvas: Canvas, x: Float, y: Float) { outline.color = 0xFFFF4D7D.toInt(); outline.strokeWidth = 4f; canvas.drawCircle(x, y, max(24f, 42f), outline); canvas.drawLine(x - 9f, y, x + 9f, y, outline); canvas.drawLine(x, y - 9f, x, y + 9f, outline) }
    private fun withAlpha(color: Int, alpha: Int): Int = (color and 0x00FFFFFF) or (alpha shl 24)
}