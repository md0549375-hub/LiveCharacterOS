package com.killer.livecharacter

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.content.pm.PackageManager
import android.provider.Settings
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.switchmaterial.SwitchMaterial
import com.killer.livecharacter.engine.CharacterEngine
import com.killer.livecharacter.model.CharacterProfile
import com.killer.livecharacter.overlay.CharacterOverlayService
import com.killer.livecharacter.perception.ScreenPerceptionController
import com.killer.livecharacter.storage.SecureSettingsStore
import com.killer.livecharacter.ui.CharacterCanvasView

class MainActivity : AppCompatActivity() {
    private lateinit var settings: SecureSettingsStore
    private lateinit var overlaySwitch: SwitchMaterial
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); settings = SecureSettingsStore(this); setContentView(buildUi()); requestNotificationPermissionIfNeeded()
    }
    private fun buildUi(): View {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(16), dp(16), dp(16)) }
        val scroll = ScrollView(this); val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }; scroll.addView(content)
        content.addView(label("LIVE CHARACTER OS", 24f)); content.addView(label("v1.0 • runtime • layered renderer • physics • overlay • secure settings", 12f).apply { alpha = .72f })
        content.addView(section("Character Preview")); content.addView(CharacterCanvasView(this, CharacterEngine(CharacterProfile("main", "Astra"))), LinearLayout.LayoutParams(-1, dp(420)))
        content.addView(section("Runtime")); content.addView(button("Android Etkileşim İznini Aç") { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) })
        content.addView(button("Ekran Üstü Karakter İznini Aç") { startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) })
        overlaySwitch = SwitchMaterial(this).apply { text = "Kalıcı ekran üstü karakter"; isChecked = settings.isOverlayEnabled(); setOnCheckedChangeListener { _, enabled -> settings.setOverlayEnabled(enabled); if (enabled) startOverlay() else stopService(Intent(this@MainActivity, CharacterOverlayService::class.java)) } }
        content.addView(overlaySwitch)
        content.addView(section("Perception")); content.addView(button("Screen Capture İznini Hazırla") { startActivityForResult(ScreenPerceptionController(this).createPermissionIntent(), ScreenPerceptionController.REQUEST_CAPTURE) })
        content.addView(label("MediaProjection yalnızca kullanıcı onayı sonrası kullanılabilir; ekran verisi varsayılan olarak kaydedilmez.", 12f).apply { alpha = .7f })
        content.addView(section("AI Provider")); val api = EditText(this).apply { hint = "Provider API key"; inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD }; content.addView(api)
        content.addView(button("API key'i güvenli kaydet") { settings.putApiKey(api.text?.toString().orEmpty()); api.text?.clear(); Toast.makeText(this, "API key Android Keystore ile şifrelendi.", Toast.LENGTH_SHORT).show() })
        content.addView(button("Kayıtlı API key var mı?") { Toast.makeText(this, if (settings.getApiKey().isNullOrBlank()) "Kayıtlı key yok" else "Güvenli key mevcut", Toast.LENGTH_SHORT).show() })
        content.addView(section("System Status")); content.addView(label("Core: CharacterEngine + IK + secondary motion", 13f)); content.addView(label("Renderer: layered procedural asset pipeline", 13f)); content.addView(label("Interaction: Accessibility gesture bridge", 13f)); content.addView(label("Overlay: foreground service + system overlay", 13f)); content.addView(label("Secrets: Android Keystore AES/GCM", 13f))
        root.addView(scroll, LinearLayout.LayoutParams(-1, -1)); return root
    }
    private fun requestNotificationPermissionIfNeeded() { if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 8101) }
    private fun startOverlay() {
        if (!Settings.canDrawOverlays(this)) { overlaySwitch.isChecked = false; startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))); Toast.makeText(this, "Önce ekran üstü iznini ver.", Toast.LENGTH_SHORT).show(); return }
        ContextCompat.startForegroundService(this, Intent(this, CharacterOverlayService::class.java))
    }
    private fun section(text: String) = TextView(this).apply { this.text = text; textSize = 16f; setPadding(0, dp(18), 0, dp(8)) }
    private fun label(text: String, size: Float) = TextView(this).apply { this.text = text; textSize = size; setPadding(0, dp(3), 0, dp(3)) }
    private fun button(text: String, action: () -> Unit) = Button(this).apply { this.text = text; setOnClickListener { action() } }
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}