package com.killer.livecharacter.storage

import android.content.Context
import android.util.Base64
import java.nio.ByteBuffer
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecureSettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("live_character_settings", Context.MODE_PRIVATE)
    private val alias = "live_character_os_aes"
    fun putApiKey(value: String) {
        if (value.isBlank()) { prefs.edit().remove("api_key").apply(); return }
        val encrypted = encrypt(value.toByteArray(Charsets.UTF_8))
        prefs.edit().putString("api_key", Base64.encodeToString(encrypted, Base64.NO_WRAP)).apply()
    }
    fun getApiKey(): String? {
        val encoded = prefs.getString("api_key", null) ?: return null
        return runCatching { String(decrypt(Base64.decode(encoded, Base64.NO_WRAP)), Charsets.UTF_8) }.getOrNull()
    }
    fun setOverlayEnabled(enabled: Boolean) = prefs.edit().putBoolean("overlay_enabled", enabled).apply()
    fun isOverlayEnabled(): Boolean = prefs.getBoolean("overlay_enabled", false)
    private fun key(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(alias, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance("AES", "AndroidKeyStore")
        generator.init(android.security.keystore.KeyGenParameterSpec.Builder(alias, android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or android.security.keystore.KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE).build())
        return generator.generateKey()
    }
    private fun encrypt(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.ENCRYPT_MODE, key())
        val iv = cipher.iv; val ciphertext = cipher.doFinal(data)
        return ByteBuffer.allocate(4 + iv.size + ciphertext.size).putInt(iv.size).put(iv).put(ciphertext).array()
    }
    private fun decrypt(data: ByteArray): ByteArray {
        val buffer = ByteBuffer.wrap(data); val ivSize = buffer.int; require(ivSize in 12..32)
        val iv = ByteArray(ivSize); buffer.get(iv); val ciphertext = ByteArray(buffer.remaining()); buffer.get(ciphertext)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv)); return cipher.doFinal(ciphertext)
    }
}