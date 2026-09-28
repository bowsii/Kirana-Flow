package com.kiranaflow.app.service

import com.kiranaflow.ai.asr.SpeechEngine
import com.kiranaflow.ai.asr.SpeechEngineState
import com.kiranaflow.ai.nlu.IntentParser
import com.kiranaflow.core.model.VoiceCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * VoiceRecognitionService wraps the underlying [SpeechEngine] and [IntentParser]
 * providing reactive StateFlows for UI and ViewModels.
 */
@Singleton
class VoiceRecognitionService @Inject constructor(
    private val speechEngine: SpeechEngine,
    private val intentParser: IntentParser
) {

    sealed class VoiceState {
        object Idle : VoiceState()
        object Listening : VoiceState()
        data class Recognised(val text: String, val command: VoiceCommand) : VoiceState()
        data class Error(val message: String) : VoiceState()
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val state: StateFlow<VoiceState> = _state.asStateFlow()

    init {
        scope.launch {
            speechEngine.state.collect { engineState ->
                when (engineState) {
                    is SpeechEngineState.Idle -> _state.value = VoiceState.Idle
                    is SpeechEngineState.Listening -> _state.value = VoiceState.Listening
                    is SpeechEngineState.Recognised -> {
                        val cmd = intentParser.parse(engineState.text)
                        _state.value = VoiceState.Recognised(text = engineState.text, command = cmd)
                    }
                    is SpeechEngineState.Error -> _state.value = VoiceState.Error(engineState.message)
                }
            }
        }
    }

    fun startListening() {
        speechEngine.startListening()
    }

    fun stopListening() {
        speechEngine.stopListening()
    }

    fun parseText(text: String) {
        scope.launch {
            val cmd = intentParser.parse(text)
            _state.value = VoiceState.Recognised(text = text, command = cmd)
        }
    }
}
