package com.killer.livecharacter.model

data class Vec2(val x: Float, val y: Float) {
    fun finite(): Vec2 = Vec2(if (x.isFinite()) x else 0f, if (y.isFinite()) y else 0f)
}

data class CharacterProfile(
    val id: String,
    val name: String,
    val gravity: Float = 980f,
    val massKg: Float = 52f,
    val armReachPx: Float = 180f,
    val followSpeed: Float = 9f,
    val idleBreathSpeed: Float = 2.2f,
    val idleBreathAmount: Float = 3f,
    val headRadius: Float = 48f,
    val torsoLength: Float = 170f,
    val upperArmRatio: Float = 0.52f,
    val lowerArmRatio: Float = 0.48f
) {
    fun sanitized(): CharacterProfile = copy(
        gravity = gravity.coerceIn(0f, 5000f), massKg = massKg.coerceIn(0.1f, 500f),
        armReachPx = armReachPx.coerceIn(20f, 2000f), followSpeed = followSpeed.coerceIn(0.1f, 60f),
        idleBreathSpeed = idleBreathSpeed.coerceIn(0f, 20f), idleBreathAmount = idleBreathAmount.coerceIn(0f, 100f),
        headRadius = headRadius.coerceIn(4f, 300f), torsoLength = torsoLength.coerceIn(10f, 1000f),
        upperArmRatio = upperArmRatio.coerceIn(0.1f, 0.9f), lowerArmRatio = lowerArmRatio.coerceIn(0.1f, 0.9f)
    )
}

data class TouchTarget(val id: String, var x: Float, var y: Float, var radius: Float = 45f)

data class CharacterPose(
    val rootX: Float, val rootY: Float, val shoulderX: Float, val shoulderY: Float,
    val elbowX: Float, val elbowY: Float, val handX: Float, val handY: Float,
    val headX: Float, val headY: Float, val idleOffsetY: Float, val lean: Float, val armWeight: Float
)

enum class LayerType { SHADOW, BODY, OUTFIT, HEAD, FACE, HAIR, ACCESSORY, EFFECT }

data class CharacterLayer(val id: String, val type: LayerType, val visible: Boolean = true, val opacity: Float = 1f, val zIndex: Int = 0)

data class CharacterAssetSet(val characterId: String, val layers: List<CharacterLayer>) {
    fun sortedVisible(): List<CharacterLayer> = layers.filter { it.visible && it.opacity > 0f }.sortedBy { it.zIndex }
}

data class RuntimeMetrics(val fps: Float = 0f, val frameTimeMs: Float = 0f, val targetActive: Boolean = false, val overlayActive: Boolean = false)