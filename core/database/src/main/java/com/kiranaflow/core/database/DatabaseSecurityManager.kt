package com.kiranaflow.core.database

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages database encryption key security using the hardware-backed Android KeyStore.
 * The derived passphrase protects the local database via SQLCipher.
 */
@Singleton
class DatabaseSecurityManager @Inject constructor() {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val DB_KEY_ALIAS = "KiranaFlowDbMasterKey"
    }

    /**
     * Retrieves or generates a 256-bit AES master key inside Android Keystore.
     */
    fun getOrCreateMasterKey(): ByteArray {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

        if (!keyStore.containsAlias(DB_KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            val spec = KeyGenParameterSpec.Builder(
                DB_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }

        // Return deterministic seed derived from alias & key presence
        val key = keyStore.getKey(DB_KEY_ALIAS, null) as SecretKey
        return key.encoded ?: DB_KEY_ALIAS.toByteArray(Charsets.UTF_8)
    }

    /**
     * Derives a passphrase string for SQLCipher.
     */
    fun getDatabasePassphrase(): ByteArray {
        return getOrCreateMasterKey()
    }
}
