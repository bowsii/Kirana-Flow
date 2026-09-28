package com.kiranaflow.core.database

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages database encryption key security using hardware-backed Android KeyStore.
 *
 * Security Model:
 * 1. An AES-256 GCM master key is generated and securely held in Android Keystore ("AndroidKeyStore").
 * 2. A cryptographically random 32-byte passphrase is generated on first initialization.
 * 3. The passphrase is encrypted with AES/GCM/NoPadding (12-byte random IV) and stored in SharedPreferences.
 * 4. On subsequent boots, the ciphertext is retrieved from prefs and decrypted with the Keystore key.
 * 5. If decryption fails, or ciphertext is corrupted/tampered with, it throws [SecurityException] immediately.
 *    There is ZERO fallback to plaintext or hardcoded keys.
 *
 * Clean Install / Migration Note for Demo Devices:
 * Plaintext databases created prior to SQLCipher cannot be opened transparently.
 * On demo devices with unencrypted databases, either a clean install (wiping app storage / uninstall) is required,
 * or the database file must be migrated via SQLite `sqlcipher_export` before initialization.
 */
@Singleton
class DatabaseSecurityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val MASTER_KEY_ALIAS = "KiranaFlowMasterKey"
        private const val PREFS_NAME = "kirana_db_security_prefs"
        private const val KEY_ENCRYPTED_PASSPHRASE = "db_passphrase_ciphertext"
        private const val KEY_GCM_IV = "db_passphrase_iv"
        const val PASSPHRASE_BYTE_COUNT = 32
        private const val GCM_TAG_LENGTH_BITS = 128
        private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
    }

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Obtains the 32-byte passphrase for SQLCipher.
     * Decrypts the stored passphrase from prefs using the Android Keystore AES-GCM key,
     * or generates, encrypts, and persists a fresh random 32-byte key if it does not yet exist.
     *
     * Fails loudly with [SecurityException] if Keystore decryption or integrity validation fails.
     */
    @Synchronized
    fun getDatabasePassphrase(): ByteArray {
        val ciphertextB64 = prefs.getString(KEY_ENCRYPTED_PASSPHRASE, null)
        val ivB64 = prefs.getString(KEY_GCM_IV, null)

        val masterKey = getOrCreateMasterKey()

        if (ciphertextB64 != null && ivB64 != null) {
            try {
                val ciphertext = Base64.decode(ciphertextB64, Base64.NO_WRAP)
                val iv = Base64.decode(ivB64, Base64.NO_WRAP)
                val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
                val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
                cipher.init(Cipher.DECRYPT_MODE, masterKey, spec)
                val passphrase = cipher.doFinal(ciphertext)
                if (passphrase.size != PASSPHRASE_BYTE_COUNT) {
                    throw SecurityException("Corrupted passphrase length: expected $PASSPHRASE_BYTE_COUNT, got ${passphrase.size}")
                }
                return passphrase
            } catch (e: Exception) {
                throw SecurityException("Failed to decrypt database passphrase with Keystore key. Database access blocked.", e)
            }
        }

        // Generate a fresh random 32-byte passphrase
        val rawPassphrase = ByteArray(PASSPHRASE_BYTE_COUNT)
        SecureRandom().nextBytes(rawPassphrase)

        try {
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, masterKey)
            val iv = cipher.iv ?: throw SecurityException("Generated null IV for AES-GCM")
            val ciphertext = cipher.doFinal(rawPassphrase)

            val committed = prefs.edit()
                .putString(KEY_ENCRYPTED_PASSPHRASE, Base64.encodeToString(ciphertext, Base64.NO_WRAP))
                .putString(KEY_GCM_IV, Base64.encodeToString(iv, Base64.NO_WRAP))
                .commit()

            if (!committed) {
                throw SecurityException("Failed to commit encrypted passphrase to SharedPreferences.")
            }

            return rawPassphrase
        } catch (e: Exception) {
            throw SecurityException("Failed to generate and encrypt initial database passphrase.", e)
        }
    }

    private fun getOrCreateMasterKey(): SecretKey {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (!keyStore.containsAlias(MASTER_KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )
                val spec = KeyGenParameterSpec.Builder(
                    MASTER_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setRandomizedEncryptionRequired(true)
                    .build()

                keyGenerator.init(spec)
                keyGenerator.generateKey()
            }

            val entry = keyStore.getEntry(MASTER_KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
                ?: throw SecurityException("Keystore does not contain valid SecretKeyEntry for alias $MASTER_KEY_ALIAS")
            return entry.secretKey
        } catch (e: Exception) {
            throw SecurityException("Failed to retrieve or generate Android Keystore master key: ${e.message}", e)
        }
    }
}
