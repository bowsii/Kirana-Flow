package com.kiranaflow.ai.nlu

import com.kiranaflow.core.model.CommandIntent
import com.kiranaflow.core.model.VoiceCommand
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Deterministic rule-based NLU engine implementing [IntentParser].
 * Handles Tamil / Tanglish number words, bare numerals, units, and billing keywords.
 */
@Singleton
class RuleBasedIntentParser @Inject constructor() : IntentParser {

    private val tamilNumbers = mapOf(
        "oru" to 1.0, "onnu" to 1.0, "ond" to 1.0,
        "rendu" to 2.0, "randu" to 2.0, "iru" to 2.0,
        "moonu" to 3.0, "munu" to 3.0, "mooru" to 3.0,
        "naalu" to 4.0, "nalu" to 4.0, "naan" to 4.0,
        "aanju" to 5.0, "anju" to 5.0,
        "aaru" to 6.0, "aru" to 6.0,
        "ezhu" to 7.0, "yezhu" to 7.0,
        "ettu" to 8.0,
        "onbathu" to 9.0, "ombathu" to 9.0,
        "pathu" to 10.0, "patthu" to 10.0,
        "irubathu" to 20.0, "muppathu" to 30.0, "narppathu" to 40.0
    )

    private val englishNumbers = mapOf(
        "one" to 1.0, "two" to 2.0, "three" to 3.0, "four" to 4.0,
        "five" to 5.0, "six" to 6.0, "seven" to 7.0, "eight" to 8.0,
        "nine" to 9.0, "ten" to 10.0, "half" to 0.5,
        "quarter" to 0.25, "dozen" to 12.0
    )

    private val unitTokens = mapOf(
        "kg" to "kg", "kilo" to "kg", "kilogram" to "kg",
        "liter" to "L", "litre" to "L", "l" to "L", "ml" to "ml",
        "gram" to "g", "grams" to "g", "g" to "g",
        "pack" to "pack", "packet" to "pack", "pkt" to "pack",
        "piece" to "pcs", "pieces" to "pcs", "pcs" to "pcs",
        "dozen" to "dozen"
    )

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

    override suspend fun parse(utterance: String): VoiceCommand {
        val text = utterance.trim().lowercase()

        // 1. Intent shortcuts
        if (commitKeywords.any { text.contains(it) })
            return VoiceCommand(intent = CommandIntent.COMMIT, rawText = utterance)

        if (removeKeywords.any { text.contains(it) })
            return VoiceCommand(intent = CommandIntent.REMOVE_LAST, rawText = utterance)

        if (cameraKeywords.any { text.contains(it) })
            return VoiceCommand(intent = CommandIntent.OPEN_CAMERA, rawText = utterance)

        if (micOnKeywords.any { text.contains(it) })
            return VoiceCommand(intent = CommandIntent.MIC_ON, rawText = utterance)

        // 2. ADD intent: extract quantity + unit + item name
        val tokens = text.split(Regex("\\s+"))
        var quantity = 1.0
        var unit: String? = null
        val itemTokens = mutableListOf<String>()

        var i = 0
        while (i < tokens.size) {
            val tok = tokens[i]
            when {
                tamilNumbers.containsKey(tok) -> {
                    quantity = tamilNumbers[tok]!!
                }
                englishNumbers.containsKey(tok) -> {
                    quantity = englishNumbers[tok]!!
                }
                tok.toDoubleOrNull() != null -> {
                    quantity = tok.toDouble()
                }
                unit == null && unitTokens.containsKey(tok) -> {
                    unit = unitTokens[tok]
                }
                else -> itemTokens.add(tok)
            }
            i++
        }

        val itemName = itemTokens.joinToString(" ").trim()

        return if (itemName.isEmpty()) {
            VoiceCommand(intent = CommandIntent.UNKNOWN, rawText = utterance)
        } else {
            VoiceCommand(
                intent   = CommandIntent.ADD,
                rawText  = utterance,
                item     = itemName,
                quantity = quantity,
                unit     = unit
            )
        }
    }
}
