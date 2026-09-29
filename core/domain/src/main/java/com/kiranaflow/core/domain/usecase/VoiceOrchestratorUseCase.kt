package com.kiranaflow.core.domain.usecase

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

sealed class VoiceOrchestratorState {
    object Idle : VoiceOrchestratorState()
    object Listening : VoiceOrchestratorState()
    data class Recognised(
        val text: String,
        val command: VoiceCommand,
        val confidence: Float
    ) : VoiceOrchestratorState()
    data class Error(val message: String) : VoiceOrchestratorState()
}

@Singleton
class VoiceOrchestratorUseCase @Inject constructor(
    private val speechEngine: SpeechEngine,
    private val intentParser: IntentParser
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow<VoiceOrchestratorState>(VoiceOrchestratorState.Idle)
    val state: StateFlow<VoiceOrchestratorState> = _state.asStateFlow()

    init {
        scope.launch {
            speechEngine.state.collect { engineState ->
                when (engineState) {
                    is SpeechEngineState.Idle -> _state.value = VoiceOrchestratorState.Idle
                    is SpeechEngineState.Listening -> _state.value = VoiceOrchestratorState.Listening
                    is SpeechEngineState.Recognised -> {
                        val parsedCommands = intentParser.parse(engineState.text)
                        val top = parsedCommands.maxByOrNull { it.confidence }
                        val topCmd = top?.command
                            ?: VoiceCommand(com.kiranaflow.core.model.CommandIntent.UNKNOWN, rawText = engineState.text)
                        val confidence = top?.confidence ?: 0.0f
                        _state.value = VoiceOrchestratorState.Recognised(
                            text = engineState.text,
                            command = topCmd,
                            confidence = confidence
                        )
                    }
                    is SpeechEngineState.Error -> _state.value = VoiceOrchestratorState.Error(engineState.message)
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
            val parsedCommands = intentParser.parse(text)
            val top = parsedCommands.maxByOrNull { it.confidence }
            val topCmd = top?.command
                ?: VoiceCommand(com.kiranaflow.core.model.CommandIntent.UNKNOWN, rawText = text)
            val confidence = top?.confidence ?: 0.0f
            _state.value = VoiceOrchestratorState.Recognised(
                text = text,
                command = topCmd,
                confidence = confidence
            )
        }
    }
}
