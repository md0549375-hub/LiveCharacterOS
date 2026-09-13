package com.killer.livecharacter.planner

import com.killer.livecharacter.interaction.LiveInteractionService

class InteractionPlanner {
    fun tap(x: Float, y: Float): Boolean {
        if (!x.isFinite() || !y.isFinite()) return false
        return LiveInteractionService.instance?.tap(x, y) == true
    }
}