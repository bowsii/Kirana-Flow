package com.kiranaflow.app.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.kiranaflow.app.data.model.VoiceCommand
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * VoiceRecognitionService wraps Android's built-in SpeechRecognizer.
 *
 * ASR pipeline:
 *   Microphone → Android SpeechRecognizer (free, on-device) → raw text → NluEngine
 *
 * Note: Android's SpeechRecognizer supports offline recognition on supported
 * devices when offline language packs are downloaded. For Tamil / Tanglish,
 * it falls back to online Google ASR if the offline pack is unavailable.
 * Production will replace this with Whisper-Small INT8 via ONNX Runtime.
 */
@Singleton
class VoiceRecognitionService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val nluEngine: NluEngine
) {

    sealed class VoiceState {
        object Idle      : VoiceState()
        object Listening : VoiceState()
        data class Recognised(val text: String, val command: VoiceCommand) : VoiceState()
        data class Error(val message: String) : VoiceState()
    }

    private val _state = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val state: StateFlow<VoiceState> = _state.asStateFlow()

    private var recognizer: SpeechRecognizer? = null

    fun startListening() {
        stopListening()

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _state.value = VoiceState.Error("Speech recognition not available on this device")
            return
        }

        recognizer = SpeechRecognizer.createSpeechRecognizer(context).also { sr ->
            sr.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _state.value = VoiceState.Listening
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val best = matches?.firstOrNull() ?: ""
                    if (best.isNotBlank()) {
                        val cmd = nluEngine.parse(best)
                        _state.value = VoiceState.Recognised(text = best, command = cmd)
                    } else {
                        _state.value = VoiceState.Idle
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partial = partialResults
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull() ?: ""
                    // Surface partial results for live captions on screen
                    if (partial.isNotBlank()) {
                        _state.value = VoiceState.Recognised(
                            text    = partial,
                            command = nluEngine.parse(partial)
                        )
                    }
                }

                override fun onError(error: Int) {
                    val msg = when (error) {
                        SpeechRecognizer.ERROR_AUDIO             -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT            -> "Client error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permission denied"
                        SpeechRecognizer.ERROR_NETWORK           -> "Network error"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT   -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH          -> "No match — try again"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY   -> "Recognizer busy"
                        SpeechRecognizer.ERROR_SERVER            -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT    -> "No speech detected"
                        else                                     -> "Unknown error $error"
                    }
                    _state.value = VoiceState.Error(msg)
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ta-IN")         // Tamil India
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ta-IN")
                putExtra("android.speech.extra.EXTRA_ALSO_RECOGNIZE_LANGUAGE", "en-IN")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 500L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1000L)
            }
            sr.startListening(intent)
        }
    }

    fun stopListening() {
        recognizer?.destroy()
        recognizer = null
        _state.value = VoiceState.Idle
    }

    /**
     * Parse text directly (for testing / demo without microphone).
     */
    fun parseText(text: String) {
        val cmd = nluEngine.parse(text)
        _state.value = VoiceState.Recognised(text = text, command = cmd)
    }
}
