package com.kiranaflow.feature.billing

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class VoiceStateMonitorTest {

    private lateinit var context: Context
    private lateinit var monitor: VoiceStateMonitor

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        monitor = VoiceStateMonitor(context)
    }

    @Test
    fun testInitialStatus_queriesUnderlyingAndroidCapabilities() {
        val status = monitor.queryStatus(isListening = false)
        assertNotNull(status)
        assertFalse("Initial listening state should be false", status.isListening)
    }

    @Test
    fun testSetListening_updatesFlowState() = runTest {
        assertFalse(monitor.status.value.isListening)

        monitor.setListening(true)
        assertTrue(monitor.status.value.isListening)

        monitor.setListening(false)
        assertFalse(monitor.status.value.isListening)
    }

    @Test
    fun testSystemVoiceStatus_evaluatesReadiness() {
        val readyStatus = SystemVoiceStatus(
            hasMicPermission = true,
            isRecognizerAvailable = true,
            isOfflineTamilPackInstalled = true,
            isListening = false
        )
        assertTrue(readyStatus.isReady)

        val unpermittedStatus = SystemVoiceStatus(
            hasMicPermission = false,
            isRecognizerAvailable = true,
            isOfflineTamilPackInstalled = true,
            isListening = false
        )
        assertFalse(unpermittedStatus.isReady)
    }
}
