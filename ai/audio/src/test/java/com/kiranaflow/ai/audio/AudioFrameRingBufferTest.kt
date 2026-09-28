package com.kiranaflow.ai.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioFrameRingBufferTest {

    @Test
    fun testAudioFrame_durationAndRmsCalculation() {
        val samples = ShortArray(1600) // 100 ms at 16 kHz
        val frame = AudioFrame(data = samples, sampleRate = 16000)

        assertEquals(100L, frame.durationMs)
        assertEquals(0.0, frame.computeRms(), 0.0001)

        // Fill with full amplitude square wave
        for (i in samples.indices) {
            samples[i] = if (i % 2 == 0) 16384 else -16384
        }
        val activeFrame = AudioFrame(data = samples, sampleRate = 16000)
        assertEquals(0.5, activeFrame.computeRms(), 0.01)
    }

    @Test
    fun testAudioRingBuffer_pushAndEviction() {
        val ring = AudioRingBuffer(capacity = 3)
        assertEquals(0, ring.size())
        assertFalse(ring.isFull())

        val frame1 = AudioFrame(shortArrayOf(1))
        val frame2 = AudioFrame(shortArrayOf(2))
        val frame3 = AudioFrame(shortArrayOf(3))
        val frame4 = AudioFrame(shortArrayOf(4))

        ring.push(frame1)
        ring.push(frame2)
        assertEquals(2, ring.size())
        assertEquals(listOf(frame1, frame2), ring.toList())

        ring.push(frame3)
        assertEquals(3, ring.size())
        assertTrue(ring.isFull())
        assertEquals(listOf(frame1, frame2, frame3), ring.toList())

        // Pushing 4th evicts 1st
        ring.push(frame4)
        assertEquals(3, ring.size())
        assertEquals(listOf(frame2, frame3, frame4), ring.toList())

        ring.clear()
        assertEquals(0, ring.size())
        assertTrue(ring.toList().isEmpty())
    }

    @Test
    fun testVadHook_differentiatesSilenceFromSpeech() {
        val vad = EnergyVadHook(energyThreshold = 0.02)

        val silenceData = ShortArray(640) { 0 }
        val silenceFrame = AudioFrame(silenceData)
        assertFalse("Silence frame should not be classified as speech", vad.isSpeech(silenceFrame))

        val speechData = ShortArray(640) { 5000 }
        val speechFrame = AudioFrame(speechData)
        assertTrue("High energy frame should be classified as speech", vad.isSpeech(speechFrame))
    }
}
