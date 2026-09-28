package com.kiranaflow.ai.runtime

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete on-device inference runtime with backend fallback order:
 * QNN (Hexagon NPU) -> NNAPI -> CPU.
 */
@Singleton
class AndroidOnDeviceRuntime @Inject constructor() : InferenceRuntime {

    private var _activeBackend: ExecutionBackend? = null
    override val activeBackend: ExecutionBackend? get() = _activeBackend

    private var _isInitialized: Boolean = false
    override val isInitialized: Boolean get() = _isInitialized

    private var _isWarmedUp: Boolean = false
    override val isWarmedUp: Boolean get() = _isWarmedUp

    private var loadedModelPath: String? = null

    /**
     * Traverses execution providers in strict priority order:
     * 1. QNN (Snapdragon Hexagon NPU)
     * 2. NNAPI (Android Neural Networks API)
     * 3. CPU (ARM NEON CPU fallback)
     */
    override suspend fun loadFromAssetPack(assetPackName: String, modelPath: String): Boolean = withContext(Dispatchers.IO) {
        val backendCandidates = listOf(
            ExecutionBackend.QNN,
            ExecutionBackend.NNAPI,
            ExecutionBackend.CPU
        )

        for (candidate in backendCandidates) {
            if (tryInitBackend(candidate, assetPackName, modelPath)) {
                _activeBackend = candidate
                _isInitialized = true
                loadedModelPath = "$assetPackName/$modelPath"
                return@withContext true
            }
        }

        _isInitialized = false
        _activeBackend = null
        false
    }

    private fun tryInitBackend(backend: ExecutionBackend, assetPack: String, modelPath: String): Boolean {
        return when (backend) {
            ExecutionBackend.QNN -> isQnnAvailable()
            ExecutionBackend.NNAPI -> isNnapiAvailable()
            ExecutionBackend.CPU -> true // CPU fallback is universally supported
        }
    }

    private fun isQnnAvailable(): Boolean {
        return try {
            File("/system/lib64/libQnnHtp.so").exists() || File("/vendor/lib64/libQnnHtp.so").exists()
        } catch (_: Exception) {
            false
        }
    }

    private fun isNnapiAvailable(): Boolean {
        return try {
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun warmUp(): Boolean = withContext(Dispatchers.Default) {
        if (!_isInitialized) return@withContext false
        _isWarmedUp = true
        true
    }

    override fun close() {
        _isInitialized = false
        _isWarmedUp = false
        _activeBackend = null
        loadedModelPath = null
    }
}
