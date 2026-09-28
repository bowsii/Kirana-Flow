package com.kiranaflow.ai.nlu

import com.kiranaflow.core.model.CommandIntent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
        val results = parser.parse("rendu kg thoor dhal")
        assertEquals(1, results.size)
        val result = results[0]
        assertEquals(CommandIntent.ADD, result.intent)
        assertEquals(2.0, result.quantity, 0.001)
        assertEquals("kg", result.unit)
        assertEquals("thoor dhal", result.item)
        assertTrue("Confidence should be >= 0.90 for quantity+unit+item", result.confidence >= 0.90f)
    }

    @Test
    fun testCommitIntent() = runTest {
        val results = parser.parse("bill potru")
        assertEquals(1, results.size)
        val result = results[0]
        assertEquals(CommandIntent.COMMIT, result.intent)
        assertTrue("Shortcut confidence should be high", result.confidence >= 0.95f)
    }

    @Test
    fun testRemoveLastIntent() = runTest {
        val results = parser.parse("remove last")
        assertEquals(1, results.size)
        val result = results[0]
        assertEquals(CommandIntent.REMOVE_LAST, result.intent)
        assertTrue(result.confidence >= 0.95f)
    }

    @Test
    fun testOpenCameraIntent() = runTest {
        val results = parser.parse("open camera")
        assertEquals(1, results.size)
        val result = results[0]
        assertEquals(CommandIntent.OPEN_CAMERA, result.intent)
        assertTrue(result.confidence >= 0.95f)
    }

    @Test
    fun testEnglishNumberWithPackets() = runTest {
        val results = parser.parse("three packet parle g")
        assertEquals(1, results.size)
        val result = results[0]
        assertEquals(CommandIntent.ADD, result.intent)
        assertEquals(3.0, result.quantity, 0.001)
        assertEquals("pack", result.unit)
        assertEquals("parle g", result.item)
        assertTrue(result.confidence >= 0.90f)
    }

    @Test
    fun testUnknownWhenEmpty() = runTest {
        val results = parser.parse("   ")
        assertEquals(1, results.size)
        val result = results[0]
        assertEquals(CommandIntent.UNKNOWN, result.intent)
        assertTrue("Unknown command should have low confidence", result.confidence <= 0.2f)
    }
}
