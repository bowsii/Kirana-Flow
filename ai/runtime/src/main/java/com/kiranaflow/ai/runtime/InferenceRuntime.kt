package com.kiranaflow.ai.runtime

/**
 * Common abstraction for AI execution providers (CPU, NNAPI, QNN Hexagon NPU).
 */
interface InferenceRuntime {
    val isAvailable: Boolean
    val providerName: String
    suspend fun initialize(): Boolean
    suspend fun close()
}
