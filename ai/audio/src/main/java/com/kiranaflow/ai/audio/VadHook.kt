package com.kiranaflow.ai.audio

/**
 * Hook interface for Voice Activity Detection (VAD).
 * Determines whether an [AudioFrame] contains active human speech or silence/ambient noise.
 */
interface VadHook {
    fun isSpeech(frame: AudioFrame): Boolean
}

/**
 * Energy-based Root Mean Square (RMS) Voice Activity Detector.
 * Compares normalized frame energy against a calibrated threshold.
 */
class EnergyVadHook(
    val energyThreshold: Double = 0.015
) : VadHook {
    override fun isSpeech(frame: AudioFrame): Boolean {
        return frame.computeRms() >= energyThreshold
    }
}
