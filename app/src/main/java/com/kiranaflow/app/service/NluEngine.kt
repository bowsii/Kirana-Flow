package com.kiranaflow.app.service

import com.kiranaflow.core.model.CommandIntent
import com.kiranaflow.core.model.VoiceCommand
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Rule-based NLU engine for Tamil / Tanglish billing commands.
 *
 * In production this will be replaced by Gemma-3n-E2B INT4 via ONNX Runtime.
 * For the hackathon prototype, a deterministic rule engine gives identical
 * output without requiring model inference infrastructure.
 *
 * Handles:
 *   Tamil number words: oru(1) rendu(2) moonu(3) naalu(4) aanju(5) aaru(6)
 *                       ezhu(7) ettu(8) onbathu(9) pathu(10)
 *   English number words: one two three … ten
 *   Bare numerals: 2 Parle-G, 3 parle g
 *   Intent keywords: bill potru / commit, remove last / thiri, open camera
 */
@Singleton
class NluEngine @Inject constructor() {

    // ─── Tamil number words (Tanglish transliterations) ─────────────────────
    private val tamilNumbers = mapOf(
        "oru" to 1.0, "onnu" to 1.0, "ond" to 1.0,
        "rendu" to 2.0, "randu" to 2.0, "iru" to 2.0,
        "moonu" to 3.0, "munu" to 3.0, "mooru" to 3.0,
        "naalu" to 4.0, "nalu" to 4.0, "naan" to 4.0,
        "aanju" to 5.0, "anju" to 5.0,
        "aaru" to 6.0, "aru" to 6.0,
        "ezhu" to 7.0, "yezhu" to 7.0,
        "ettu" to 8.0, "ettu" to 8.0,
        "onbathu" to 9.0, "ombathu" to 9.0,
        "pathu" to 10.0, "patthu" to 10.0,
        "irubathu" to 20.0, "muppathu" to 30.0, "narppathu" to 40.0
    )

    // ─── English number words ─────────────────────────────────────────────────
    private val englishNumbers = mapOf(
        "one" to 1.0, "two" to 2.0, "three" to 3.0, "four" to 4.0,
        "five" to 5.0, "six" to 6.0, "seven" to 7.0, "eight" to 8.0,
        "nine" to 9.0, "ten" to 10.0, "half" to 0.5,
        "quarter" to 0.25, "dozen" to 12.0
    )

    // ─── Unit tokens ─────────────────────────────────────────────────────────
    private val unitTokens = mapOf(
        "kg" to "kg", "kilo" to "kg", "kilogram" to "kg",
        "liter" to "L", "litre" to "L", "l" to "L", "ml" to "ml",
        "gram" to "g", "grams" to "g", "g" to "g",
        "pack" to "pack", "packet" to "pack", "pkt" to "pack",
        "piece" to "pcs", "pieces" to "pcs", "pcs" to "pcs",
        "dozen" to "dozen"
    )

    // ─── Intent keywords ─────────────────────────────────────────────────────
    private val commitKeywords = setOf(
        "bill potru", "bill pottru", "bill", "commit", "done", "finish",
        "save bill", "generate bill", "close", "finalize", "finalise",
        "bill podu", "bill po"
    )
    private val removeKeywords = setOf(
        "remove last", "undo", "cancel last", "delete last", "thiri",
        "remove", "delete", "undo last", "erase last"
    )
    private val cameraKeywords = setOf(
        "open camera", "scan", "barcode", "camera", "scan barcode"
    )
    private val micOnKeywords = setOf("mic on", "start", "listen", "micon")

    // ─── Public API ──────────────────────────────────────────────────────────

    /**
     * Parse a raw transcript string into a [VoiceCommand].
     *
     * @param rawText  The ASR transcript (lowercase preferred but not required)
     * @return         A parsed [VoiceCommand] ready for the Command Router
     */
    fun parse(rawText: String): VoiceCommand {
        val text = rawText.trim().lowercase()

        // 1. Intent shortcuts (exact / prefix match)
        if (commitKeywords.any { text.contains(it) })
            return VoiceCommand(intent = CommandIntent.COMMIT, rawText = rawText)

        if (removeKeywords.any { text.contains(it) })
            return VoiceCommand(intent = CommandIntent.REMOVE_LAST, rawText = rawText)

        if (cameraKeywords.any { text.contains(it) })
            return VoiceCommand(intent = CommandIntent.OPEN_CAMERA, rawText = rawText)

        if (micOnKeywords.any { text.contains(it) })
            return VoiceCommand(intent = CommandIntent.MIC_ON, rawText = rawText)

        // 2. ADD intent: extract quantity + unit + item name
        val tokens = text.split(Regex("\\s+"))
        var quantity = 1.0
        var unit: String? = null
        val itemTokens = mutableListOf<String>()

        var i = 0
        while (i < tokens.size) {
            val tok = tokens[i]
            when {
                // Tamil number word
                tamilNumbers.containsKey(tok) -> {
                    quantity = tamilNumbers[tok]!!
                }
                // English number word
                englishNumbers.containsKey(tok) -> {
                    quantity = englishNumbers[tok]!!
                }
                // Bare numeral (e.g. "2", "1.5")
                tok.toDoubleOrNull() != null -> {
                    quantity = tok.toDouble()
                }
                // Unit token
                unitTokens.containsKey(tok) -> {
                    unit = unitTokens[tok]
                }
                // Otherwise it's part of the item name
                else -> itemTokens.add(tok)
            }
            i++
        }

        val itemName = itemTokens.joinToString(" ").trim()

        return if (itemName.isEmpty()) {
            VoiceCommand(intent = CommandIntent.UNKNOWN, rawText = rawText)
        } else {
            VoiceCommand(
                intent   = CommandIntent.ADD,
                rawText  = rawText,
                item     = itemName,
                quantity = quantity,
                unit     = unit
            )
        }
    }
}
