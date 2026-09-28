package com.kiranaflow.ai.asr

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.kiranaflow.ai.audio.AudioFrame
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidSpeechEngine @Inject constructor(
    @ApplicationContext private val context: Context
) : SpeechEngine {

    private val _state = MutableStateFlow<SpeechEngineState>(SpeechEngineState.Idle)
    override val state: StateFlow<SpeechEngineState> = _state.asStateFlow()

    private val _transcripts = MutableSharedFlow<Transcript>(replay = 1, extraBufferCapacity = 32)

    private var recognizer: SpeechRecognizer? = null

    private fun getOrCreateRecognizer(): SpeechRecognizer {
        return recognizer ?: SpeechRecognizer.createSpeechRecognizer(context).also { sr ->
            sr.setRecognitionListener(createListener())
            recognizer = sr
        }
    }

    private fun createListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _state.value = SpeechEngineState.Listening
        }
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onError(error: Int) {
            val msg = when (error) {
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                else -> "Recognition error ($error)"
            }
            _state.value = SpeechEngineState.Error(msg)
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val topResult = matches?.firstOrNull()?.trim()
            if (!topResult.isNullOrBlank()) {
                val transcript = Transcript(text = topResult, isFinal = true, confidence = 0.95f)
                _state.value = SpeechEngineState.Recognised(topResult)
                _transcripts.tryEmit(transcript)
            } else {
                _state.value = SpeechEngineState.Error("No text recognized")
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
            if (!partial.isNullOrBlank()) {
                _transcripts.tryEmit(Transcript(text = partial, isFinal = false, confidence = 0.70f))
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    /**
     * Consumes incoming [AudioFrame] stream and emits transcribed tokens.
     */
    override fun processAudio(audioStream: Flow<AudioFrame>): Flow<Transcript> = channelFlow {
        startListening()
        val audioIngestJob = launch {
            audioStream.collect {
                // Audio frames collected for ASR pipeline / VAD gating
            }
        }
        val transcriptJob = launch {
            _transcripts.collect { transcript ->
                send(transcript)
            }
        }
        awaitClose {
            audioIngestJob.cancel()
            transcriptJob.cancel()
            stopListening()
        }
    }

    override fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ta-IN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ta-IN")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }
        try {
            getOrCreateRecognizer().startListening(intent)
        } catch (e: Exception) {
            _state.value = SpeechEngineState.Error(e.message ?: "Failed to start recognition")
        }
    }

    override fun stopListening() {
        try {
            recognizer?.stopListening()
        } catch (_: Exception) {}
        _state.value = SpeechEngineState.Idle
    }

    override fun cancel() {
        try {
            recognizer?.cancel()
        } catch (_: Exception) {}
        _state.value = SpeechEngineState.Idle
    }

    override fun simulate(text: String) {
        val trimmed = text.trim()
        val transcript = Transcript(text = trimmed, isFinal = true, confidence = 1.0f)
        _state.value = SpeechEngineState.Recognised(trimmed)
        _transcripts.tryEmit(transcript)
    }
}
