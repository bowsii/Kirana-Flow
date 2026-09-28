package com.kiranaflow.ai.nlu

import com.kiranaflow.core.model.CommandIntent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RuleBasedIntentParserTest {

    private lateinit var parser: RuleBasedIntentParser

    @Before
    fun setUp() {
        parser = RuleBasedIntentParser()
    }

    @Test
    fun testTamilNumberAddIntent() = runTest {
        val result = parser.parse("rendu kg thoor dhal")
        assertEquals(CommandIntent.ADD, result.intent)
        assertEquals(2.0, result.quantity, 0.001)
        assertEquals("kg", result.unit)
        assertEquals("thoor dhal", result.item)
    }

    @Test
    fun testCommitIntent() = runTest {
        val result = parser.parse("bill potru")
        assertEquals(CommandIntent.COMMIT, result.intent)
    }

    @Test
    fun testRemoveLastIntent() = runTest {
        val result = parser.parse("remove last")
        assertEquals(CommandIntent.REMOVE_LAST, result.intent)
    }

    @Test
    fun testOpenCameraIntent() = runTest {
        val result = parser.parse("open camera")
        assertEquals(CommandIntent.OPEN_CAMERA, result.intent)
    }

    @Test
    fun testEnglishNumberWithPackets() = runTest {
        val result = parser.parse("three packet parle g")
        assertEquals(CommandIntent.ADD, result.intent)
        assertEquals(3.0, result.quantity, 0.001)
        assertEquals("pack", result.unit)
        assertEquals("parle g", result.item)
    }

    @Test
    fun testUnknownWhenEmpty() = runTest {
        val result = parser.parse("   ")
        assertEquals(CommandIntent.UNKNOWN, result.intent)
    }
}
