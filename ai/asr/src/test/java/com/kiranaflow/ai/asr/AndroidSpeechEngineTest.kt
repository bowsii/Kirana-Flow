package com.kiranaflow.ai.asr

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.kiranaflow.ai.audio.AudioFrame
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AndroidSpeechEngineTest {

    private lateinit var context: Context
    private lateinit var speechEngine: AndroidSpeechEngine

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        speechEngine = AndroidSpeechEngine(context)
    }

    @Test
    fun testSimulate_updatesEngineStateAndEmitsTranscript() {
        speechEngine.simulate("oru parle g")

        val state = speechEngine.state.value
        assertTrue("State should be Recognised", state is SpeechEngineState.Recognised)
        assertEquals("oru parle g", (state as SpeechEngineState.Recognised).text)
    }

    @Test
    fun testProcessAudio_consumesAudioFramesAndEmitsTranscripts() = runTest {
        speechEngine.simulate("tata salt 1kg")

        val audioStream = flowOf(
            AudioFrame(ShortArray(640)),
            AudioFrame(ShortArray(640))
        )

        val results = speechEngine.processAudio(audioStream).take(1).toList()

        assertEquals(1, results.size)
        assertEquals("tata salt 1kg", results[0].text)
        assertTrue(results[0].isFinal)
        assertEquals(1.0f, results[0].confidence, 0.001f)
    }
}
