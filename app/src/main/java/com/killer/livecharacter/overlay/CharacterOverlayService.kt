package com.killer.livecharacter.overlay

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.killer.livecharacter.R
import com.killer.livecharacter.engine.CharacterEngine
import com.killer.livecharacter.model.CharacterProfile
import com.killer.livecharacter.rights.CharacterCatalog
import com.killer.livecharacter.rights.RightsGate
import com.killer.livecharacter.ui.CharacterCanvasView

class CharacterOverlayService : Service() {
    private var windowManager: WindowManager? = null
    private var view: CharacterCanvasView? = null

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= 26) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel("live_character", getString(R.string.overlay_channel_name), NotificationManager.IMPORTANCE_LOW)
            )
        }
        val notification = NotificationCompat.Builder(this, "live_character")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle("Live Character OS")
            .setContentText("Karakter katmanı aktif")
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= 34) startForeground(1101, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        else startForeground(1101, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val id = intent?.getStringExtra(EXTRA_CHARACTER_ID) ?: "astra"
        val profile = CharacterCatalog.find(id)
        if (profile == null || !RightsGate.canLoad(profile) || !Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        showOverlay(profile)
        return START_STICKY
    }

    private fun showOverlay(profile: CharacterProfile) {
        if (view != null) return
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        view = CharacterCanvasView(this, CharacterEngine(profile))
        val type = if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE
        val params = WindowManager.LayoutParams(
            280, 360, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 180
        }
        windowManager?.addView(view, params)
    }

    override fun onDestroy() {
        view?.let { runCatching { windowManager?.removeView(it) } }
        view = null
        windowManager = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val EXTRA_CHARACTER_ID = "character_id"
        fun intent(context: Context, characterId: String): Intent =
            Intent(context, CharacterOverlayService::class.java).putExtra(EXTRA_CHARACTER_ID, characterId)
    }
}
