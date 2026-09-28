package com.kiranaflow.ai.audio

/**
 * A discrete chunk of raw audio samples captured from the microphone.
 * Standard format: 16 kHz, 16-bit PCM, single channel (mono).
 */
data class AudioFrame(
    val data: ShortArray,
    val sampleRate: Int = 16000,
    val timestampMs: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AudioFrame
        return data.contentEquals(other.data) && sampleRate == other.sampleRate && timestampMs == other.timestampMs
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + sampleRate
        result = 31 * result + timestampMs.hashCode()
        return result
    }

    val durationMs: Long
        get() = (data.size.toDouble() / sampleRate * 1000).toLong()

    /**
     * Computes the Root Mean Square (RMS) energy of the frame.
     */
    fun computeRms(): Double {
        if (data.isEmpty()) return 0.0
        var sumSquares = 0.0
        for (sample in data) {
            val normalized = sample / 32768.0
            sumSquares += normalized * normalized
        }
        return Math.sqrt(sumSquares / data.size)
    }
}
