package com.kiranaflow.core.model

import com.kiranaflow.core.common.UuidV7
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class DataCorrectnessTest {

    @Test
    fun testMoneyPaiseArithmetic() {
        val m1 = Money.fromRupees(100L) // 10,000 paise
        val m2 = Money.fromRupees(50.50) // 5,050 paise

        assertEquals(10000L, m1.paise)
        assertEquals(5050L, m2.paise)

        val sum = m1 + m2
        assertEquals(15050L, sum.paise)
        assertEquals("₹150.50", sum.toFormattedString())

        val diff = m1 - m2
        assertEquals(4950L, diff.paise)
        assertEquals("₹49.50", diff.toFormattedString())

        val scaled = m1 * 2L
        assertEquals(20000L, scaled.paise)
        assertEquals("₹200", scaled.toFormattedString())
    }

    @Test
    fun testMoneyIndianNumberFormatting() {
        val amount = Money.fromRupees(16420L)
        assertEquals("₹16,420", amount.toFormattedString())

        val largeAmount = Money.fromRupees(1234567L)
        assertEquals("₹12,34,567", largeAmount.toFormattedString())

        val zero = Money.ZERO
        assertEquals("₹0", zero.toFormattedString())
    }

    @Test
    fun testQuantityBaseUnitsAndConversion() {
        val q1 = Quantity.ofKg(1.5) // 1500 grams
        assertEquals(1500L, q1.baseUnits)
        assertEquals("1.5 kg", q1.toDisplayString(DisplayUnit.KG))

        val q2 = Quantity.of(500L, DisplayUnit.G)
        assertEquals(500L, q2.baseUnits)

        val total = q1 + q2
        assertEquals(2000L, total.baseUnits)
        assertEquals("2 kg", total.toDisplayString(DisplayUnit.KG))

        val milk = Quantity.ofLiters(1.0)
        assertEquals(1000L, milk.baseUnits)
        assertEquals("1 L", milk.toDisplayString(DisplayUnit.L))

        val eggs = Quantity.of(12L, DisplayUnit.PCS)
        assertEquals(12L, eggs.baseUnits)
        assertEquals("12 pcs", eggs.toDisplayString(DisplayUnit.PCS))
    }

    @Test
    fun testUuidV7StructureAndOrdering() {
        val id1 = UuidV7.generate()
        Thread.sleep(5)
        val id2 = UuidV7.generate()

        // Valid UUID format
        val u1 = UUID.fromString(id1)
        val u2 = UUID.fromString(id2)

        // Version should be 7
        assertEquals(7, u1.version())
        assertEquals(7, u2.version())

        // Time ordering: id2 should sort after id1
        assertTrue("UUIDv7 must be chronologically ordered", id2 > id1)
    }

    @Test
    fun testBusinessDayCutoff() {
        val bdm = BusinessDayManager()
        bdm.dayCutoffHour = 2 // 02:00 AM cutoff

        // 2026-09-28 01:30 AM -> should be treated as 2026-09-27
        val calBeforeCutoff = java.util.Calendar.getInstance().apply {
            set(2026, java.util.Calendar.SEPTEMBER, 28, 1, 30, 0)
        }
        val dateBefore = bdm.getBusinessDate(calBeforeCutoff.timeInMillis)
        assertEquals("2026-09-27", dateBefore)

        // 2026-09-28 03:00 AM -> should be treated as 2026-09-28
        val calAfterCutoff = java.util.Calendar.getInstance().apply {
            set(2026, java.util.Calendar.SEPTEMBER, 28, 3, 0, 0)
        }
        val dateAfter = bdm.getBusinessDate(calAfterCutoff.timeInMillis)
        assertEquals("2026-09-28", dateAfter)
    }
}
