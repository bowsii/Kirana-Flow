package com.kiranaflow.ai.runtime

/**
 * Execution backends for on-device AI inference ordered by hardware priority:
 * 1. QNN (Qualcomm Hexagon NPU on Snapdragon platforms)
 * 2. NNAPI (Android Neural Networks API driver)
 * 3. CPU (ARM NEON optimized CPU execution provider)
 */
enum class ExecutionBackend(val priority: Int, val displayName: String) {
    QNN(1, "QNN (Hexagon NPU)"),
    NNAPI(2, "NNAPI"),
    CPU(3, "ARM NEON CPU")
}

/**
 * Common abstraction for loading, initializing, warming up, and executing on-device AI models.
 * Enforces fallback order: QNN -> NNAPI -> CPU.
 */
interface InferenceRuntime : AutoCloseable {
    val activeBackend: ExecutionBackend?
    val isInitialized: Boolean
    val isWarmedUp: Boolean

    /**
     * Loads model weights from the specified Android Dynamic Asset Pack or local asset path.
     * Evaluates backends in strict fallback order: QNN -> NNAPI -> CPU.
     */
    suspend fun loadFromAssetPack(assetPackName: String, modelPath: String): Boolean

    /**
     * Pre-allocates execution buffers and runs a warm-up inference pass to eliminate first-token latency.
     */
    suspend fun warmUp(): Boolean

    /**
     * Releases runtime native memory, NPU/NNAPI/CPU execution provider sessions, and unloaded weights.
     */
    override fun close()
}
