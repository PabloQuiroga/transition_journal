package com.refugio.journal.core.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.refugio.journal.domain.repository.SecurityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidSecurityManager @Inject constructor(
    @ApplicationContext private val context: Context
) : SecurityManager {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val PREFS_NAME = "refugio_security_prefs"
        private const val KEY_PIN_HASH = "key_pin_hash"
        private const val KEY_PIN_SALT = "key_pin_salt"
    }

    override suspend fun isPinConfigured(): Boolean {
        return prefs.contains(KEY_PIN_HASH) && prefs.contains(KEY_PIN_SALT)
    }

    override suspend fun setupPin(rawPin: String): Result<Unit> {
        return runCatching {
            val salt = ByteArray(16)
            SecureRandom().nextBytes(salt)
            val saltHex = salt.toHexString()

            val hash = hashPin(rawPin, saltHex)

            prefs.edit()
                .putString(KEY_PIN_HASH, hash)
                .putString(KEY_PIN_SALT, saltHex)
                .apply()
        }
    }

    override suspend fun validatePin(inputPin: String): Boolean {
        if (!isPinConfigured()) return false
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        val storedSalt = prefs.getString(KEY_PIN_SALT, null) ?: return false

        val inputHash = hashPin(inputPin, storedSalt)
        return inputHash == storedHash
    }

    override fun isBiometricAvailable(): Boolean {
        return false
    }

    private fun hashPin(pin: String, saltHex: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(saltHex.toByteArray(Charsets.UTF_8))
        val hashedBytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return hashedBytes.toHexString()
    }

    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02x".format(it) }
    }
}
