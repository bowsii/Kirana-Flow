package com.kiranaflow.core.model

import kotlinx.serialization.Serializable

/**
 * Structured command parsed from voice input.
 *
 * Examples:
 *   "Rendu Parle-G"   → VoiceCommand(intent=ADD, item="Parle-G", quantity=2.0)
 *   "Bill potru"      → VoiceCommand(intent=COMMIT)
 *   "Remove last"     → VoiceCommand(intent=REMOVE_LAST)
 *   "Open camera"     → VoiceCommand(intent=OPEN_CAMERA)
 */
@Serializable
data class VoiceCommand(
    val intent: CommandIntent,
    val rawText: String = "",
    val item: String? = null,
    val quantity: Double = 1.0,
    val unit: String? = null
)

@Serializable
enum class CommandIntent {
    ADD,          // add item to cart
    REMOVE_LAST,  // undo last cart line
    COMMIT,       // "bill potru" — finalise and save
    OPEN_CAMERA,  // barcode fallback
    MIC_ON,       // start listening
    UNKNOWN       // NLU couldn't parse
}
