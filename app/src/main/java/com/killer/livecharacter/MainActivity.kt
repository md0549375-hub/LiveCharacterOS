package com.killer.livecharacter

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.switchmaterial.SwitchMaterial
import com.killer.livecharacter.engine.CharacterEngine
import com.killer.livecharacter.model.CharacterProfile
import com.killer.livecharacter.model.RightsStatus
import com.killer.livecharacter.overlay.CharacterOverlayService
import com.killer.livecharacter.perception.ScreenPerceptionController
import com.killer.livecharacter.rights.CharacterCatalog
import com.killer.livecharacter.rights.RightsGate
import com.killer.livecharacter.storage.SecureSettingsStore
import com.killer.livecharacter.ui.CharacterCanvasView

class MainActivity : AppCompatActivity() {
    private lateinit var settings: SecureSettingsStore
    private lateinit var host: FrameLayout
    private lateinit var overlaySwitch: SwitchMaterial
    private val catalog = CharacterCatalog.defaults()
    private var selectedId = "astra"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = SecureSettingsStore(this)
        setContentView(buildRoot())
        requestNotificationPermissionIfNeeded()
        showDashboard()
    }

    private fun buildRoot(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(8, 8, 15))
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(8))
        }
        header.addView(label("LIVE CHARACTER OS", 24f, Color.WHITE))
        header.addView(label("Rights-aware character runtime", 12f, Color.LTGRAY))

        host = FrameLayout(this).apply { setPadding(dp(14), dp(8), dp(14), 0) }

        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(8), dp(8), dp(12))
        }
        nav.addView(navButton("Ana Menü") { showDashboard() }, LinearLayout.LayoutParams(0, dp(52), 1f))
        nav.addView(navButton("Karakterler") { showCharacters() }, LinearLayout.LayoutParams(0, dp(52), 1f))
        nav.addView(navButton("Ayarlar") { showSettings() }, LinearLayout.LayoutParams(0, dp(52), 1f))

        root.addView(header)
        root.addView(host, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(nav)
        return root
    }

    private fun showDashboard() {
        val scroll = ScrollView(this)
        val box = column()
        val profile = selectedProfile()
        val decision = RightsGate.evaluate(profile)

        box.addView(section("Runtime"))
        box.addView(card().apply {
            addView(label(profile.name, 19f, Color.WHITE))
            addView(label("Hak durumu: " + statusLabel(profile.rights.status), 13f, statusColor(profile.rights.status)))
            addView(label(decision.message, 12f, Color.LTGRAY))
        })

        if (decision.allowed) {
            box.addView(CharacterCanvasView(this, CharacterEngine(profile)), LinearLayout.LayoutParams(-1, dp(330)).apply {
                setMargins(0, dp(10), 0, dp(10))
            })
            box.addView(actionButton("Ekran üstü karakteri başlat") { startOverlay() })
        } else {
            box.addView(blockedCard(profile, decision.message))
        }

        overlaySwitch = SwitchMaterial(this).apply {
            text = "Kalıcı ekran üstü karakter"
            setTextColor(Color.WHITE)
            isChecked = settings.isOverlayEnabled()
            setOnCheckedChangeListener { _, enabled ->
                settings.setOverlayEnabled(enabled)
                if (enabled) startOverlay() else stopService(Intent(this@MainActivity, CharacterOverlayService::class.java))
            }
        }
        box.addView(overlaySwitch)

        box.addView(section("Sistem"))
        box.addView(label("CharacterEngine • IK • secondary motion", 13f, Color.LTGRAY))
        box.addView(label("Overlay • foreground service + system overlay", 13f, Color.LTGRAY))
        box.addView(label("Rights Gate • Restricted/Unknown yükleme yok", 13f, Color.LTGRAY))
        box.addView(label("Secrets • Android Keystore AES/GCM", 13f, Color.LTGRAY))

        scroll.addView(box)
        host.removeAllViews()
        host.addView(scroll)
    }

    private fun showCharacters() {
        val scroll = ScrollView(this)
        val box = column()
        box.addView(section("Karakter Kütüphanesi"))
        box.addView(label("Restricted ve Unknown karakterler yüklenmez. Attribution tek başına izin değildir.", 12f, Color.LTGRAY))

        catalog.forEach { profile ->
            val decision = RightsGate.evaluate(profile)
            box.addView(card().apply {
                addView(label(profile.name, 18f, Color.WHITE))
                addView(label(statusLabel(profile.rights.status), 12f, statusColor(profile.rights.status)))
                val source = listOfNotNull(profile.rights.company, profile.rights.workTitle).joinToString(" • ")
                addView(label(if (source.isBlank()) "Kaynak bilgisi uygulama içi kayıtta." else source, 12f, Color.LTGRAY))
                addView(actionButton(if (decision.allowed) "Seç" else "Neden engellendi?") {
                    if (decision.allowed) {
                        selectedId = profile.id
                        Toast.makeText(this@MainActivity, profile.name + " seçildi.", Toast.LENGTH_SHORT).show()
                        showDashboard()
                    } else {
                        showRightsDialog(profile)
                    }
                })
            })
        }

        box.addView(section("Kaynak / Hak kayıtları"))
        catalog.forEach { profile ->
            box.addView(label(
                profile.name + " — " + statusLabel(profile.rights.status) + "
" +
                    profile.rights.sources.joinToString { it.name },
                12f, Color.LTGRAY
            ))
        }

        scroll.addView(box)
        host.removeAllViews()
        host.addView(scroll)
    }

    private fun showSettings() {
        val scroll = ScrollView(this)
        val box = column()

        box.addView(section("İzinler"))
        box.addView(actionButton("Android Etkileşim İznini Aç") {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        })
        box.addView(actionButton("Ekran Üstü Karakter İznini Aç") {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName)))
        })
        box.addView(actionButton("Screen Capture İznini Hazırla") {
            startActivityForResult(
                ScreenPerceptionController(this).createPermissionIntent(),
                ScreenPerceptionController.REQUEST_CAPTURE
            )
        })
        box.addView(label("MediaProjection yalnızca kullanıcı onayı sonrası kullanılabilir; ekran verisi varsayılan olarak kaydedilmez.", 12f, Color.LTGRAY))

        box.addView(section("AI Provider"))
        val api = EditText(this).apply {
            hint = "Provider API key"
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        box.addView(api)
        box.addView(actionButton("API key'i güvenli kaydet") {
            settings.putApiKey(api.text?.toString().orEmpty())
            api.text?.clear()
            Toast.makeText(this, "API key Android Keystore ile şifrelendi.", Toast.LENGTH_SHORT).show()
        })

        box.addView(section("Rights & Attribution"))
        catalog.forEach { profile ->
            val r = profile.rights
            box.addView(card().apply {
                addView(label(profile.name, 16f, Color.WHITE))
                addView(label("Durum: " + statusLabel(r.status), 12f, statusColor(r.status)))
                addView(label("Şirket: " + (r.company ?: "Belirtilmemiş"), 12f, Color.LTGRAY))
                addView(label("Eser: " + (r.workTitle ?: "Belirtilmemiş"), 12f, Color.LTGRAY))
                addView(label("Karakter: " + (r.characterName ?: profile.name), 12f, Color.LTGRAY))
                addView(label("Lisans: " + (r.licenseId ?: "Yok"), 12f, Color.LTGRAY))
                addView(label("Not: " + (r.permissionNote ?: r.sources.joinToString { it.note ?: it.name }), 12f, Color.LTGRAY))
            })
        }

        box.addView(section("Güvenlik"))
        box.addView(label("Rights Gate: Restricted/Unknown yükleme yok", 13f, Color.WHITE))
        box.addView(label("Attribution ≠ permission", 13f, Color.WHITE))
        box.addView(label("Overlay ve Accessibility kullanıcı iznine bağlı", 13f, Color.WHITE))

        scroll.addView(box)
        host.removeAllViews()
        host.addView(scroll)
    }

    private fun startOverlay() {
        val profile = selectedProfile()
        val decision = RightsGate.evaluate(profile)
        if (!decision.allowed) {
            showRightsDialog(profile)
            if (::overlaySwitch.isInitialized) overlaySwitch.isChecked = false
            return
        }
        if (!Settings.canDrawOverlays(this)) {
            if (::overlaySwitch.isInitialized) overlaySwitch.isChecked = false
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName)))
            Toast.makeText(this, "Önce ekran üstü iznini ver.", Toast.LENGTH_SHORT).show()
            return
        }
        ContextCompat.startForegroundService(this, CharacterOverlayService.intent(this, profile.id))
    }

    private fun showRightsDialog(profile: CharacterProfile) {
        val decision = RightsGate.evaluate(profile)
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(decision.title)
            .setMessage(
                profile.name + "

" + decision.message + "

Kaynaklar: " +
                    profile.rights.sources.joinToString { it.name }
            )
            .setPositiveButton("Tamam", null)
            .show()
    }

    private fun blockedCard(profile: CharacterProfile, message: String): View = card().apply {
        addView(label("⚠ " + profile.name + " yüklenemedi", 18f, Color.WHITE))
        addView(label(message, 13f, Color.LTGRAY))
        addView(label("Hak durumu doğrulanmadan model/karakter runtime'a alınmaz.", 12f, Color.LTGRAY))
    }

    private fun selectedProfile(): CharacterProfile =
        catalog.firstOrNull { it.id == selectedId } ?: catalog.first()

    private fun column() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(0, 0, 0, dp(12))
    }

    private fun card() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(14), dp(14), dp(14), dp(14))
        background = GradientDrawable().apply {
            cornerRadius = dp(18).toFloat()
            setColor(Color.rgb(21, 21, 34))
        }
        layoutParams = LinearLayout.LayoutParams(-1, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            setMargins(0, dp(7), 0, dp(7))
        }
    }

    private fun section(text: String) = label(text, 16f, Color.WHITE).apply {
        setPadding(0, dp(14), 0, dp(6))
    }

    private fun navButton(text: String, action: () -> Unit) = Button(this).apply {
        this.text = text
        setOnClickListener { action() }
        textSize = 11f
    }

    private fun actionButton(text: String, action: () -> Unit) = Button(this).apply {
        this.text = text
        setOnClickListener { action() }
        isAllCaps = false
    }

    private fun label(text: String, size: Float, color: Int) = TextView(this).apply {
        this.text = text
        textSize = size
        setTextColor(color)
        setPadding(0, dp(3), 0, dp(3))
    }

    private fun statusLabel(status: RightsStatus): String = when (status) {
        RightsStatus.ORIGINAL -> "ORIGINAL"
        RightsStatus.LICENSED -> "LICENSED"
        RightsStatus.USER_OWNED_LICENSE -> "USER OWNED LICENSE"
        RightsStatus.PERMITTED_USE -> "PERMITTED USE"
        RightsStatus.RESTRICTED -> "RESTRICTED"
        RightsStatus.UNKNOWN -> "UNKNOWN"
    }

    private fun statusColor(status: RightsStatus): Int = when (status) {
        RightsStatus.ORIGINAL, RightsStatus.LICENSED, RightsStatus.USER_OWNED_LICENSE, RightsStatus.PERMITTED_USE -> Color.rgb(93, 230, 180)
        RightsStatus.RESTRICTED -> Color.rgb(255, 88, 110)
        RightsStatus.UNKNOWN -> Color.rgb(255, 190, 82)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 8101)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
