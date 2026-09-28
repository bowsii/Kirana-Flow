package com.kiranaflow.ai.asr

import kotlinx.coroutines.flow.StateFlow

sealed interface SpeechEngineState {
    object Idle : SpeechEngineState
    object Listening : SpeechEngineState
    data class Recognised(val text: String) : SpeechEngineState
    data class Error(val message: String) : SpeechEngineState
}

interface SpeechEngine {
    val state: StateFlow<SpeechEngineState>
    fun startListening()
    fun stopListening()
    fun cancel()
    fun simulate(text: String)
}
