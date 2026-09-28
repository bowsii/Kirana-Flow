package com.kiranaflow.ai.asr

import com.kiranaflow.ai.audio.AudioFrame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Result of speech recognition containing transcribed text, finality status, and confidence score.
 */
data class Transcript(
    val text: String,
    val isFinal: Boolean = true,
    val confidence: Float = 1.0f
)

sealed interface SpeechEngineState {
    object Idle : SpeechEngineState
    object Listening : SpeechEngineState
    data class Recognised(val text: String) : SpeechEngineState
    data class Error(val message: String) : SpeechEngineState
}

/**
 * Speech Recognition Engine contract consuming reactive [AudioFrame] streams
 * and emitting real-time [Transcript] tokens.
 */
interface SpeechEngine {
    val state: StateFlow<SpeechEngineState>

    /**
     * Consumes raw 16 kHz Mono AudioFrames and emits recognized transcripts.
     */
    fun processAudio(audioStream: Flow<AudioFrame>): Flow<Transcript>

    fun startListening()
    fun stopListening()
    fun cancel()
    fun simulate(text: String)
}
