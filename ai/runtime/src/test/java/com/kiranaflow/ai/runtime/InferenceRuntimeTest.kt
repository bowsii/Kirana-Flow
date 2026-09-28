package com.kiranaflow.ai.runtime

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InferenceRuntimeTest {

    private lateinit var runtime: AndroidOnDeviceRuntime

    @Before
    fun setUp() {
        runtime = AndroidOnDeviceRuntime()
    }

    @Test
    fun testBackendPriorityOrder_QnnFirst_NnapiSecond_CpuThird() {
        val backends = listOf(ExecutionBackend.QNN, ExecutionBackend.NNAPI, ExecutionBackend.CPU)
        assertEquals(1, backends[0].priority)
        assertEquals(2, backends[1].priority)
        assertEquals(3, backends[2].priority)
        assertTrue(backends[0].priority < backends[1].priority)
        assertTrue(backends[1].priority < backends[2].priority)
    }

    @Test
    fun testLifecycle_loadFromAssetPack_warmUp_close() = runTest {
        assertFalse(runtime.isInitialized)
        assertFalse(runtime.isWarmedUp)
        assertNull(runtime.activeBackend)

        // Load model
        val loaded = runtime.loadFromAssetPack("models_pack", "gemma_3n_e2b.onnx")
        assertTrue("Model should load successfully with fallback", loaded)
        assertTrue(runtime.isInitialized)
        assertNotNull(runtime.activeBackend)

        // Warm up
        val warmed = runtime.warmUp()
        assertTrue("Warmup pass should complete successfully", warmed)
        assertTrue(runtime.isWarmedUp)

        // Close
        runtime.close()
        assertFalse("Runtime should be de-initialized after close", runtime.isInitialized)
        assertFalse("Runtime should not be warmed up after close", runtime.isWarmedUp)
        assertNull(runtime.activeBackend)
    }

    @Test
    fun testWarmUpFails_whenNotInitialized() = runTest {
        assertFalse(runtime.isInitialized)
        val warmed = runtime.warmUp()
        assertFalse("Warmup must return false if model is not loaded", warmed)
    }
}
