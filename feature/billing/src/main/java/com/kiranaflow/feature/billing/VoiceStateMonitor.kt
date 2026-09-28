package com.kiranaflow.feature.billing

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Real system state for speech and voice recognition hardware/software capabilities.
 */
data class SystemVoiceStatus(
    val hasMicPermission: Boolean = false,
    val isRecognizerAvailable: Boolean = false,
    val isOfflineTamilPackInstalled: Boolean = false,
    val isListening: Boolean = false
) {
    val isReady: Boolean get() = hasMicPermission && isRecognizerAvailable
}

/**
 * Monitors and queries the actual underlying Android runtime capabilities for:
 * 1. RECORD_AUDIO microphone permission
 * 2. SpeechRecognizer availability on device
 * 3. Offline Tamil on-device recognition model pack
 * 4. Real-time active listening state
 */
@Singleton
class VoiceStateMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _status = MutableStateFlow(queryStatus())
    val status: StateFlow<SystemVoiceStatus> = _status.asStateFlow()

    fun refreshStatus(isListening: Boolean = _status.value.isListening) {
        _status.value = queryStatus(isListening)
    }

    fun setListening(listening: Boolean) {
        _status.value = _status.value.copy(isListening = listening)
    }

    fun queryStatus(isListening: Boolean = false): SystemVoiceStatus {
        val hasMic = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        val isRecognizerAvail = try {
            SpeechRecognizer.isRecognitionAvailable(context)
        } catch (_: Exception) {
            false
        }

        val isTamilPackInstalled = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
            } else {
                isRecognizerAvail
            }
        } catch (_: Exception) {
            false
        }

        return SystemVoiceStatus(
            hasMicPermission = hasMic,
            isRecognizerAvailable = isRecognizerAvail,
            isOfflineTamilPackInstalled = isTamilPackInstalled,
            isListening = isListening
        )
    }
}
