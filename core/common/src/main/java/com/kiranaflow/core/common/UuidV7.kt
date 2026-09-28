package com.kiranaflow.core.common

import java.security.SecureRandom
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

/**
 * RFC 9562 UUIDv7 generator.
 *
 * UUIDv7 provides time-ordered, millisecond-precision UUIDs that sort chronologically
 * and maintain optimal B-tree locality for SQLite / Room primary keys.
 */
object UuidV7 {
    private val secureRandom = SecureRandom()
    private val counter = AtomicInteger(secureRandom.nextInt(0x0FFF))

    fun generate(): String {
        val timestampMs = System.currentTimeMillis()
        val count = counter.incrementAndGet() and 0x0FFF

        // Most Significant Bits:
        // 48 bits: Unix timestamp in ms
        // 4 bits: Version 7 (0x7)
        // 12 bits: Counter
        val msb = (timestampMs shl 16) or (0x7000L) or count.toLong()

        // Least Significant Bits:
        // 2 bits: Variant 1 (0b10)
        // 62 bits: Cryptographically secure random
        val randomLong = secureRandom.nextLong()
        val lsb = (0x8000000000000000UL.toLong()) or (randomLong and 0x3FFFFFFFFFFFFFFFL)

        return UUID(msb, lsb).toString()
    }
}
