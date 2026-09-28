package com.kiranaflow.ai.nlu

import com.kiranaflow.core.model.CommandIntent
import com.kiranaflow.core.model.VoiceCommand

/**
 * A parsed command hypothesis extracted from voice transcription,
 * accompanied by an associated confidence score in the range [0.0f, 1.0f].
 */
data class ParsedCommand(
    val command: VoiceCommand,
    val confidence: Float
) {
    val intent: CommandIntent get() = command.intent
    val item: String? get() = command.item
    val quantity: Double get() = command.quantity
    val unit: String? get() = command.unit
    val rawText: String get() = command.rawText
}
