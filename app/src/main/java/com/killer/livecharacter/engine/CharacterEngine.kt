package com.killer.livecharacter.engine

import com.killer.livecharacter.model.CharacterPose
import com.killer.livecharacter.model.CharacterProfile
import com.killer.livecharacter.model.TouchTarget
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

class CharacterEngine(profile: CharacterProfile) {
    val profile: CharacterProfile = profile.sanitized()
    var rootX = 0.5f; private set
    var rootY = 0.62f; private set
    var target: TouchTarget? = null; private set
    private var viewportWidth = 1f
    private var viewportHeight = 1f
    private var elapsedSeconds = 0f
    private var handInitialized = false
    private var velocityY = 0f
    private var shoulderInitialized = false
    var shoulderX = 0f; private set
    var shoulderY = 0f; private set
    var elbowX = 0f; private set
    var elbowY = 0f; private set
    var handX = 0f; private set
    var handY = 0f; private set
    var headX = 0f; private set
    var headY = 0f; private set
    var lean = 0f; private set
    private val upperArmLength get() = profile.armReachPx * profile.upperArmRatio
    private val lowerArmLength get() = profile.armReachPx * profile.lowerArmRatio

    fun update(width: Float, height: Float, dtSeconds: Float) {
        viewportWidth = width.coerceAtLeast(1f); viewportHeight = height.coerceAtLeast(1f)
        val dt = if (dtSeconds.isFinite()) dtSeconds.coerceIn(0.001f, 0.033f) else 0.016f
        elapsedSeconds += dt
        val breath = if (target == null) sin(elapsedSeconds * profile.idleBreathSpeed) * profile.idleBreathAmount else 0f
        val desiredShoulderY = viewportHeight * rootY + breath
        if (!shoulderInitialized) { shoulderY = desiredShoulderY; shoulderInitialized = true }
        val gravityStep = profile.gravity * dt * 0.04f
        velocityY = (velocityY + gravityStep).coerceIn(-200f, 200f)
        val spring = (desiredShoulderY - shoulderY) * 0.06f
        shoulderY += (velocityY + spring) * dt
        velocityY *= 0.92f
        shoulderX = viewportWidth * rootX
        if (!shoulderY.isFinite()) shoulderY = desiredShoulderY
        val torsoHalf = profile.torsoLength * 0.5f
        headX = shoulderX; headY = shoulderY - torsoHalf - profile.headRadius * 1.2f
        if (!handInitialized) { handX = shoulderX; handY = shoulderY + upperArmLength * 0.72f; handInitialized = true }
        val currentTarget = target
        val desired = if (currentTarget == null) shoulderX to (shoulderY + upperArmLength * 0.72f) else {
            val dx = currentTarget.x - shoulderX; val dy = currentTarget.y - shoulderY
            val distance = hypot(dx, dy).coerceAtLeast(0.001f)
            val reachable = (upperArmLength + lowerArmLength - 1f).coerceAtLeast(1f)
            val scale = minOf(1f, reachable / distance)
            shoulderX + dx * scale to shoulderY + dy * scale
        }
        val follow = (dt * profile.followSpeed).coerceIn(0f, 1f)
        handX += (desired.first - handX) * follow; handY += (desired.second - handY) * follow
        if (!handX.isFinite() || !handY.isFinite()) { handX = shoulderX; handY = shoulderY + upperArmLength * 0.72f }
        lean = ((handX - shoulderX) / profile.armReachPx.coerceAtLeast(1f) * 0.08f).coerceIn(-0.16f, 0.16f)
        solveArm()
    }
    fun setTarget(x: Float, y: Float) { if (x.isFinite() && y.isFinite()) target = TouchTarget("canvas-target", x, y) }
    fun clearTarget() { target = null }
    fun pose() = CharacterPose(rootX, rootY, shoulderX, shoulderY, elbowX, elbowY, handX, handY, headX, headY, sin(elapsedSeconds * profile.idleBreathSpeed) * profile.idleBreathAmount, lean, if (target == null) 0f else 1f)
    private fun solveArm() {
        val upper = upperArmLength; val lower = lowerArmLength
        val dx = handX - shoulderX; val dy = handY - shoulderY; val rawDistance = hypot(dx, dy)
        val minReach = kotlin.math.abs(upper - lower) + 0.1f; val maxReach = upper + lower - 0.1f
        val distance = rawDistance.coerceIn(minReach, maxReach)
        val directionX = if (rawDistance < 0.001f) 0f else dx / rawDistance
        val directionY = if (rawDistance < 0.001f) 1f else dy / rawDistance
        val baseAngle = atan2(directionY, directionX)
        val cosAngle = ((upper * upper + distance * distance - lower * lower) / (2f * upper * distance)).coerceIn(-1f, 1f)
        val elbowAngle = baseAngle + acos(cosAngle)
        elbowX = shoulderX + cos(elbowAngle) * upper; elbowY = shoulderY + sin(elbowAngle) * upper
        if (!elbowX.isFinite() || !elbowY.isFinite()) { elbowX = shoulderX; elbowY = shoulderY + upper }
    }
}