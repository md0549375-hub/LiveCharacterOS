package com.killer.livecharacter.perception

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager

class ScreenPerceptionController(private val activity: Activity) {
    companion object { const val REQUEST_CAPTURE = 7401 }
    fun createPermissionIntent(): Intent =
        (activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager).createScreenCaptureIntent()
}