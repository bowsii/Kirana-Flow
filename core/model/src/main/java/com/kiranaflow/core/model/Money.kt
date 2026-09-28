package com.kiranaflow.core.model

import kotlinx.serialization.Serializable

/**
 * Value class representing an amount of money stored as Long in paise (1 INR = 100 paise).
 * Eliminates floating-point inaccuracies.
 */
@JvmInline
@Serializable
value class Money(val paise: Long) : Comparable<Money> {

    operator fun plus(other: Money): Money = Money(paise + other.paise)
    operator fun minus(other: Money): Money = Money(paise - other.paise)
    operator fun times(scalar: Long): Money = Money(paise * scalar)
    operator fun times(scalar: Double): Money = Money((paise * scalar + 0.5).toLong())
    operator fun div(divisor: Long): Money = Money(paise / divisor)

    override fun compareTo(other: Money): Int = paise.compareTo(other.paise)

    val inRupeesDouble: Double get() = paise / 100.0
    val rupeesOnly: Long get() = paise / 100
    val paiseRemainder: Long get() = paise % 100

    /**
     * Formats with Indian numbering system (e.g. ₹16,420 or ₹209).
     */
    fun toFormattedString(includeSymbol: Boolean = true): String {
        val rupees = paise / 100
        val rem = paise % 100
        val prefix = if (includeSymbol) "₹" else ""
        val formatted = formatIndianNumber(rupees)
        return if (rem == 0L) {
            "$prefix$formatted"
        } else {
            "$prefix$formatted.%02d".format(rem)
        }
    }

    override fun toString(): String = toFormattedString()

    companion object {
        val ZERO = Money(0L)

        fun fromPaise(paise: Long): Money = Money(paise)
        fun fromRupees(rupees: Long): Money = Money(rupees * 100L)
        fun fromRupees(rupees: Double): Money = Money((rupees * 100.0 + 0.5).toLong())

        fun formatIndianNumber(n: Long): String {
            if (n < 0) return "-" + formatIndianNumber(-n)
            if (n < 1000) return n.toString()
            val s = n.toString()
            val lastThree = s.takeLast(3)
            val rest = s.dropLast(3)
            val groups = mutableListOf<String>()
            var i = rest.length
            while (i > 0) {
                val start = (i - 2).coerceAtLeast(0)
                groups.add(0, rest.substring(start, i))
                i -= 2
            }
            return groups.joinToString(",") + "," + lastThree
        }
    }
}
