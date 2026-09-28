package com.kiranaflow.ai.audio

/**
 * Thread-safe circular buffer for [AudioFrame] objects.
 * Supports zero-allocation lookback windows, sliding history, and pre-speech VAD buffering.
 */
class AudioRingBuffer(val capacity: Int) {
    init {
        require(capacity > 0) { "Capacity must be greater than zero, got $capacity" }
    }

    private val buffer = arrayOfNulls<AudioFrame>(capacity)
    private var head = 0
    private var size = 0
    private val lock = Any()

    fun push(frame: AudioFrame) = synchronized(lock) {
        buffer[head] = frame
        head = (head + 1) % capacity
        if (size < capacity) {
            size++
        }
    }

    fun toList(): List<AudioFrame> = synchronized(lock) {
        val result = ArrayList<AudioFrame>(size)
        val start = if (size < capacity) 0 else head
        for (i in 0 until size) {
            val idx = (start + i) % capacity
            buffer[idx]?.let { result.add(it) }
        }
        result
    }

    fun clear() = synchronized(lock) {
        for (i in 0 until capacity) {
            buffer[i] = null
        }
        head = 0
        size = 0
    }

    fun size(): Int = synchronized(lock) { size }
    fun isFull(): Boolean = synchronized(lock) { size == capacity }
}
