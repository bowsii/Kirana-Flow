package com.kiranaflow.core.domain.usecase

import com.kiranaflow.ai.audio.AudioFrame
import com.kiranaflow.ai.asr.SpeechEngine
import com.kiranaflow.ai.asr.SpeechEngineState
import com.kiranaflow.ai.asr.Transcript
import com.kiranaflow.ai.nlu.IntentParser
import com.kiranaflow.ai.nlu.ParsedCommand
import com.kiranaflow.core.model.CommandIntent
import com.kiranaflow.core.model.VoiceCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VoiceOrchestratorUseCaseTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeSpeechEngine : SpeechEngine {
        private val _state = MutableStateFlow<SpeechEngineState>(SpeechEngineState.Idle)
        override val state: StateFlow<SpeechEngineState> = _state.asStateFlow()

        private val _transcripts = MutableSharedFlow<Transcript>(replay = 1)

        var isListeningStarted = false
        var isListeningStopped = false

        fun emitState(s: SpeechEngineState) {
            _state.value = s
        }

        override fun startListening() {
            isListeningStarted = true
            _state.value = SpeechEngineState.Listening
        }

        override fun stopListening() {
            isListeningStopped = true
            _state.value = SpeechEngineState.Idle
        }

        override fun cancel() {
            _state.value = SpeechEngineState.Idle
        }

        override fun simulate(text: String) {
            _state.value = SpeechEngineState.Recognised(text)
        }

        override fun processAudio(audioStream: Flow<AudioFrame>): Flow<Transcript> = _transcripts.asSharedFlow()
    }

    private class FakeIntentParser : IntentParser {
        override suspend fun parse(text: String): List<ParsedCommand> {
            return when {
                text.contains("bill", ignoreCase = true) -> listOf(
                    ParsedCommand(VoiceCommand(intent = CommandIntent.COMMIT, rawText = text), 0.95f)
                )
                text.contains("sugar", ignoreCase = true) -> listOf(
                    ParsedCommand(VoiceCommand(intent = CommandIntent.ADD, item = "sugar", rawText = text), 0.90f)
                )
                else -> listOf(
                    ParsedCommand(VoiceCommand(intent = CommandIntent.UNKNOWN, rawText = text), 0.30f)
                )
            }
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testVoiceOrchestration_stateTransitionsAndCommandParsing() = runTest {
        val fakeEngine = FakeSpeechEngine()
        val fakeParser = FakeIntentParser()
        val useCase = VoiceOrchestratorUseCase(fakeEngine, fakeParser)

        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(useCase.state.value is VoiceOrchestratorState.Idle)

        useCase.startListening()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(useCase.state.value is VoiceOrchestratorState.Listening)
        assertTrue(fakeEngine.isListeningStarted)

        fakeEngine.emitState(SpeechEngineState.Recognised("sugar 1 kg"))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = useCase.state.value
        assertTrue(state is VoiceOrchestratorState.Recognised)
        val recognised = state as VoiceOrchestratorState.Recognised
        assertEquals("sugar 1 kg", recognised.text)
        assertEquals(CommandIntent.ADD, recognised.command.intent)
        assertEquals("sugar", recognised.command.item)
        assertEquals(0.90f, recognised.confidence, 0.01f)

        useCase.stopListening()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(fakeEngine.isListeningStopped)
        assertTrue(useCase.state.value is VoiceOrchestratorState.Idle)
    }

    @Test
    fun testSimulateParseText() = runTest {
        val fakeEngine = FakeSpeechEngine()
        val fakeParser = FakeIntentParser()
        val useCase = VoiceOrchestratorUseCase(fakeEngine, fakeParser)

        useCase.parseText("bill potru")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = useCase.state.value
        assertTrue(state is VoiceOrchestratorState.Recognised)
        val recognised = state as VoiceOrchestratorState.Recognised
        assertEquals(CommandIntent.COMMIT, recognised.command.intent)
        assertEquals(0.95f, recognised.confidence, 0.01f)
    }
}
