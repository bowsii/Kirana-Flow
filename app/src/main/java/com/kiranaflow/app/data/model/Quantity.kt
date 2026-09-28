package com.kiranaflow.app.data.model

import kotlinx.serialization.Serializable

/**
 * Base units:
 * - PIECE: 1 piece = 1 base unit
 * - GRAM: 1 gram = 1 base unit (1 kg = 1000 base units)
 * - MILLILITER: 1 ml = 1 base unit (1 L = 1000 base units)
 */
@Serializable
enum class BaseUnitType {
    PIECE,
    GRAM,
    MILLILITER
}

/**
 * Display units with multiplier to convert to/from base units.
 */
@Serializable
enum class DisplayUnit(
    val label: String,
    val baseUnit: BaseUnitType,
    val multiplierToBase: Long
) {
    PCS("pcs", BaseUnitType.PIECE, 1L),
    PACK("pack", BaseUnitType.PIECE, 1L),
    DOZEN("dozen", BaseUnitType.PIECE, 12L),
    KG("kg", BaseUnitType.GRAM, 1000L),
    G("g", BaseUnitType.GRAM, 1L),
    L("L", BaseUnitType.MILLILITER, 1000L),
    ML("ml", BaseUnitType.MILLILITER, 1L);

    companion object {
        fun fromString(str: String): DisplayUnit {
            return when (str.lowercase().trim()) {
                "kg", "kilo" -> KG
                "g", "gram", "grams" -> G
                "l", "liter", "litre" -> L
                "ml" -> ML
                "dozen" -> DOZEN
                "pack", "pkt" -> PACK
                else -> PCS
            }
        }
    }
}

/**
 * Quantity value class stored as Long in base units.
 * Eliminates floating point inaccuracies when measuring fractional amounts.
 */
@JvmInline
@Serializable
value class Quantity(val baseUnits: Long) : Comparable<Quantity> {

    operator fun plus(other: Quantity): Quantity = Quantity(baseUnits + other.baseUnits)
    operator fun minus(other: Quantity): Quantity = Quantity(baseUnits - other.baseUnits)
    operator fun times(scalar: Long): Quantity = Quantity(baseUnits * scalar)
    operator fun times(scalar: Double): Quantity = Quantity((baseUnits * scalar + 0.5).toLong())
    operator fun div(divisor: Long): Quantity = Quantity(baseUnits / divisor)

    override fun compareTo(other: Quantity): Int = baseUnits.compareTo(other.baseUnits)

    fun toDisplayString(displayUnit: DisplayUnit): String {
        return when (displayUnit) {
            DisplayUnit.KG -> {
                if (baseUnits % 1000L == 0L) "${baseUnits / 1000L} kg"
                else if (baseUnits < 1000L) "$baseUnits g"
                else "${baseUnits / 1000.0} kg"
            }
            DisplayUnit.L -> {
                if (baseUnits % 1000L == 0L) "${baseUnits / 1000L} L"
                else if (baseUnits < 1000L) "$baseUnits ml"
                else "${baseUnits / 1000.0} L"
            }
            DisplayUnit.DOZEN -> {
                if (baseUnits % 12L == 0L) "${baseUnits / 12L} dozen"
                else "$baseUnits pcs"
            }
            DisplayUnit.G -> "$baseUnits g"
            DisplayUnit.ML -> "$baseUnits ml"
            else -> "$baseUnits ${displayUnit.label}"
        }
    }

    companion object {
        val ZERO = Quantity(0L)

        fun of(amount: Long, unit: DisplayUnit): Quantity = Quantity(amount * unit.multiplierToBase)
        fun of(amount: Double, unit: DisplayUnit): Quantity = Quantity((amount * unit.multiplierToBase + 0.5).toLong())
        fun ofPieces(count: Long): Quantity = Quantity(count)
        fun ofGrams(grams: Long): Quantity = Quantity(grams)
        fun ofKg(kg: Double): Quantity = Quantity((kg * 1000.0 + 0.5).toLong())
        fun ofMl(ml: Long): Quantity = Quantity(ml)
        fun ofLiters(liters: Double): Quantity = Quantity((liters * 1000.0 + 0.5).toLong())
    }
}
