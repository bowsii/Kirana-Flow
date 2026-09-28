package com.kiranaflow.ai.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Audio capture pipeline delivering continuous 16 kHz Mono 16-bit PCM [AudioFrame] streams.
 */
@Singleton
class AudioRecordSource @Inject constructor() {

    companion object {
        const val SAMPLE_RATE_HZ = 16000
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        const val FRAME_SIZE_SAMPLES = 640 // 40 ms chunks at 16 kHz
    }

    /**
     * Emits a reactive stream of [AudioFrame] chunks captured from the microphone.
     * Can optionally apply a [VadHook] for real-time speech activity tagging or filtering.
     */
    @SuppressLint("MissingPermission")
    fun startCapture(vadHook: VadHook? = null): Flow<AudioFrame> = flow {
        val minBufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE_HZ,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
        ).coerceAtLeast(FRAME_SIZE_SAMPLES * 2)

        var record: AudioRecord? = null
        try {
            record = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE_HZ,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                minBufferSize
            )

            if (record.state != AudioRecord.STATE_INITIALIZED) {
                return@flow
            }

            record.startRecording()
            val buffer = ShortArray(FRAME_SIZE_SAMPLES)

            while (currentCoroutineContext().isActive) {
                val readCount = record.read(buffer, 0, FRAME_SIZE_SAMPLES)
                if (readCount > 0) {
                    val frameData = buffer.copyOf(readCount)
                    val frame = AudioFrame(
                        data = frameData,
                        sampleRate = SAMPLE_RATE_HZ,
                        timestampMs = System.currentTimeMillis()
                    )
                    emit(frame)
                }
            }
        } finally {
            try {
                record?.stop()
                record?.release()
            } catch (_: Exception) {}
        }
    }
}
